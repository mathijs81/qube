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
import java.io.IOException;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

import nl.umcg.qube.model.CAGSequence;
import nl.umcg.qube.model.SequenceIdentifier;

/**
 * AbstractSequenceProvider is an abstract class that provides functionality to
 * load a CAG sequence. Ideally, the user interface should not block when
 * loading a large file over the network, and therefore, a callback mechanism is
 * provided. If this is not needed, the static method "getSequenceFromSource"
 * can be used, which will block until the sequence is fully loaded and then
 * return that sequence (or throw an exception)
 * 
 * @author mathijs
 */
public abstract class AbstractSequenceProvider {

    public static CAGSequence getSequenceFromSource(final AbstractSequenceProvider source,
            final SequenceIdentifier mi) throws IOException {
        final SynchronousQueue<Object> sequenceLink = new SynchronousQueue<Object>();
        new Thread() {
            public void run() {
                source.getSequence(mi, new SequenceLoadListener() {

                    @Override
                    public void error(Throwable t) {
                        try {
                            sequenceLink.put(t);
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }

                    @Override
                    public void finished(CAGSequence sequence) {
                        try {
                            sequenceLink.put(sequence);
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }

                    @Override
                    public boolean update(int frame, BufferedImage currImage) {
                        return true;
                    }
                });
            }
        }.start();
        try {
            Object o = sequenceLink.take();
            if (o instanceof Throwable)
                throw new IOException((Throwable) o);
            return (CAGSequence) o;
        } catch (InterruptedException ie) {
            throw new IOException("Did not get sequenceLink.");
        }
    }

    /**
     * Callback to be called when a sequence is loaded. The loader can provide
     * status and frames during loading, so that the user can see the sequence
     * as it is being loaded.
     * 
     * @author mathijs
     */
    public interface SequenceLoadListener {
        public void finished(CAGSequence sequence);

        /**
         * Update the status, signal that a frame has been read, and allow the
         * listener to terminate the loading of the sequence.
         * 
         * @param frame
         *            The number of the frame just read.
         * @param currImage
         *            The image data of the frame that was just read.
         * @return whether the process should continue.
         */
        public boolean update(int frame, BufferedImage currImage);

        public void error(Throwable t);
    }

    /**
     * An interface defining a session containing multiple CAG sequences.
     * 
     * @author mathijs
     */
    public interface ICAGSession {
        /**
         * Get the identifier for a specific sequence in this session
         * 
         * @param index
         *            The index number of the sequence
         * @return The SequenceIdentifier corresponding to that index
         */
        public SequenceIdentifier getID(int index);

        /**
         * Get the name of this session
         * 
         * @return name of the session
         */
        public String getName();

        /**
         * Return the total number of sequences in this session
         * 
         * @return the number of sequences. Valid indices should be 0 ..
         *         sequencecount-1
         */
        public int getSequenceCount();

        /**
         * Get a thumbnail image for a sequence
         * 
         * @param index
         *            the index of the sequence
         * @return A thumbnail image
         */
        public Image getSequenceImage(int index);

        /**
         * Retrieve a CAG sequence
         * 
         * @param index
         *            the index of the sequence
         * @param mll
         *            the callback to call after loading the sequence
         */
        public void retrieveSequence(int index, SequenceLoadListener sll);
    }

    /**
     * Retrieve sessions for a patient
     * 
     * @param patientId
     *            a patient identification string
     * @return a list of sessions for a patient
     * @throws IOException
     */
    public abstract List<ICAGSession> getSessions(String patientId) throws IOException;

    /**
     * Retrieve a sequence with a specific identifier
     * 
     * @param mi
     *            the sequence identifier of a sequence
     * @param mll
     *            the callback to be called when the sequence is loaded
     */
    public abstract void getSequence(SequenceIdentifier si, SequenceLoadListener sll);
}
