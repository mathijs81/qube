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

import java.awt.Point;
import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * This class represents a drawn region on a CAG sequence
 * 
 * @author mathijs
 */
public class Polygon implements Serializable {
    private static final long serialVersionUID = 3442734889662651300L;
    private final Point[] points;

    public Polygon(Point[] points) {
        this.points = points;
    }

    public int getMinX() {
        int minx = Integer.MAX_VALUE;
        for (Point point : points)
            minx = Math.min(point.x, minx);
        return minx;
    }

    public int getMinY() {
        int miny = Integer.MAX_VALUE;
        for (Point point : points)
            miny = Math.min(point.y, miny);
        return miny;
    }

    public int getMaxY() {
        int maxy = Integer.MIN_VALUE;
        for (Point point : points)
            maxy = Math.max(point.y, maxy);
        return maxy;
    }

    public int getMaxX() {
        int maxx = Integer.MIN_VALUE;
        for (Point point : points)
            maxx = Math.max(point.x, maxx);
        return maxx;
    }

    public int pointCount() {
        return points.length;
    }

    public Point getPoint(int index) {
        return points[index];
    }

    public String storeString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(points.length);
        for (Point p : points) {
            buffer.append(String.format(" %d %d", p.x, p.y));
        }
        return buffer.toString();
    }

    public static Polygon fromString(String str) {
        StringTokenizer tok = new StringTokenizer(str);
        int cnt = Integer.parseInt(tok.nextToken());
        Point[] points = new Point[cnt];
        for (int i = 0; i < cnt; i++) {
            points[i] = new Point(Integer.parseInt(tok.nextToken()), Integer.parseInt(tok
                    .nextToken()));
        }
        return new Polygon(points);
    }

    @Override
    public boolean equals(Object arg0) {
        if (!(arg0 instanceof Polygon))
            return false;
        return ((Polygon) arg0).storeString().equals(storeString());
    }

    @Override
    public int hashCode() {
        return storeString().hashCode();
    }
}
