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

import java.util.Arrays;

/**
 * This class contains methods to calculate a trimmed mean, ie. a mean value
 * over values with some of the outliers removed.
 * 
 * @author mathijs
 */
public class TrimmedMean {

    public static double trimMean(double[] values, double discardFraction) {
        return trimMean(values, discardFraction / 2, (1 - discardFraction / 2));
    }

    public static double trimMean(double[] values, int begin, int size, double discardFraction) {
        return trimMean(values, begin, size, discardFraction / 2, (1 - discardFraction / 2));
    }

    public static double trimMean(double[] values, double startFrac, double endFrac) {
        return trimMean(values, 0, values.length, startFrac, endFrac);
    }

    public static double trimMean(double[] values, int begin, int size, double startFrac,
            double endFrac) {
        Arrays.sort(values, begin, begin + size);
        int start = begin + (int) (size * startFrac);
        int end = begin + (int) (size * endFrac);

        if (end >= begin + size)
            end = begin + size - 1;
        if (begin >= end)
            begin = end;
        double sum = 0;
        for (int i = start; i <= end; i++)
            sum += values[i];
        return sum / (end - start + 1);
    }

    public static double discardOutliers(double[] values, double limit) {
        Arrays.sort(values);
        while (true) {
            double mean = 0;
            for (double d : values) {
                mean += d;
            }
            mean /= values.length;
            double sd = 0;
            for (double d : values)
                sd += (d - mean) * (d - mean);
            sd = Math.sqrt(sd / values.length);

            if (values[0] >= mean - limit * sd && values[values.length - 1] <= mean + limit * sd)
                return mean;

            int idx1 = Arrays.binarySearch(values, mean - limit * sd);
            int idx2 = Arrays.binarySearch(values, mean + limit * sd);
            if (idx1 < 0)
                idx1 = -(idx1 + 1);
            if (idx2 < 0)
                idx2 = -(idx2 + 1);
            else
                idx2++;
            values = Arrays.copyOfRange(values, idx1, idx2);
        }
    }
}
