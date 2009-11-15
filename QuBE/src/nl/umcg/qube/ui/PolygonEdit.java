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
package nl.umcg.qube.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nl.umcg.qube.model.CAGSequence;
import nl.umcg.qube.model.Polygon;
import nl.umcg.qube.ui.CAGPanel.IExtraRenderPlugin;

/**
 * PolygonEdit is an overlay over the CAGPanel that shows and allows editing of
 * a polygon.
 * 
 * @author mathijs
 * 
 */
public class PolygonEdit extends MouseAdapter implements IExtraRenderPlugin {
  private static final int DIST_SQ_LIMIT = 50;

  protected CAGPanel panel;
  protected int draggingPoint = -1;
  protected Polygon polygon;

  protected ArrayList<ChangeListener> changeListeners = new ArrayList<ChangeListener>();

  protected void fireChange() {
    ChangeEvent ev = new ChangeEvent(this);
    for (ChangeListener cl : changeListeners)
      cl.stateChanged(ev);

    if (panel != null)
      panel.repaint();
  }

  public void attachPanel(CAGPanel panel) {
    this.panel = panel;
    panel.addMouseListener(this);
    panel.addMouseMotionListener(this);
    panel.addExtraRenderPlugin(this);
  }

  protected void checkBounds(Point p) {
    CAGSequence sequence = panel.getSequence();
    if (sequence != null) {
      if (p.x < 10)
        p.x = 10;
      if (p.x > sequence.getWidth() - 10)
        p.x = sequence.getWidth() - 10;
      if (p.y < 10)
        p.y = 10;
      if (p.y > sequence.getHeight() - 10)
        p.y = sequence.getHeight() - 10;
    }
  }

  @Override
  public void mouseDragged(MouseEvent arg0) {
    Point p = arg0.getPoint();
    checkBounds(p);
    if (draggingPoint != -1) {
      if (!p.equals(polygon.getPoint(draggingPoint))) {
        int dx = p.x - polygon.getPoint(draggingPoint).x;
        int dy = p.y - polygon.getPoint(draggingPoint).y;

        Point[] pointArray = new Point[polygon.pointCount()];

        if ((arg0.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0) {
          for (int i = 0; i < pointArray.length; i++)
            pointArray[i] = new Point(polygon.getPoint(i).x + dx, polygon
                .getPoint(i).y
                + dy);
        } else {
          for (int i = 0; i < pointArray.length; i++)
            pointArray[i] = polygon.getPoint(i);
          pointArray[draggingPoint] = p;
        }
        polygon = new Polygon(pointArray);
        fireChange();
      }
      panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
  }

  @Override
  public void mouseMoved(MouseEvent arg0) {
    Point p = arg0.getPoint();

    boolean onPoint = false;
    if (polygon != null) {
      for (int i = 0; i < polygon.pointCount(); i++) {
        if (polygon.getPoint(i).distanceSq(p) < DIST_SQ_LIMIT)
          onPoint = true;
      }
    }
    panel.setCursor(Cursor.getPredefinedCursor(onPoint ? Cursor.HAND_CURSOR
        : Cursor.DEFAULT_CURSOR));
  }

  @Override
  public void mousePressed(MouseEvent arg0) {

    if ((arg0.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
      if (polygon != null) {
        Point p = arg0.getPoint();
        for (int i = 0; i < polygon.pointCount(); i++)
          if (polygon.getPoint(i).distanceSq(p) < DIST_SQ_LIMIT)
            draggingPoint = i;
      }
    }

    super.mousePressed(arg0);
  }

  @Override
  public void mouseReleased(MouseEvent arg0) {

    if (arg0.getButton() == MouseEvent.BUTTON1) {

      if (draggingPoint != -1) {
        if ((arg0.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0) {
          Point[] pointArray = new Point[polygon.pointCount() - 1];
          for (int i = 0; i <= pointArray.length; i++)
            if (i < draggingPoint)
              pointArray[i] = polygon.getPoint(i);
            else if (i > draggingPoint)
              pointArray[i - 1] = polygon.getPoint(i);
          polygon = new Polygon(pointArray);
          fireChange();
        }

        draggingPoint = -1;
        return;
      }

      Point p = arg0.getPoint();
      checkBounds(p);
      if (polygon == null)
        polygon = new Polygon(new Point[] { p });
      else {
        Point[] pointArray = new Point[polygon.pointCount() + 1];
        for (int i = 0; i < polygon.pointCount(); i++)
          pointArray[i] = polygon.getPoint(i);
        pointArray[pointArray.length - 1] = p;
        polygon = new Polygon(pointArray);
      }
      fireChange();
    }
  }

  @Override
  public void renderFrame(CAGSequence sequence, int frame, BufferedImage image) {
    Graphics2D g = image.createGraphics();
    g.setPaint(Color.LIGHT_GRAY);
    if (polygon != null) {
      for (int i = 0; i < polygon.pointCount(); i++) {
        Point p = polygon.getPoint(i);
        Point p2 = polygon.getPoint((i + 1) % polygon.pointCount());
        g.drawLine(p.x, p.y, p2.x, p2.y);
      }
      g.setPaint(Color.RED);
      for (int i = 0; i < polygon.pointCount(); i++) {
        Point p = polygon.getPoint(i);
        g.draw(new Ellipse2D.Float(p.x - 2, p.y - 2, 4, 4));
      }
    }
  }

  public void setPolygon(Polygon poly) {
    polygon = poly;
    draggingPoint = -1;
    fireChange();
  }

  public Polygon getPolygon() {
    return polygon;
  }

  public void addChangeListener(ChangeListener cl) {
    changeListeners.add(cl);
  }

  public void removeChangeListener(ChangeListener cl) {
    changeListeners.remove(cl);
  }
}
