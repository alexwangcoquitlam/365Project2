package src;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.*;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class WavePanel extends JPanel {
    private int[][] audioValues;
    private int channels;
    private int dimensionX = 1000, dimensionY = 128 * 3;

    WavePanel() {
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setPreferredSize(new Dimension(dimensionX, dimensionY));
    }

    public void repaint(int[][] data, int channels) {
        this.channels = channels;
        audioValues = data;
        repaint();
    }

    public void paintComponent(Graphics g) {
        g.drawLine(0, dimensionY / 2, dimensionX, dimensionY / 2);
        g.setColor(Color.blue);
        if (audioValues != null) {
            for (int i = 0; i < audioValues.length; i++) {
                double normalizationFactorX = (double) dimensionX / (double) audioValues[i].length;
                double normalizationFactorY = (double) dimensionY / (double) GetMax(audioValues[i]) / 2 / channels;
                int offset = i*dimensionY/channels;
                g.setColor(Color.black);
                g.drawLine(0, dimensionY / 2 / channels + offset, dimensionX, dimensionY / 2 /channels + offset);
                g.setColor(Color.blue);
                for (int j = 0; j < audioValues[i].length-1; j++) {
                    int x1 = (int) Math.ceil(j * normalizationFactorX);
                    int y1 = (int) Math.ceil(-1*audioValues[i][j] * normalizationFactorY + offset + dimensionY/2/channels);
                    int x2 = (int) Math.ceil((j + 1) * normalizationFactorX);
                    int y2 = (int) Math.ceil(-1*audioValues[i][j+1] * normalizationFactorY + offset + dimensionY/2/channels);
                    g.drawLine(x1, y1, x2, y2);
                }
            }
        }
    }

    private int GetMax(int[] input) {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < input.length; i++) {
            if (input[i] > max)
                max = input[i];
        }
        return max;
    }
}