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

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nl.umcg.qube.model.CAGSequence;
import nl.umcg.qube.model.Polygon;
import nl.umcg.qube.process.IBlushCalculator;
import nl.umcg.qube.process.IMoveCalculator;

/**
 * @author mathijs
 * 
 */
public class BlushScorePanel extends JPanel {
    private static final long serialVersionUID = 9074523012696916756L;

    protected CAGPanel cagPanel;
    protected StatisticsPanel calcPanel;

    protected PolygonEdit editor;

    public BlushScorePanel(IMoveCalculator moveCalculator, IBlushCalculator blushCalculator) {
        super(new BorderLayout());
        cagPanel = new CAGPanel(moveCalculator);

        cagPanel.setShowInformation(true);
        add(cagPanel);
        calcPanel = new StatisticsPanel(cagPanel);
        calcPanel.setBlushCalculator(blushCalculator);
        add(calcPanel, BorderLayout.EAST);

        editor = new PolygonEdit();
        editor.attachPanel(cagPanel);
        editor.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                Polygon p = ((PolygonEdit) e.getSource()).getPolygon();
                calcPanel.setPolygon(p);
            }
        });
    }

    public void setSequence(CAGSequence sequence) {
        setPolygon(null);
        cagPanel.setSequence(sequence);
        cagPanel.setMoveCorrectionEnabled(true);
    }

    public CAGSequence getSequence() {
        return cagPanel.getSequence();
    }

    public void setPolygon(Polygon p) {
        editor.setPolygon(p);
    }

    public Polygon getPolygon() {
        return editor.getPolygon();
    }

    public void addChangeListener(ChangeListener cl) {
        cagPanel.addChangeListener(cl);
    }

    public void removeChangeListener(ChangeListener cl) {
        cagPanel.removeChangeListener(cl);
    }

    public int getSelectedFrame() {
        return cagPanel.getCurrentFrameNo();
    }

    public void setShowInformation(boolean showInformation) {
        cagPanel.setShowInformation(showInformation);
    }
}