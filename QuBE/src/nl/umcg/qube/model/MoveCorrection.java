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

import java.util.StringTokenizer;

/**
 * This class contains information that is used to correct panning motion in CAG
 * sequences. For every single frame, an integer pixel offset is calculated.
 * Usually (this is up to the calculating class) these offsets are normalized
 * such that the middle frame in the sequence has offset 0.
 * 
 * @author mathijs
 */
public class MoveCorrection {
    public int[] dx, dy;

    public MoveCorrection(int[] dx, int[] dy) {
        if (dx.length != dy.length)
            throw new IllegalArgumentException("dx.length != dy.length");
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Store this class to a string (space separated list of length and then all
     * indices)
     * 
     * @return A stored list
     */
    public String storeString() {
        StringBuffer buf = new StringBuffer();
        buf.append(dx.length);

        for (int i = 0; i < dx.length; i++) {
            buf.append(" ");
            buf.append(dx[i]);
            buf.append(" ");
            buf.append(dy[i]);
        }
        return buf.toString();
    }

    /**
     * Construct this class from a stored string
     * 
     * @param str
     *            the string to construct the class from
     * @return The constructed instance
     */
    public static MoveCorrection fromString(String str) {
        StringTokenizer tok = new StringTokenizer(str);

        int len = Integer.parseInt(tok.nextToken());
        int[] dx = new int[len], dy = new int[len];
        for (int i = 0; i < len; i++) {
            dx[i] = Integer.parseInt(tok.nextToken());
            dy[i] = Integer.parseInt(tok.nextToken());
        }
        return new MoveCorrection(dx, dy);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MoveCorrection))
            return false;
        return storeString().equals(((MoveCorrection) obj).storeString());
    }

    @Override
    public int hashCode() {
        return storeString().hashCode();
    }
}
