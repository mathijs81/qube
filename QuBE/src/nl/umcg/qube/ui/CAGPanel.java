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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nl.umcg.qube.model.CAGFrame;
import nl.umcg.qube.model.CAGSequence;
import nl.umcg.qube.model.MoveCorrection;
import nl.umcg.qube.model.SequenceIdentifier;
import nl.umcg.qube.process.IMoveCalculator;

/**
 * @author mathijs
 * 
 */
public class CAGPanel extends JPanel {

    public interface IExtraRenderPlugin {
        public void renderFrame(CAGSequence sequence, int frame, BufferedImage image);
    }

    public interface IRenderPlugin {
        void renderFrame(CAGSequence sequence, int frame, int dx, int dy, BufferedImage image);

        String getName();
    }

    private static final long serialVersionUID = 4342415747954504809L;

    private static final int DEFAULT_TIMER_DELAY = 80;

    private static int CONTROL_PANEL_HEIGHT = 30;

    protected CAGSequence sequence;
    protected int currentFrame;
    protected BufferedImage image;
    protected Timer sequenceTimer;

    protected JScrollBar scrollBar;
    protected ArrayList<ChangeListener> changeListeners;
    protected ArrayList<IExtraRenderPlugin> extraRenderPlugins;

    protected JButton playButton;
    protected boolean correctMove;
    protected JButton moveButton;
    protected boolean showInformation;

    protected ArrayList<IRenderPlugin> plugins;
    protected JComboBox renderChoice;
    protected DefaultComboBoxModel renderChoices;

    protected IMoveCalculator moveCalculator;

    protected void fireChange() {
        for (ChangeListener cl : changeListeners)
            cl.stateChanged(new ChangeEvent(this));
    }

    public CAGPanel(IMoveCalculator moveCalculator) {
        plugins = new ArrayList<IRenderPlugin>();
        extraRenderPlugins = new ArrayList<IExtraRenderPlugin>();
        this.moveCalculator = moveCalculator;
        changeListeners = new ArrayList<ChangeListener>();

        playButton = new JButton(">");
        playButton.setMargin(new Insets(0, 0, 0, 0));
        playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (sequenceTimer.isRunning())
                    stopSequence();
                else
                    startSequence();
            }
        });
        add(playButton);

        moveButton = new JButton("M");
        moveButton.setMargin(new Insets(0, 0, 0, 0));
        moveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                correctMove = !correctMove;
                repaint();
            }
        });
        add(moveButton);

        correctMove = true;

        sequenceTimer = new Timer(DEFAULT_TIMER_DELAY, new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (sequence != null) {
                    currentFrame++;
                    currentFrame %= sequence.frameCount();
                    scrollBar.setValue(currentFrame);
                    fireChange();
                    repaint();
                }
            }
        });

        scrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
        scrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent arg0) {
                if (arg0.getValue() != currentFrame) {
                    currentFrame = arg0.getValue();
                    fireChange();
                    repaint();
                }

            }
        });
        add(scrollBar);

        renderChoices = new DefaultComboBoxModel();
        renderChoices.addElement("Default");
        renderChoice = new JComboBox(renderChoices);
        renderChoice.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                repaint();
            }
        });
        add(renderChoice);

        showInformation = true;
    }

    MoveCorrection tempMi;
    SequenceIdentifier idid;

    @Override
    public void paintComponent(Graphics g) {
        if (sequence == null) {
            g.drawString("No sequence loaded.", 5, 15);
            return;
        }

        Graphics2D g2 = (Graphics2D) image.getGraphics();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, image.getWidth(), image.getHeight());
        CAGFrame frame = sequence.getFrame(currentFrame);

        MoveCorrection mi = null;
        if (correctMove) {
            mi = moveCalculator.getMoveCorrection(sequence);
        }

        if (renderChoice.getSelectedIndex() == 0) {
            short[][] pixelValues = frame.getValues();

            int width = sequence.getWidth(), height = sequence.getHeight();

            for (int x = 0; x < width; x++)
                for (int y = 0; y < height; y++) {
                    if (correctMove) {
                        int nx = x - mi.dx[currentFrame], ny = y - mi.dy[currentFrame];
                        if (nx < 0 || ny < 0 || nx >= width || ny >= height)
                            continue;
                    }

                    int col = 0xFF000000 + (pixelValues[y][x] * (int) 0x010101);

                    if (correctMove)
                        image.setRGB(x - mi.dx[currentFrame], y - mi.dy[currentFrame], col);
                    else
                        image.setRGB(x, y, col);
                }
        } else {
            if (correctMove)
                plugins.get(renderChoice.getSelectedIndex() - 1).renderFrame(sequence,
                        currentFrame, mi.dx[currentFrame], mi.dy[currentFrame], image);
            else
                plugins.get(renderChoice.getSelectedIndex() - 1).renderFrame(sequence,
                        currentFrame, 0, 0, image);
        }

        for (IExtraRenderPlugin plugin : extraRenderPlugins)
            plugin.renderFrame(sequence, currentFrame, image);

        g.drawImage(image, 0, 0, null);

        g.setColor(Color.GRAY);
        g.fillRect(0, getHeight() - CONTROL_PANEL_HEIGHT, getWidth(), CONTROL_PANEL_HEIGHT);

        g.setColor(Color.BLACK);
        g.drawString(String.format("%d/%d", currentFrame + 1, sequence.frameCount()),
                getWidth() / 2 - 40, getHeight() - 1);

        if (showInformation) {
            g.setColor(Color.WHITE);
            g.drawString(sequence.getPatientName(), 0, 10);
            g.drawString(sequence.getStudyID(), 0, 20);
        }
    }

    public void startSequence() {
        sequenceTimer.start();
        playButton.setText("||");
    }

    public void stopSequence() {
        sequenceTimer.stop();
        playButton.setText(">");
    }

    public Dimension getPreferredSize() {
        if (sequence == null)
            return new Dimension(512, 512 + CONTROL_PANEL_HEIGHT);

        return new Dimension(sequence.getWidth(), sequence.getHeight() + CONTROL_PANEL_HEIGHT);
    }

    public void setSequence(CAGSequence sequence) {
        this.sequence = sequence;
        currentFrame = 0;
        scrollBar.setEnabled(sequence != null);
        scrollBar.setBlockIncrement(1);
        scrollBar.setValue(0);
        scrollBar.setMinimum(0);
        if (sequence != null) {
            scrollBar.setMaximum(sequence.frameCount());
            image = new BufferedImage(sequence.getWidth(), sequence.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
        }
        scrollBar.getModel().setExtent(1);

        fireChange();
        repaint();
    }

    public CAGSequence getSequence() {
        return sequence;
    }

    @Override
    public void doLayout() {
        scrollBar.setBounds(new Rectangle(getWidth() / 2, getHeight() - CONTROL_PANEL_HEIGHT,
                getWidth() / 2, CONTROL_PANEL_HEIGHT));
        playButton.setBounds(new Rectangle(0, getHeight() - CONTROL_PANEL_HEIGHT,
                CONTROL_PANEL_HEIGHT, CONTROL_PANEL_HEIGHT));
        moveButton.setBounds(new Rectangle(CONTROL_PANEL_HEIGHT * 2, getHeight()
                - CONTROL_PANEL_HEIGHT, CONTROL_PANEL_HEIGHT, CONTROL_PANEL_HEIGHT));

        renderChoice.setBounds(new Rectangle(CONTROL_PANEL_HEIGHT * 3, getHeight()
                - CONTROL_PANEL_HEIGHT, 4 * CONTROL_PANEL_HEIGHT, CONTROL_PANEL_HEIGHT));
    }

    public void addChangeListener(ChangeListener cl) {
        changeListeners.add(cl);
    }

    public void removeChangeListener(ChangeListener cl) {
        changeListeners.remove(cl);
    }

    public void addExtraRenderPlugin(IExtraRenderPlugin plugin) {
        extraRenderPlugins.add(plugin);
        repaint();
    }

    public void addRenderPlugin(IRenderPlugin plugin) {
        plugins.add(plugin);
        renderChoices.addElement(plugin.getName());
    }

    public void removeExtraRenderPlugin(IExtraRenderPlugin plugin) {
        extraRenderPlugins.remove(plugin);
        repaint();
    }

    public void setShowInformation(boolean show) {
        showInformation = show;
    }

    public int getCurrentFrameNo() {
        return currentFrame;
    }

    public void setCurrentFrameNo(int frame) {
        currentFrame = frame;
        repaint();
    }

    public boolean sequenceRunning() {
        return sequenceTimer.isRunning();
    }

    public boolean isMoveCorrectionEnabled() {
        return correctMove;
    }

    public void setMoveCorrectionEnabled(boolean corrMove) {
        correctMove = corrMove;
        repaint();
    }
}