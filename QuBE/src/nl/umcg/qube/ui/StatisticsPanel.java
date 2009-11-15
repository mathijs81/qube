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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nl.umcg.qube.model.CAGSequence;
import nl.umcg.qube.model.MoveCorrection;
import nl.umcg.qube.model.Polygon;
import nl.umcg.qube.process.IBlushCalculator;
import nl.umcg.qube.process.IBlushCalculator.CalculationResult;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.xy.DefaultXYDataset;

/**
 * TODO because this class was made later, it depends a lot on CAGPanel, common
 * stuff should be pulled up instead of this class getting it all from cagpanel.
 * 
 * @author mathijs
 * 
 */
public class StatisticsPanel extends JPanel {

    private static final long serialVersionUID = 5398400651129966151L;
    protected CAGPanel cagPanel;

    protected ChartPanel chartPanel;
    protected JFreeChart chart;
    protected DefaultXYDataset dataSet;
    protected JLabel blushLabel;

    protected Polygon polygon;

    protected IBlushCalculator blushCalculator;

    public void recalc() {
        if (polygon != null && cagPanel.getSequence() != null) {
            CAGSequence sequence = cagPanel.getSequence();
            MoveCorrection mi = cagPanel.moveCalculator.getMoveCorrection(sequence);

            while (dataSet.getSeriesCount() > 0)
                dataSet.removeSeries(dataSet.getSeriesKey(0));

            double[] idx = new double[sequence.frameCount() - 2];
            double[] val = new double[sequence.frameCount() - 2];
            CalculationResult cr = blushCalculator.calculate(sequence, polygon, mi);

            for (int i = 1; i <= idx.length; i++) {
                idx[i - 1] = i + 1;
                val[i - 1] = cr.curve[i];
            }
            dataSet.addSeries("Blush curve", new double[][] { idx, val });

            blushLabel.setText(String.format("QuBE value: %.1f", cr.qubeValue));

            chartPanel.repaint();
        }
    }

    public StatisticsPanel(CAGPanel cag_panel) {
        super(new BorderLayout());
        cagPanel = cag_panel;
        cagPanel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
                // redrawing the chart during sequence viewing is very slow
                // don't update it therefore.
                if (chart != null && !cagPanel.sequenceRunning()) {
                    chart.getXYPlot().clearDomainMarkers();
                    chart.getXYPlot().addDomainMarker(
                            new ValueMarker(cagPanel.getCurrentFrameNo() + 1));
                }
            }
        });

        JPanel commandPanel = new JPanel();

        JButton calc = new JButton("Recalculate");
        calc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                recalc();
            }
        });
        commandPanel.add(calc);

        blushLabel = new JLabel("");
        commandPanel.add(blushLabel);

        add(commandPanel, BorderLayout.NORTH);

        dataSet = new DefaultXYDataset();
        chart = ChartFactory.createXYLineChart("Blush value", "Frame no.", "arbitrary units",
                dataSet, PlotOrientation.VERTICAL, true, false, false);
        chartPanel = new ChartPanel(chart);
        add(chartPanel);
    }

    public void setBlushCalculator(IBlushCalculator calculator) {
        this.blushCalculator = calculator;
    }

    public void resetSequenceOrPolygon() {
        recalc();
    }

    public void setPolygon(Polygon poly) {
        this.polygon = poly;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(500, 500);
    }

}
