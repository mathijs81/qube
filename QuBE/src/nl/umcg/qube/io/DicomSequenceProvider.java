/* (C) 2009 Mathijs Vogelzang/University Medical Center Groningen.
 * For more information go to http://qube.sf.net/ 
 * 
 * This file is part of QuBE (Quantitative Blush Evaluator).
 * 
 * QuBE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * QuBE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with QuBE.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.umcg.qube.io;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import nl.umcg.qube.model.SequenceIdentifier;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomDirectory;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;

/**
 * DicomMovieProvider is a AbstractMovieProvider that can read CAG sessions from
 * a filesystem (usually a network drive).
 * 
 * @author mathijs
 */
public class DicomSequenceProvider extends AbstractSequenceProvider {

    private static final String DIR_FILE_NAME = "DICOMDIR";
    protected String baseDir;

    /**
     * Helper class to keep track of movieidentifiers and the file where they're
     * stored.
     */
    protected class FileAndIdentifier implements Comparable<FileAndIdentifier> {
        SequenceIdentifier mi;
        File file;

        public FileAndIdentifier(File file, SequenceIdentifier mi) {
            this.file = file;
            this.mi = mi;
        }

        @Override
        public int compareTo(FileAndIdentifier o) {
            return file.compareTo(o.file);
        }
    }

    /**
     * Instantiate a new DicomMovieProvider.
     * 
     * @param baseDir
     *            The directory where to get information from.
     */
    public DicomSequenceProvider(String baseDir) {
        this.baseDir = baseDir;
    }

    protected String findSessionDir(String patientID) {
        boolean validNumber = true;
        try {
            Integer.parseInt(patientID);
        } catch (Exception e) {
            validNumber = false;
        }

        // We always pad numbers with preceding zeroes until the string is 7
        // characters (might be UMCG specific)
        while (validNumber && patientID.length() < 7)
            patientID = "0" + patientID;

        // Might also be UMCG specific: patient 1234567 is stored in directory
        // /67/1234567 (ie. in a directory containing the last two chars)
        return new File(baseDir, patientID.substring(patientID.length() - 2) + File.separator
                + patientID + File.separator).getAbsolutePath();
    }

    /**
     * Read all sessions and movies for a specific patient.
     * 
     * @param patientId
     *            The patient identifier
     * @return A list of sessions, each of which is a list of FileAndIdentifiers
     * @throws IOException
     *             When an IO error occurs.
     */
    protected ArrayList<ArrayList<FileAndIdentifier>> readSessionsAndMovies(final String patientId)
            throws IOException {
        ArrayList<ArrayList<FileAndIdentifier>> result = new ArrayList<ArrayList<FileAndIdentifier>>();
        final String sessionDir = findSessionDir(patientId);
        File dir = new File(sessionDir);
        if (!dir.exists())
            throw new IOException("No sessions found for patient " + patientId);

        try {
            for (final File file : dir.listFiles())
                if (file.isDirectory()) {
                    ArrayList<FileAndIdentifier> sessionList = new ArrayList<FileAndIdentifier>();

                    // Try to read the DICOMDIR description
                    String dirFileName = file.getAbsolutePath() + File.separator + DIR_FILE_NAME;

                    File dirFile = new File(dirFileName);

                    DicomInputStream in = new DicomInputStream(new BufferedInputStream(
                            new FileInputStream(dirFileName)));
                    AttributeList list = new AttributeList();
                    list.read(in);
                    in.close();
                    DicomDirectory dicomDirectory = new DicomDirectory(list);
                    @SuppressWarnings("unchecked")
                    HashMap allDicomFiles = dicomDirectory
                            .findAllContainedReferencedFileNamesAndTheirRecords(dirFile
                                    .getParentFile().getPath());
                    @SuppressWarnings("unchecked")
                    Iterator it = allDicomFiles.keySet().iterator();

                    while (it.hasNext()) {
                        String mediaFileName = (String) it.next();
                        File movieFile = new File(mediaFileName);
                        SequenceIdentifier id = new SequenceIdentifier(patientId, file.getName(),
                                movieFile.getName());
                        sessionList.add(new FileAndIdentifier(movieFile, id));
                    }
                    Collections.sort(sessionList);

                    result.add(sessionList);
                }
        } catch (DicomException de) {
            throw new IOException(de);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see mathijs.blush.AbstractMovieSource#getSessions(java.lang.String)
     */
    @Override
    public List<ICAGSession> getSessions(final String patientId) throws IOException {
        final ArrayList<ArrayList<FileAndIdentifier>> sessions = readSessionsAndMovies(patientId);
        ExecutorService serv = Executors.newFixedThreadPool(3);
        ArrayList<ICAGSession> cagsessions = new ArrayList<ICAGSession>();

        for (final ArrayList<FileAndIdentifier> sessList : sessions) {
            final ArrayList<Future<BufferedImage>> images = new ArrayList<Future<BufferedImage>>();

            for (int i = 0; i < sessList.size(); i++) {
                final int index = i;
                images.add(serv.submit(new Callable<BufferedImage>() {
                    @Override
                    public BufferedImage call() throws Exception {
                        return DicomUtil.extractThumbFrame(sessList.get(index).file);
                    }
                }));

            }

            cagsessions.add(new ICAGSession() {

                @Override
                public int getSequenceCount() {
                    return images.size();
                }

                @Override
                public Image getSequenceImage(int index) {
                    try {
                        return images.get(index).get();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                public String getName() {
                    return sessList.get(0).mi.sessionID;
                }

                @Override
                public void retrieveSequence(int index, SequenceLoadListener mll) {
                    File movieFile = sessList.get(index).file;
                    SequenceIdentifier id = sessList.get(index).mi;
                    DicomLoader.loadFromFileAsync(movieFile, id, mll);
                }

                @Override
                public SequenceIdentifier getID(int index) {
                    return sessList.get(index).mi;
                }
            });
        }
        return cagsessions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * mathijs.blush.models.AbstractMovieSource#getMovie(mathijs.blush.models
     * .MovieIdentifier)
     */
    @Override
    public void getSequence(SequenceIdentifier mi, SequenceLoadListener mll) {
        ArrayList<ArrayList<FileAndIdentifier>> res;

        if (mi.patientID.equals("")) {
            DicomLoader.loadFromFileAsync(new File(mi.sequenceID), mi, mll);
            return;
        }

        try {
            res = readSessionsAndMovies(mi.patientID);
        } catch (IOException ioe) {
            mll.error(ioe);
            return;
        }
        for (ArrayList<FileAndIdentifier> sess : res) {
            for (FileAndIdentifier fai : sess) {
                if (fai.mi.equals(mi)) {
                    DicomLoader.loadFromFileAsync(fai.file, fai.mi, mll);
                    return;
                }
            }
        }
        mll.error(new IOException("Movie not found: " + mi));
    }
}
