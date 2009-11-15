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
package nl.umcg.qube;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import nl.umcg.qube.io.DicomLoader;
import nl.umcg.qube.model.CAGSequence;
import nl.umcg.qube.model.SequenceIdentifier;
import nl.umcg.qube.process.BinaryMoveCalc;
import nl.umcg.qube.process.BlushCalculatorImpl;
import nl.umcg.qube.process.Caching;
import nl.umcg.qube.process.IBlushCalculator;
import nl.umcg.qube.process.IMoveCalculator;
import nl.umcg.qube.ui.BlushScorePanel;

/**
 * Main class for the QuBE (Quantitative Blush Evaluator) program.
 * 
 * @author Mathijs Vogelzang
 */
public class QubeMain extends JFrame {

    private JPanel mainPanel;

    private IBlushCalculator blushCalc = new BlushCalculatorImpl();
    private IMoveCalculator moveCalc = new Caching.CachingMoveCalculator(new BinaryMoveCalc());

    public QubeMain(String[] args) {
        // TODO make pluggable movie providers, etc. configurable via command
        // line?
        // AbstractSequenceProvider provider = new DicomSequenceProvider("")
        // For now, just open a JFileChooserDialog to directly import the DICOM
        // file.

        super("Quantitative Blush Evaluator (QuBE)");
        JPanel controlPanel = new JPanel();
        JButton openButton = new JButton("Open DICOM file");
        openButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JFileChooser chooser = new JFileChooser();
                if (chooser.showOpenDialog(QubeMain.this) == JFileChooser.APPROVE_OPTION) {
                    // Load a sequence from the chosen file.
                    SequenceIdentifier id = new SequenceIdentifier("", "", chooser
                            .getSelectedFile().getAbsolutePath());
                    try {
                        CAGSequence sequence = DicomLoader.loadFromFile(chooser.getSelectedFile(),
                                id);
                        BlushScorePanel scorePanel = new BlushScorePanel(moveCalc, blushCalc);
                        scorePanel.setSequence(sequence);
                        mainPanel.removeAll();
                        mainPanel.add(scorePanel);
                        mainPanel.validate();
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(QubeMain.this, "An error occurred: "
                                + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }

            }
        });
        controlPanel.add(openButton);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        mainPanel = new JPanel();
        add(controlPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        setSize(640, 480);
        setLocationRelativeTo(null);
        setVisible(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        new QubeMain(args);
    }

}
