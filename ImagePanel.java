import javax.swing.BorderFactory;
import java.awt.*;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;

public class ImagePanel extends JPanel {
    private Color[][] colorArray;
    private int dimensionX, dimensionY;
    int step;

    ImagePanel() {
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setPreferredSize(new Dimension(dimensionX, dimensionY));
    }

    public void SetSize(int w, int h) {
        dimensionX = w;
        dimensionY = h;
        setPreferredSize(new Dimension(dimensionX, dimensionY));
    }

    public void repaint(Color[][] input, int w, int h) {
        dimensionX = w;
        dimensionY = h;
        colorArray = input;
        setPreferredSize(new Dimension(dimensionX, dimensionY));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (colorArray != null) {
            for (int x = 0; x < dimensionX; x++) {
                for (int y = 0; y < dimensionY; y++) {
                    g.setColor(colorArray[x][y]);
                    g.drawLine(x, y, x, y);
                }
            }
        }
    }
}
