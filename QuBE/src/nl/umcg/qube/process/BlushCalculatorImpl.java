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
package nl.umcg.qube.process;

import java.awt.Point;
import java.util.HashSet;
import java.util.List;

import nl.umcg.qube.model.CAGSequence;
import nl.umcg.qube.model.MoveCorrection;
import nl.umcg.qube.model.Polygon;

/**
 * This class performs the calculation of the QuBE value, given a CAG sequence,
 * the information for panning correction and the user-drawn polygon.
 * 
 * @author mathijs
 */
public class BlushCalculatorImpl implements IBlushCalculator {
    private static final int BLOCKSIZE = 5;
    private static final int MEDIAN_FILTER_SIZE = 35;

    public CalculationResult calculate(CAGSequence sequence, Polygon poly, MoveCorrection mc) {
        // Array holding all median filtered values of the sequence
        short[][][] medpixels = new short[sequence.frameCount()][][];

        // Array holding all source pixels of the sequence
        short[][][] pixels = new short[sequence.frameCount()][][];

        // Get source & median filtered pixels
        for (int i = 0; i < Math.min(125, sequence.frameCount()); i++) {
            pixels[i] = sequence.getFrame(i).getValues();
            medpixels[i] = MedianFilter.fastBoundaryfilter(pixels[i], MEDIAN_FILTER_SIZE);
        }

        // Rasterize the polygon.
        List<Point> points = PolygonRasterizer.rasterize(poly);
        double[] curveValues = new double[sequence.frameCount()];
        HashSet<Integer> blocks = new HashSet<Integer>();
        for (Point p : points)
            blocks.add(((p.y / BLOCKSIZE) * BLOCKSIZE) * 1000 + ((p.x / BLOCKSIZE) * BLOCKSIZE));
        double[] values = new double[blocks.size()];
        double[] blockvalues = new double[BLOCKSIZE * BLOCKSIZE];

        // Fluctuate the polygon by 4 and 8 pixels in all directions
        // to make the exact placement of points less important.
        int[] adx = new int[] { 0, -4, 4, 0, 0, -8, 8, 0, 0 };
        int[] ady = new int[] { 0, 0, 0, -4, 4, 0, 0, -8, 8 };

        for (int r = 0; r < adx.length; r++) {
            for (int i = 0; i < Math.min(125, sequence.frameCount()); i++) {
                int idx = 0;

                for (int ind : blocks) {
                    int blockind = 0;
                    int x = (ind % 1000) + mc.dx[i] + adx[r], y = (ind / 1000) + mc.dy[i] + ady[r];

                    for (int dx = 0; dx < BLOCKSIZE; dx++)
                        for (int dy = 0; dy < BLOCKSIZE; dy++)
                            if (x + dx >= 0 && x + dx < 512 && y + dy >= 0 && y + dy < 512
                                    && medpixels[i][y + dy][x + dx] > 10) {
                                blockvalues[blockind] = (medpixels[i][y + dy][x + dx] - pixels[i][y
                                        + dy][x + dx])
                                        * 128.0 / medpixels[i][y + dy][x + dx];
                                blockind++;
                            }
                    if (blockind > BLOCKSIZE * BLOCKSIZE / 2) {
                        values[idx] = TrimmedMean.trimMean(blockvalues, 0, blockind, 0.75, 1);
                        idx++;
                    }
                }
                curveValues[i] += TrimmedMean.trimMean(values, 0, idx, 0.5, 1) / adx.length;
            }

        }

        CalculationResult res = new CalculationResult();
        res.curve = curveValues;

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        double peak = 0, down = 0;

        // The curve is now calculated. For the real qube value, walk
        // over the curve and keep track of the highest rise +
        // the highest decline after that
        for (int i = 1; i < Math.min(125, curveValues.length - 1); i++) {
            min = Math.min(min, curveValues[i]);
            max = Math.max(max, curveValues[i]);

            if (peak < curveValues[i] - min) {
                // highest peak reached, reset the decline
                down = 0;
            }
            peak = Math.max(curveValues[i] - min, peak);
            down = Math.max(down, max - curveValues[i]);
        }
        res.qubeValue = peak + down;

        return res;
    }
}
