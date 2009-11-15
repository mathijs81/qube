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

import nl.umcg.qube.model.CAGSequence;
import nl.umcg.qube.model.MoveCorrection;

/**
 * Correct for movement in a CAGSequence by calculating the highest correlation
 * between subsequent frames for different values of shifting in the x and y
 * direction. To prevent longer-term drift, the current frame is also compared
 * to older frames. For efficiency, the number of older frames to compare to is
 * O(log(i)) where i is the frame number (hence the name BinaryMoveCalc).
 */
public class BinaryMoveCalc implements IMoveCalculator {

    @Override
    public MoveCorrection getMoveCorrection(CAGSequence sequence) {
        int[] dx = new int[sequence.frameCount()], dy = new int[sequence.frameCount()];

        for (int i = 1; i < dx.length; i++) {
            long bestScore = Long.MAX_VALUE;
            int bx = 0, by = 0;
            short[][] pixels = sequence.getFrame(i).getValues();

            // Determine the number of previous frames to compare to.
            int offset = 1;
            int cnt = 0;
            while (i >= offset && offset <= 32) {
                cnt++;
                offset *= 2;
            }

            // Fetch pixel values of previous frames.
            short[][][] prev_pixels = new short[cnt][][];
            offset = 1;
            for (int k = 0; k < cnt; k++) {
                prev_pixels[k] = sequence.getFrame(i - offset).getValues();
                offset *= 2;
            }

            // Test for each difference in x and y between -10 and 10.
            for (int tx = -10; tx <= 10; tx++)
                for (int ty = -10; ty <= 10; ty++) {

                    short[][] pp;
                    offset = 1;
                    long score = 0;
                    for (int k = 0; k < cnt; k++) {
                        int tx2 = tx + dx[i - offset] - dx[i - 1];
                        int ty2 = ty + dy[i - offset] - dy[i - 1];
                        pp = prev_pixels[k];

                        // Only check the middle part of the frame and take
                        // steps of 4 pixels at a time for performance.
                        for (int y = 128; y < 384; y += 4)
                            for (int x = 128; x < 384; x += 4) {

                                int scInc = (pixels[y][x] - pp[y + ty2][x + tx2])
                                        * (pixels[y][x] - pp[y + ty2][x + tx2]);
                                // Cap the maximum squared difference to
                                // mitigate the effect of outliers somewhat.
                                if (scInc > 100)
                                    scInc = 100;
                                score += scInc;
                            }
                        offset *= 2;
                    }

                    // Store the tx, ty with the lowest squared difference.
                    if (score < bestScore) {
                        bestScore = score;
                        bx = tx;
                        by = ty;
                    }
                }

            dx[i] = dx[i - 1] - bx;
            dy[i] = dy[i - 1] - by;
        }

        int adjx = dx[sequence.frameCount() / 2], adjy = dy[sequence.frameCount() / 2];
        for (int i = 0; i < dx.length; i++) {
            dx[i] -= adjx;
            dy[i] -= adjy;
        }

        return new MoveCorrection(dx, dy);
    }
}
