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
 * This class provides methods to calculate a medianfilter over an image
 * (represented by an 2D array of ints)
 * 
 * @author mathijs
 */
public class MedianFilter {

    /**
     * Calculate median filter. This uses the straightforward, but slow method
     * of constructing the list of pixels for each destination pixels, doing a
     * sort, and taking the median pixel.
     * 
     * @param src
     *            The source image
     * @param siz
     *            The window size (must be odd)
     * @return A new median filtered image
     */
    public static int[][] filter(int[][] src, int siz) {
        if (siz % 2 != 1)
            throw new IllegalArgumentException("siz must be odd");
        // slow method
        int Y = src.length, X = src[0].length;
        int[][] dest = new int[Y][X];

        int[] tab = new int[siz * siz];

        for (int y = siz / 2; y < Y - siz / 2; y++)
            for (int x = siz / 2; x < X - siz / 2; x++) {
                int idx = 0;
                for (int dy = -siz / 2; dy <= siz / 2; dy++)
                    for (int dx = -siz / 2; dx <= siz / 2; dx++) {
                        tab[idx++] = src[y + dy][x + dx];
                    }

                Arrays.sort(tab);
                dest[y][x] = tab[tab.length / 2];
            }

        return dest;
    }

    /**
     * Calculate median filter. This uses a method using a sliding window, and
     * keeps track of the histogram of pixel values at each time. This is
     * faster, as only one row of pixels has to be added and subtracted from the
     * histogram at each step.
     * 
     * @param src
     *            The source image
     * @param siz
     *            The window size (must be odd)
     * @return A new median filtered image
     */
    public static int[][] fastfilter(int[][] src, int siz) {
        if (siz % 2 != 1)
            throw new IllegalArgumentException("siz must be odd");

        int Y = src.length, X = src[0].length;
        int[][] dest = new int[Y][X];

        int[] cnt = new int[256];
        for (int y = 0; y < siz; y++)
            for (int x = 0; x < siz; x++)
                cnt[src[y][x]]++;

        boolean down = true;
        int cur = 0;
        int sum = 0;
        int targ = (siz * siz) / 2 + 1;

        for (int x = siz / 2; x < X - siz / 2; x++) {
            int y = siz / 2;
            if (!down)
                y = Y - siz / 2 - 1;

            for (int n = 0; n < Y - siz + 1; n++) {
                while (sum >= targ || sum + cnt[cur] < targ) {
                    if (sum < targ) {
                        sum += cnt[cur];
                        cur++;
                    } else {
                        cur--;
                        sum -= cnt[cur];
                    }
                }

                dest[y][x] = cur;

                if (n == Y - siz) {
                    if (x != X - siz / 2 - 1) {
                        for (int pos = y - siz / 2; pos <= y + siz / 2; pos++) {
                            int rem = src[pos][x - siz / 2];
                            int add = src[pos][x + siz / 2 + 1];
                            cnt[add]++;
                            cnt[rem]--;
                            if (add < cur)
                                sum++;
                            if (rem < cur)
                                sum--;
                        }
                    }
                } else {
                    int addrow, remrow;
                    if (down) {
                        y++;
                        addrow = y + siz / 2;
                        remrow = y - siz / 2 - 1;
                    } else {
                        y--;
                        addrow = y - siz / 2;
                        remrow = y + siz / 2 + 1;
                    }

                    for (int pos = x - siz / 2; pos <= x + siz / 2; pos++) {
                        int rem = src[remrow][pos], add = src[addrow][pos];
                        cnt[add]++;
                        cnt[rem]--;
                        if (add < cur)
                            sum++;
                        if (rem < cur)
                            sum--;
                    }
                }
            }

            down = !down;
        }

        return dest;
    }

    public static short[][] fastBoundaryfilter(short[][] src, final int siz) {
        if (siz % 2 != 1)
            throw new IllegalArgumentException("siz must be odd");
        int Y = src.length, X = src[0].length;
        short[][] dest = new short[Y][X];

        int[] cnt = new int[256];
        for (int y = 0; y < siz; y++) {
            cnt[src[y][0]]++;
            cnt[src[y][siz - 1]]++;
        }
        for (int x = 1; x < siz - 1; x++) {
            cnt[src[0][x]]++;
            cnt[src[siz - 1][x]]++;
        }

        boolean down = true;
        short cur = 0;
        int sum = 0;
        final int targ = (siz - 1) * 4 / 2;

        for (int x = siz / 2; x < X - siz / 2; x++) {
            int y = siz / 2;
            if (!down)
                y = Y - siz / 2 - 1;

            for (int n = 0; n < Y - siz + 1; n++) {
                while (sum >= targ || sum + cnt[cur] < targ) {
                    if (sum < targ) {
                        sum += cnt[cur];
                        cur++;
                    } else {
                        cur--;
                        sum -= cnt[cur];
                    }
                }

                dest[y][x] = cur;

                if (n == Y - siz) {
                    if (x != X - siz / 2 - 1) {
                        for (int pos = y - siz / 2; pos <= y + siz / 2; pos++) {
                            int rem = src[pos][x - siz / 2];
                            int add = src[pos][x + siz / 2 + 1];
                            cnt[add]++;
                            cnt[rem]--;
                            if (add < cur)
                                sum++;
                            if (rem < cur)
                                sum--;
                            if (pos != y - siz / 2 && pos != y + siz / 2) {
                                rem = src[pos][x + siz / 2];
                                add = src[pos][x - siz / 2 + 1];
                                cnt[add]++;
                                cnt[rem]--;
                                if (add < cur)
                                    sum++;
                                if (rem < cur)
                                    sum--;
                            }
                        }
                    }
                } else {
                    int addrow, remrow, addrow2, remrow2;
                    if (down) {
                        y++;
                        addrow = y + siz / 2;
                        remrow2 = addrow - 1;
                        remrow = y - siz / 2 - 1;
                        addrow2 = remrow + 1;
                    } else {
                        y--;
                        addrow = y - siz / 2;
                        remrow2 = addrow + 1;
                        remrow = y + siz / 2 + 1;
                        addrow2 = remrow - 1;
                    }

                    for (int pos = x - siz / 2; pos <= x + siz / 2; pos++) {
                        int rem = src[remrow][pos], add = src[addrow][pos];
                        cnt[add]++;
                        cnt[rem]--;
                        if (add < cur)
                            sum++;
                        if (rem < cur)
                            sum--;
                        if (pos != x - siz / 2 && pos != x + siz / 2) {
                            rem = src[remrow2][pos];
                            add = src[addrow2][pos];
                            cnt[add]++;
                            cnt[rem]--;
                            if (add < cur)
                                sum++;
                            if (rem < cur)
                                sum--;
                        }
                    }
                }
            }

            down = !down;
        }

        return dest;
    }

    public static int[][] fastfilter(int[][] src, int siz, int percentile) {
        if (siz % 2 != 1)
            throw new IllegalArgumentException("siz must be odd");
        int Y = src.length, X = src[0].length;
        int[][] dest = new int[Y][X];

        int[] cnt = new int[256];
        for (int y = 0; y < siz; y++)
            for (int x = 0; x < siz; x++)
                cnt[src[y][x]]++;

        boolean down = true;
        int cur = 0;
        int sum = 0;
        int targ = (siz * siz * percentile) / 100 + 1;

        for (int x = siz / 2; x < X - siz / 2; x++) {
            int y = siz / 2;
            if (!down)
                y = Y - siz / 2 - 1;

            for (int n = 0; n < Y - siz + 1; n++) {
                while (sum >= targ || sum + cnt[cur] < targ) {
                    if (sum < targ) {
                        sum += cnt[cur];
                        cur++;
                    } else {
                        cur--;
                        sum -= cnt[cur];
                    }
                }

                dest[y][x] = cur;

                if (n == Y - siz) {
                    if (x != X - siz / 2 - 1) {
                        for (int pos = y - siz / 2; pos <= y + siz / 2; pos++) {
                            int rem = src[pos][x - siz / 2];
                            int add = src[pos][x + siz / 2 + 1];
                            cnt[add]++;
                            cnt[rem]--;
                            if (add < cur)
                                sum++;
                            if (rem < cur)
                                sum--;
                        }
                    }
                } else {
                    int addrow, remrow;
                    if (down) {
                        y++;
                        addrow = y + siz / 2;
                        remrow = y - siz / 2 - 1;
                    } else {
                        y--;
                        addrow = y - siz / 2;
                        remrow = y + siz / 2 + 1;
                    }

                    for (int pos = x - siz / 2; pos <= x + siz / 2; pos++) {
                        int rem = src[remrow][pos], add = src[addrow][pos];
                        cnt[add]++;
                        cnt[rem]--;
                        if (add < cur)
                            sum++;
                        if (rem < cur)
                            sum--;
                    }
                }
            }

            down = !down;
        }

        return dest;
    }

    public static int[][] fastBoundaryfilter(int[][] src, int siz, int percentile) {
        if (siz % 2 != 1)
            throw new IllegalArgumentException("siz must be odd");
        // stupid slow method
        int Y = src.length, X = src[0].length;
        int[][] dest = new int[Y][X];

        int[] cnt = new int[256];
        for (int y = 0; y < siz; y++) {
            cnt[src[y][0]]++;
            cnt[src[y][siz - 1]]++;
        }
        for (int x = 1; x < siz - 1; x++) {
            cnt[src[0][x]]++;
            cnt[src[siz - 1][x]]++;
        }

        boolean down = true;
        int cur = 0;
        int sum = 0;
        int targ = ((siz - 1) * 4 * percentile) / 100;

        for (int x = siz / 2; x < X - siz / 2; x++) {
            int y = siz / 2;
            if (!down)
                y = Y - siz / 2 - 1;

            for (int n = 0; n < Y - siz + 1; n++) {
                while (sum >= targ || sum + cnt[cur] < targ) {
                    if (sum < targ) {
                        sum += cnt[cur];
                        cur++;
                    } else {
                        cur--;
                        sum -= cnt[cur];
                    }
                }

                dest[y][x] = cur;

                if (n == Y - siz) {
                    if (x != X - siz / 2 - 1) {
                        for (int pos = y - siz / 2; pos <= y + siz / 2; pos++) {
                            int rem = src[pos][x - siz / 2];
                            int add = src[pos][x + siz / 2 + 1];
                            cnt[add]++;
                            cnt[rem]--;
                            if (add < cur)
                                sum++;
                            if (rem < cur)
                                sum--;
                            if (pos != y - siz / 2 && pos != y + siz / 2) {
                                rem = src[pos][x + siz / 2];
                                add = src[pos][x - siz / 2 + 1];
                                cnt[add]++;
                                cnt[rem]--;
                                if (add < cur)
                                    sum++;
                                if (rem < cur)
                                    sum--;
                            }
                        }
                    }
                } else {
                    int addrow, remrow, addrow2, remrow2;
                    if (down) {
                        y++;
                        addrow = y + siz / 2;
                        remrow2 = addrow - 1;
                        remrow = y - siz / 2 - 1;
                        addrow2 = remrow + 1;
                    } else {
                        y--;
                        addrow = y - siz / 2;
                        remrow2 = addrow + 1;
                        remrow = y + siz / 2 + 1;
                        addrow2 = remrow - 1;
                    }

                    for (int pos = x - siz / 2; pos <= x + siz / 2; pos++) {
                        int rem = src[remrow][pos], add = src[addrow][pos];
                        cnt[add]++;
                        cnt[rem]--;
                        if (add < cur)
                            sum++;
                        if (rem < cur)
                            sum--;
                        if (pos != x - siz / 2 && pos != x + siz / 2) {
                            rem = src[remrow2][pos];
                            add = src[addrow2][pos];
                            cnt[add]++;
                            cnt[rem]--;
                            if (add < cur)
                                sum++;
                            if (rem < cur)
                                sum--;
                        }
                    }
                }
            }

            down = !down;
        }

        return dest;
    }
}
