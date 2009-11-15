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

import java.io.Serializable;

/**
 * SequenceIdentifier is a class representing the most basic information needed
 * to identify a CAG sequence. A SequenceProvider should be able to load a movie
 * using the SequenceIdentifier class.
 * 
 * @author mathijs
 */
public class SequenceIdentifier implements Serializable {
    private static final long serialVersionUID = 1L;
    public final String patientID;
    public final String sessionID;
    public final String sequenceID;

    public SequenceIdentifier(String patientID, String sessionID, String sequenceID) {
        this.patientID = patientID;
        this.sessionID = sessionID;
        this.sequenceID = sequenceID;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SequenceIdentifier) {
            SequenceIdentifier mi = (SequenceIdentifier) o;
            return patientID.equals(mi.patientID) && sessionID.equals(mi.sessionID)
                    && sequenceID.equals(mi.sequenceID);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return patientID.hashCode() * 7 + sessionID.hashCode() + sequenceID.hashCode() * 31;
    }

    @Override
    public String toString() {
        return patientID + "/" + sessionID + "/" + sequenceID;
    }

}
