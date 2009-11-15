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

import java.lang.ref.SoftReference;

/**
 * CAGFrame represents a single frame in a coronary angiography sequence. This
 * class uses a byte array to store the 8-bit values (unfortunately java byte is
 * signed), and converts this array to an array of ints on demand. The int array
 * is kept in a softreference so that it is freed when there is not enough
 * memory available.
 * 
 * @author mathijs
 */
public class CAGFrame {
    protected byte[][] values;

    protected int width, height;

    protected SoftReference<short[][]> bufferedPixels = null;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getValue(int x, int y) {
        if (values[y][x] < 0)
            return values[y][x] + 256;
        return values[y][x];
    }

    /**
     * Return an array of int values for all pixels. This is more efficient than
     * calling getValue(x,y) very often. The returned array is not cloned, thus
     * it must not be altered by the caller.
     * 
     * @return An array of greyvalues
     */
    public short[][] getValues() {
        short[][] vals = null;

        if (bufferedPixels != null)
            vals = bufferedPixels.get();

        if (vals == null) {
            vals = new short[height][width];
            for (int y = 0; y < height; y++)
                for (int x = 0; x < width; x++) {
                    if (values[y][x] < 0)
                        vals[y][x] = (short) (256 + values[y][x]);
                    else
                        vals[y][x] = values[y][x];
                }
            bufferedPixels = new SoftReference<short[][]>(vals);
        }
        return vals;
    }

    public CAGFrame(byte[][] values) {
        this.values = values;
        width = values[0].length;
        height = values.length;
    }
}
