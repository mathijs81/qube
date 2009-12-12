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

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import nl.umcg.qube.io.AbstractSequenceProvider.SequenceLoadListener;
import nl.umcg.qube.model.CAGFrame;
import nl.umcg.qube.model.CAGSequence;
import nl.umcg.qube.model.SequenceIdentifier;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.display.SourceImage;

/**
 * @author mathijs
 * 
 */
public class DicomLoader {

    /**
     * Load a DICOM sequence from a file asynchronously. The provided listener
     * will be notified when the loading is done.
     * 
     * @param file
     *            The file to load the sequence from.
     * @param identifier
     *            The SequenceIdentifier of this file.
     * @param listen
     *            The listener to notify when the loading is done.
     */
    public static void loadFromFileAsync(final File file, final SequenceIdentifier identifier,
            final SequenceLoadListener listen) {
        new Thread(new Runnable() {
            public void run() {
                DicomInputStream in = null;
                try {
                    in = new DicomInputStream(new BufferedInputStream(new FileInputStream(file)));
                    AttributeList al = new AttributeList();

                    al.read(in, TagFromName.PixelData);

                    int frames = ((Attribute) al.get(TagFromName.NumberOfFrames))
                            .getSingleIntegerValueOrDefault(2);
                    in.readSigned32();
                    in.readSigned32();
                    String readerWanted = "jpeg-lossless";

                    ImageReader reader = (ImageReader) (ImageIO
                            .getImageReadersByFormatName(readerWanted).next());
                    in.readUnsigned16();
                    in.readUnsigned16();
                    in.readUnsigned32(); // length
                    long[] offs = new long[frames];

                    for (int i = 0; i < frames; i++)
                        offs[i] = in.readUnsigned32();
                    in.skipInsistently(8);
                    ImageInputStream iiois = ImageIO.createImageInputStream(in);

                    CAGFrame[] cagFrames = new CAGFrame[frames];

                    for (int i = 0; i < frames; i++) {
                        if (iiois.getStreamPosition() != offs[i])
                            iiois.skipBytes(offs[i] - iiois.getStreamPosition());
                        reader.setInput(iiois);
                        BufferedImage image = reader.read(0);
                        Raster raster = image.getRaster();
                        byte[][] bytes = new byte[raster.getHeight()][raster.getWidth()];
                        for (int x = 0; x < raster.getWidth(); x++)
                            for (int y = 0; y < raster.getHeight(); y++)
                                bytes[y][x] = (byte) raster.getSample(x, y, 0);
                        cagFrames[i] = new CAGFrame(bytes);
                        if (!listen.update(i, image))
                            return;
                    }
                    CAGSequence sequence = new CAGSequence(cagFrames, identifier);
                    sequence.setPatientName(al.get(TagFromName.PatientName)
                            .getSingleStringValueOrDefault(""));
                    sequence.setStudyID(al.get(TagFromName.ContentTime) == null ? "" : al.get(
                            TagFromName.ContentTime).getSingleStringValueOrDefault(""));

                    listen.finished(sequence);
                } catch (Exception e) {
                    listen.error(e);
                } finally {
                    if (in != null)
                        try {
                            in.close();
                        } catch (Exception e) {
                        }
                }
            }
        }).start();
    }

    public static CAGSequence loadFromFile(File file, SequenceIdentifier id) throws IOException {
        try {
            DicomInputStream in = new DicomInputStream(file);
            AttributeList al = new AttributeList();
            al.read(in);
            SourceImage image = new SourceImage(al);
            int count = image.getNumberOfBufferedImages();
            int picwidth = image.getHeight(), picheight = image.getHeight();
            int downscale = Math.max(1, Math.max((picwidth + 511) / 512, (picheight + 511) / 512));
            int divider = 1;
            int bits = al.get(TagFromName.BitsStored).getSingleIntegerValueOrDefault(8);

            divider = (1 << (bits - 8));
            picwidth = picwidth / downscale;
            picheight = picheight / downscale;
            in.close();
            CAGFrame[] frames = new CAGFrame[count];

            for (int i = 0; i < count; i++) {
                byte[][] values = new byte[picheight][picwidth];

                // int max = 0;
                Raster cur = image.getBufferedImage(i).getRaster();
                for (int x = 0; x < picwidth; x++)
                    for (int y = 0; y < picheight; y++) {
                        // max = Math.max(cur.getSample(x*downscale,
                        // y*downscale, 0), max);
                        values[y][x] = (byte) (cur.getSample(x * downscale, y * downscale, 0) / divider);
                    }
                // System.out.println(max);
                frames[i] = new CAGFrame(values);
            }
            CAGSequence sequence = new CAGSequence(frames, id);
            Attribute patient = al.get(TagFromName.PatientName);
            Attribute contentTime = al.get(TagFromName.ContentTime);
            sequence.setPatientName(patient == null ? "" : patient
                    .getSingleStringValueOrDefault(""));
            sequence.setStudyID(contentTime == null ? "" : contentTime
                    .getSingleStringValueOrDefault(""));
            return sequence;
        } catch (DicomException de) {
            throw new IOException(de);
        }
    }

}
