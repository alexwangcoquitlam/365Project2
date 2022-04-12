package src;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class Project1Frame extends JFrame implements ActionListener {

    // Combined
    private JPanel panel, controlPanel;
    private JButton fileButton;
    private JLabel fileLabel;

    // Audio
    private JLabel totalSamplesLabel, samplingRateLabel;
    private WavePanel wavePanel;
    private JPanel infoPanel;

    // Image
    private JButton exitButton, nextButton;
    private JPanel imagesPanel;
    private ImagePanel imgPanel, ditheredPanel;
    private HistogramPanel histogramPanel;
    private int step = 1;

    private BufferedImage img;
    private int[][] rgbArray;
    private Color[][] originalColor, ditheredColor;

    Project1Frame() {
        this.setTitle("CMPT 365 Project 1");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);

        // Home Screen
        JLabel instructionLabel = new JLabel("Please select a .wav or .png file.");
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        instructionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        fileLabel = new JLabel("No File Selected");
        fileLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        fileButton = new JButton("Choose File");
        fileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        fileButton.addActionListener(this);

        panel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(panel);
        BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        panel.add(instructionLabel);
        panel.add(fileButton);
        panel.add(fileLabel);

        // Audio
        wavePanel = new WavePanel();
        wavePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel = new JPanel();
        totalSamplesLabel = new JLabel("Total Samples: ");
        samplingRateLabel = new JLabel("Sampling Rate: ");
        infoPanel.add(totalSamplesLabel);
        infoPanel.add(samplingRateLabel);

        // Images
        imagesPanel = new JPanel();
        imagesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        imgPanel = new ImagePanel();
        imgPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imgPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

        ditheredPanel = new ImagePanel();
        ditheredPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        imagesPanel.add(imgPanel);
        imagesPanel.add(ditheredPanel);

        histogramPanel = new HistogramPanel();
        histogramPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        controlPanel = new JPanel();

        nextButton = new JButton("Next");
        nextButton.addActionListener(this);
        nextButton.setAlignmentX(Component.RIGHT_ALIGNMENT);

        exitButton = new JButton("Exit");
        exitButton.addActionListener(this);
        exitButton.setAlignmentX(Component.RIGHT_ALIGNMENT);

        controlPanel.add(nextButton);
        controlPanel.add(exitButton);

        this.add(scrollPane, BorderLayout.CENTER);
        this.pack();
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == fileButton) {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(".png and .wav", "png", "wav", "wave");
            fileChooser.setFileFilter(filter);
            fileChooser.setCurrentDirectory(new File("."));

            int response = fileChooser.showOpenDialog(null);

            if (response == JFileChooser.APPROVE_OPTION) {
                File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
                String fileName = file.getName();
                int i = fileName.lastIndexOf(".");

                if (i != -1) {
                    String extension = fileName.substring(i);
                    if (extension.equals(".png")) {
                        RemoveAudioComponents();

                        fileLabel.setText(fileName);
                        CreateImagePanel(file);

                        this.pack();
                        this.setLocationRelativeTo(null);
                    } else if (extension.equals(".wav")) {
                        RemoveImageComponents();

                        fileLabel.setText(fileName);
                        CreateAudioPanel(file);

                        this.pack();
                        this.setLocationRelativeTo(null);
                    } else {
                        fileLabel.setText("Invalid File Format.");
                        fileLabel.setForeground(Color.RED);
                        Timer timer = new Timer(2000, event -> {
                            fileLabel.setText("No File Selected");
                            fileLabel.setForeground(Color.BLACK);
                        });
                        timer.setRepeats(false);
                        timer.start();
                    }
                }
            }
        }
        if (e.getSource() == nextButton) {
            if (step == 1) {
                ditheredColor = MakeDitheredImage(rgbArray);
                ditheredPanel.repaint(ditheredColor, img.getWidth(), img.getHeight());
                step = 2;
            } else if (step == 2) {
                ditheredPanel.repaint(null, img.getWidth(), img.getHeight());
                step = 1;
            }
        }
        if (e.getSource() == exitButton) {
            this.dispose();
        }
    }

    // Audio
    private void RemoveAudioComponents() {
        panel.remove(wavePanel);
        panel.remove(infoPanel);
    }

    private void CreateAudioPanel(File file) {
        try {
            AudioInputStream fileStream = AudioSystem.getAudioInputStream(file);
            byte[] data = new byte[fileStream.available()];
            fileStream.read(data);

            int channels = fileStream.getFormat().getChannels();
            int frameLength = (int) fileStream.getFrameLength();
            int bitsPerSample = fileStream.getFormat().getSampleSizeInBits();

            int sampleRate = (int) fileStream.getFormat().getSampleRate();
            int totalSamples = data.length / (channels * bitsPerSample / 8);
            int[][] finalAudio = new int[channels][frameLength];

            if (bitsPerSample == 16) {
                int index = 0;
                for (int d = 0; d < data.length;) {
                    for (int c = 0; c < channels; c++) {
                        int low = (int) data[d];
                        d++;
                        int high = (int) data[d];
                        d++;
                        finalAudio[c][index] = (high << 8) + (low & 0x00ff);
                    }
                    index++;
                }
            } else {
                int index = 0;
                for (int d = 0; d < data.length;) {
                    for (int c = 0; c < channels; c++) {
                        finalAudio[c][index] = (int) data[d];
                        d++;
                    }
                    index++;
                }
            }
            totalSamplesLabel.setText("Total Samples: " + totalSamples);
            samplingRateLabel.setText("Sampling Rate: " + sampleRate + "hz");
            wavePanel.repaint(finalAudio, channels);

            panel.add(infoPanel);
            panel.add(wavePanel);
        } catch (Exception ex) {
            fileLabel.setText("Error reading .wav");
            fileLabel.setForeground(Color.RED);
            Timer timer = new Timer(2000, event -> {
                fileLabel.setText("No File Selected");
                fileLabel.setForeground(Color.BLACK);
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    // Images
    private void RemoveImageComponents() {
        panel.remove(controlPanel);
        panel.remove(imagesPanel);
        panel.remove(histogramPanel);
    }

    private void CreateImagePanel(File file) {
        try {
            step = 1;
            img = ImageIO.read(file);
            rgbArray = GetRGBArray(img);
            originalColor = MakeColorArray(rgbArray);
            int width = img.getWidth(), height = img.getHeight();
            int[][] histograms = CreateHistogramArrays();

            imgPanel.repaint(originalColor, width, height);
            ditheredPanel.repaint(null, width, height);
            histogramPanel.repaint(histograms, width*2+13, 150);

            panel.add(imagesPanel);
            panel.add(histogramPanel);
            panel.add(controlPanel);
        } catch (Exception ex) {
            ex.printStackTrace();
            fileLabel.setText("Error reading .png");
            fileLabel.setForeground(Color.RED);
            Timer timer = new Timer(2000, event -> {
                fileLabel.setText("No File Selected");
                fileLabel.setForeground(Color.BLACK);
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    private int[][] GetRGBArray(BufferedImage image) {
        int w = image.getWidth(), h = image.getHeight();
        int[][] output = new int[w][h];

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                output[x][y] = image.getRGB(x, y);
            }
        }

        return output;
    }

    private Color[][] MakeColorArray(int[][] input) {
        int w = input.length, h = input[0].length;
        Color[][] output = new Color[w][h];

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int R = (input[x][y] >> 16) & 0xFF;
                int G = (input[x][y] >> 8) & 0xFF;
                int B = (input[x][y]) & 0xFF;
                output[x][y] = new Color(R, G, B);
            }
        }

        return output;
    }

    private int[][] CreateHistogramArrays() {
        int[][] rgbArrays = new int[3][256];
        Arrays.fill(rgbArrays[0], 0);
        Arrays.fill(rgbArrays[1], 0);
        Arrays.fill(rgbArrays[2], 0);
        for (int x = 0; x < originalColor.length; x++) {
            for (int y = 0; y < originalColor[0].length; y++) {
                rgbArrays[0][originalColor[x][y].getRed()]++;
                rgbArrays[1][originalColor[x][y].getGreen()]++;
                rgbArrays[2][originalColor[x][y].getBlue()]++;
            }
        }

        return rgbArrays;
    }

    private Color[][] MakeDitheredImage(int[][] input) {
        int row = 0, col = 0;
        int w = input.length, h = input[0].length;
        int[][] ditherMatrix = { { 15, 195, 60, 240 },
                { 135, 75, 180, 120 },
                { 45, 225, 30, 210 },
                { 165, 105, 150, 90 } };

        Color[][] output = MakeColorArray(input);

        for (int x = 0; x < w; x++) {
            row = x % 4;
            for (int y = 0; y < h; y++) {
                col = y % 4;
                int R = output[x][y].getRed();
                int G = output[x][y].getGreen();
                int B = output[x][y].getBlue();
                output[x][y] = new Color(GetValue(R, ditherMatrix, row, col), GetValue(G, ditherMatrix, row, col),
                        GetValue(B, ditherMatrix, row, col));
            }
        }
        return output;
    }

    private int GetValue(int n, int[][] ditherMatrix, int row, int col) {
        return (n > ditherMatrix[row][col]) ? 255 : 0;
    }
}