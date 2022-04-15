import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import java.io.File;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Comparator;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

class HuffmanNode {
    int item;
    int c;
    HuffmanNode left;
    HuffmanNode right;
}

class ImplementComparator implements Comparator<HuffmanNode> {
    public int compare(HuffmanNode x, HuffmanNode y) {
        return x.item - y.item;
    }
}

class RGB{
    int[][] R;
    int[][] G;
    int[][] B;
}

public class Project2Frame extends JFrame implements ActionListener {
    private JPanel panel;
    private JButton fileButton;
    private JLabel fileLabel;

    // Audio
    private JPanel audioPanel;
    private JLabel ratioLabel, beforeLabel, afterLabel;
    private static double bitsAfterEncoding;

    // Image
    private RGB rgbArray;
    private JPanel imagePanel;
    private ImagePanel originalPanel, compressedPanel;

    Project2Frame() {
        this.setTitle("CMPT 365 Project 2");
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
        audioPanel = new JPanel();
        audioPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        audioPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        BoxLayout audioLayout = new BoxLayout(audioPanel, BoxLayout.Y_AXIS);
        audioPanel.setLayout(audioLayout);
        beforeLabel = new JLabel();
        afterLabel = new JLabel();
        ratioLabel = new JLabel();
        audioPanel.add(beforeLabel);
        audioPanel.add(afterLabel);
        audioPanel.add(ratioLabel);

        // Image
        imagePanel = new JPanel();
        imagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        originalPanel = new ImagePanel();
        originalPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        originalPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

        compressedPanel = new ImagePanel();
        compressedPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        imagePanel.add(originalPanel);
        imagePanel.add(compressedPanel);

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

                if (i != 1) {
                    String extension = fileName.substring(i);
                    if (extension.equals(".png")) {
                        fileLabel.setText(fileName);

                        RemoveAudioComponents();

                        InitializeImagePanel(file);

                        this.pack();
                        this.setLocationRelativeTo(null);
                    } else if (extension.equals(".wav")) {
                        fileLabel.setText(fileName);

                        RemoveImageComponents();

                        InitializeAudioPanel(file);

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
    }

    private void RemoveAudioComponents() {
        panel.remove(audioPanel);
    }

    private void RemoveImageComponents() {
        panel.remove(imagePanel);
    }

    private void InitializeAudioPanel(File file) {
        try {
            AudioInputStream fileStream = AudioSystem.getAudioInputStream(file);
            byte[] data = new byte[fileStream.available()];
            fileStream.read(data);

            double bitsBeforeEncoding = data.length * 8;

            int[] finalAudio = new int[data.length];

            for (int i = 0; i < data.length; i++) {
                finalAudio[i] = (int) data[i] & 0xFF;
            }

            bitsAfterEncoding = 0;
            CreateHuffman(finalAudio);

            double compressionRatio = Math.round((bitsBeforeEncoding / bitsAfterEncoding) * 100.0) / 100.0;
            beforeLabel.setText(String.format("%-25s%s", "Bits Before Encoding: ", bitsBeforeEncoding));
            afterLabel.setText(String.format("%-25s%s", "Bits After Encoding: ", bitsAfterEncoding));
            ratioLabel.setText(String.format("%-25s%s", "Compression Ratio: ", compressionRatio));
            panel.add(audioPanel);
        } catch (Exception ex) {
            fileLabel.setText("Error reading .wav");
            fileLabel.setForeground(Color.RED);
            Timer timer = new Timer(2000, event -> {
                fileLabel.setText("No File Selected");
                fileLabel.setForeground(Color.BLACK);
            });
            timer.setRepeats(false);
            timer.start();
            ex.printStackTrace();
        }
    }

    private static void CreateHuffman(int[] input) {
        int n = 256;
        int[] frequencies = new int[256];

        for (int i = 0; i < input.length; i++) {
            frequencies[input[i]]++;
        }

        PriorityQueue<HuffmanNode> q = new PriorityQueue<HuffmanNode>(n, new ImplementComparator());

        for (int i = 0; i < frequencies.length; i++) {
            HuffmanNode hn = new HuffmanNode();

            hn.c = i;
            hn.item = frequencies[i];

            hn.left = null;
            hn.right = null;

            q.add(hn);
        }

        HuffmanNode root = null;

        while (q.size() > 1) {

            HuffmanNode x = q.peek();
            q.poll();

            HuffmanNode y = q.peek();
            q.poll();

            HuffmanNode f = new HuffmanNode();

            f.item = x.item + y.item;
            f.c = '-';
            f.left = x;
            f.right = y;
            root = f;

            q.add(f);
        }
        getBits(root, "");
    }

    private static void getBits(HuffmanNode root, String s) {
        if (root.left == null && root.right == null) {
            bitsAfterEncoding += (root.item) * String.valueOf(s).length();

            return;
        }
        getBits(root.left, s + "0");
        getBits(root.right, s + "1");
    }

    private void InitializeImagePanel(File file) {
        try {
            BufferedImage img = ImageIO.read(file);
            rgbArray = GetRGBArray(img);
            Color[][] colourArray = MakeColorArray(rgbArray);
            int width = img.getWidth(), height = img.getHeight();

            long bitsBeforeEncoding = 24 * width * height;

            for (int i = 0; i < width;) {
                for (int j = 0; j < height;) {
                    //int[][] redMatrix = CalculateDCT(rgbArray.R, 0, 0);
                    //int[][] greenMatrix = CalculateDCT(rgbArray.G);
                    //int[][] blueMatrix = CalculateDCT(rgbArray.B);
                    
                    j += 8;
                }
                i += 8;
            }

            //CalculateDCT();

            originalPanel.repaint(colourArray, width, height);
            compressedPanel.repaint(null, width, height);
            panel.add(imagePanel);
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

    private RGB GetRGBArray(BufferedImage image) {
        int w = image.getWidth(), h = image.getHeight();
        RGB result = new RGB();
        result.R = new int[w][h];
        result.G = new int[w][h];
        result.B = new int[w][h];

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int byteVals = image.getRGB(x, y);
                result.R[x][y] = (byteVals >> 16) & 0xFF;
                result.G[x][y] = (byteVals >> 8) & 0xFF;
                result.B[x][y] = (byteVals) & 0xFF;
            }
        }

        return result;
    }

    private Color[][] MakeColorArray(RGB input) {
        int w = input.R.length, h = input.R[0].length;
        Color[][] output = new Color[w][h];

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int R = input.R[x][y];
                int G = input.G[x][y];
                int B = input.B[x][y];
                output[x][y] = new Color(R, G, B);
            }
        }

        return output;
    }

    private int[][] CalculateDCT(int[][] input, int left, int top) {
        int M = input.length;
        int N = input[0].length;
        double[][] temp = CalculateRowTransform(input, N, M, left, top);
        temp = CalculateColumnTransform(temp, N, M, left, top);
        temp = Quantization(temp, left, top);
        temp = ReverseQuantization(temp, left, top);
        int[][] result = InverseDCT(temp, left, top);
        PrintMatrix(result);

        return result;
    }

    private static double[][] CalculateRowTransform(int[][] input, int N, int M, int left, int top) {
        double[][] result = new double[M][N];
        double coefficient = Math.sqrt(2.0 / M);
        for (int u = 0; u < M; u++) {
            double temp = 0;
            double Cu;
            if (u == 0)
                Cu = (Math.sqrt(2)) / 2;
            else
                Cu = 1;
            for (int j = 0; j < N; j++) {
                for (int i = 0; i < M; i++) {
                    temp += Math.cos(((2 * i + 1) * u * Math.PI) / (2 * M)) * input[i][j];
                }
                temp *= Cu * coefficient;

                result[u][j] = temp;

                temp = 0;
            }
        }
        return result;
    }

    private static double[][] CalculateColumnTransform(double[][] input, int N, int M, int left, int top) {
        double[][] result = new double[M][N];
        double coefficient = Math.sqrt(2.0 / N);
        for (int v = 0; v < N; v++) {
            double temp = 0;
            double Cv;
            if (v == 0)
                Cv = (Math.sqrt(2)) / 2;
            else
                Cv = 1;
            for (int u = 0; u < M; u++) {
                for (int j = 0; j < N; j++) {
                    temp += Math.cos(((2 * j + 1) * v * Math.PI) / (2 * N)) * input[u][j];
                }
                temp *= Cv * coefficient;

                result[u][v] = temp;

                temp = 0;
            }
        }

        return result;
    }

    private static double[][] Quantization(double[][] input, int left, int top) {
        int[][] quantizationTable = { { 1, 1, 2, 4, 8, 16, 32, 64 },
                { 1, 1, 2, 4, 8, 16, 32, 64 },
                { 2, 2, 2, 4, 8, 16, 32, 64 },
                { 4, 4, 4, 4, 8, 16, 32, 64 },
                { 8, 8, 8, 8, 8, 16, 32, 64 },
                { 16, 16, 16, 16, 16, 16, 32, 64 },
                { 32, 32, 32, 32, 32, 32, 32, 64 },
                { 64, 64, 64, 64, 64, 64, 64, 64 } };
        double[][] result = new double[input.length][input[0].length];
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                result[i][j] = input[i][j] / quantizationTable[i][j];
            }
        }

        return result;
    }

    private static double[][] ReverseQuantization(double[][] input, int left, int top) {
        int[][] quantizationTable = { { 1, 1, 2, 4, 8, 16, 32, 64 },
                { 1, 1, 2, 4, 8, 16, 32, 64 },
                { 2, 2, 2, 4, 8, 16, 32, 64 },
                { 4, 4, 4, 4, 8, 16, 32, 64 },
                { 8, 8, 8, 8, 8, 16, 32, 64 },
                { 16, 16, 16, 16, 16, 16, 32, 64 },
                { 32, 32, 32, 32, 32, 32, 32, 64 },
                { 64, 64, 64, 64, 64, 64, 64, 64 } };
        double[][] result = new double[input.length][input[0].length];
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                result[i][j] = input[i][j] * quantizationTable[i][j];
            }
        }

        return result;
    }

    private static int[][] InverseDCT(double[][] input, int left, int top) {
        int[][] result = new int[input.length][input[0].length];
        for (int u = 0; u <= input.length - 1; u++) {
            for (int v = 0; v <= input[0].length - 1; v++) {
                double temp = 0;
                double Cu;
                double Cv;
                if (u == 0)
                    Cu = (Math.sqrt(2)) / 2;
                else
                    Cu = 1;
                if (v == 0)
                    Cv = (Math.sqrt(2)) / 2;
                else
                    Cv = 1;

                for (int i = 0; i <= input.length - 1; i++) {
                    for (int j = 0; j <= input[0].length - 1; j++) {
                        temp += ((Cu * Cv) / 4) * Math.cos(((2 * i + 1) * u * Math.PI) / 16)
                                * Math.cos(((2 * j + 1) * v * Math.PI) / 16)
                                * input[i][j];
                    }
                }
                int res = (int) Math.round(temp);

                result[u][v] = res;
            }
        }
        return result;
    }

    private static void PrintMatrix(int[][] input) {
        int max = 0;
        boolean isNegative = false;

        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                if (Math.abs(input[i][j]) > max) {
                    max = Math.abs(input[i][j]);
                }
                if (input[i][j] < 0) {
                    isNegative = true;
                }
            }
        }
        String temp = Integer.toString(max);
        int maxWidth = temp.length();

        if (isNegative)
            maxWidth++;

        String format = "%" + maxWidth + "d";
        for (int i = 0; i < input.length; i++) {
            System.out.print("|");
            for (int j = 0; j < input[0].length; j++) {
                System.out.printf(format + " ", input[i][j]);
            }
            System.out.println("|");
        }

    }
}