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
package nl.umcg.qube.model;

/**
 * @author mathijs
 * 
 */
public class CAGSequence {
    protected CAGFrame[] frames;
    protected String patientName;
    protected String studyID;
    protected final SequenceIdentifier sequenceID;

    public SequenceIdentifier getID() {
        return sequenceID;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String name) {
        patientName = name;
    }

    public int frameCount() {
        return frames.length;
    }

    public CAGFrame getFrame(int index) {
        return frames[index];
    }

    public CAGSequence(CAGFrame[] frames, SequenceIdentifier id) {
        this.frames = frames;
        sequenceID = id;
    }

    public int getWidth() {
        return frames[0].getWidth();
    }

    public int getHeight() {
        return frames[0].getHeight();
    }

    public CAGFrame[] getFrames() {
        return frames;
    }

    /**
     * @return the studyID
     */
    public String getStudyID() {
        return studyID;
    }

    /**
     * @param studyID
     *            the studyID to set
     */
    public void setStudyID(String studyID) {
        this.studyID = studyID;
    }
}
