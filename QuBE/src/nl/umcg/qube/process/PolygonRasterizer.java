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
import java.util.ArrayList;
import java.util.Collections;

import nl.umcg.qube.model.Polygon;

/**
 * @author mathijs
 * 
 */
public class PolygonRasterizer {
    /**
     * Rasterize an arbitrary polygon into points
     * 
     * @param poly
     *            The polygon to be processed
     * @return A list of all points in the polygon
     */
    public static ArrayList<Point> rasterize(Polygon poly) {
        // The current algorithm scans for all y-values in the polygon
        // for all horizontal y-lines it then determines where each line
        // of the polygon intersects this horizontal line
        // When we then scan the list of intersections from left to right,
        // we are in the polygon when we have seen an odd number of
        // intersections and out of the poly when we've seen an even number.
        // care must be taken with the ending points of lines, horizontal lines,
        // etc.

        // TODO alternative algorithm: using
        // a scanning approach and active edge list
        ArrayList<Point> result = new ArrayList<Point>();

        int miny = poly.getMinY();
        int maxy = poly.getMaxY();

        for (int y = miny; y <= maxy; y++) {
            ArrayList<Double> xCoords = new ArrayList<Double>();

            for (int idx = 0; idx < poly.pointCount(); idx++) {
                Point p1 = poly.getPoint(idx), p2 = poly.getPoint((idx + 1) % poly.pointCount());
                // skip horizontal lines
                if (p1.y == p2.y)
                    continue;
                // p2 should be lower point
                if (p1.y > p2.y) {
                    Point temp = p1;
                    p1 = p2;
                    p2 = temp;
                }

                if (y < p1.y || y >= p2.y)
                    continue; // y not within this line

                double x = p1.x + (p2.x - p1.x) * (y - (double) p1.y) / (p2.y - p1.y);
                xCoords.add(x);
            }

            Collections.sort(xCoords);

            if (xCoords.size() % 2 != 0)
                throw new IllegalArgumentException("Degenerate polygon?");

            if (xCoords.size() != 0) {
                int x = (int) Math.round(xCoords.get(0));
                boolean draw = true;
                int idx = 1;
                while (idx < xCoords.size()) {
                    while (idx < xCoords.size() && x > Math.round(xCoords.get(idx))) {
                        draw = !draw;
                        idx++;
                    }
                    if (draw) {
                        result.add(new Point(x, y));
                    }
                    x++;
                }
            }
        }

        return result;
    }
}
