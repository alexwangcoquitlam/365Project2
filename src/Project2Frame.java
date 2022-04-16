package src;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.HashMap;

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

class RGB {
    int[][] R;
    int[][] G;
    int[][] B;
}

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

public class Project2Frame extends JFrame implements ActionListener {
    private JPanel panel;
    private JButton fileButton;
    private JLabel fileLabel;

    // Audio
    private JPanel audioPanel;
    private WavePanel wavePanel;
    private JLabel ratioLabel;
    private static double bitsAfterEncoding;
    private static HashMap<Integer, String> huffmanDictionary = new HashMap<Integer, String>();
    private static HashMap<String, Integer> reverseDictionary = new HashMap<String, Integer>();

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
        audioPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        BoxLayout audioLayout = new BoxLayout(audioPanel, BoxLayout.Y_AXIS);
        audioPanel.setLayout(audioLayout);
        ratioLabel = new JLabel();
        ratioLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        wavePanel = new WavePanel();
        wavePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        audioPanel.add(wavePanel);

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
                        RemoveImageComponents();

                        InitializeAudioPanel(file);

                        fileLabel.setText(fileName);

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
        panel.remove(ratioLabel);
        panel.remove(audioPanel);
    }

    private void RemoveImageComponents() {
        panel.remove(ratioLabel);
        panel.remove(imagePanel);
    }

    private void InitializeAudioPanel(File file) {
        try {
            AudioInputStream fileStream = AudioSystem.getAudioInputStream(file);
            byte[] data = new byte[fileStream.available()];
            fileStream.read(data);

            double bitsBeforeEncoding = data.length * 8;

            int[] byteData = new int[data.length];

            for (int i = 0; i < data.length; i++) {
                byteData[i] = (int) data[i] + 128;
            }

            bitsAfterEncoding = 0;
            huffmanDictionary.clear();
            EncodeHuffman(byteData);
            int[] decodedAudio = DecodeHuffman();

            int channels = fileStream.getFormat().getChannels();
            int frameLength = (int) fileStream.getFrameLength();
            int bitsPerSample = fileStream.getFormat().getSampleSizeInBits();

            
            int[][] finalAudio = new int[channels][frameLength];

            if (bitsPerSample == 16) {
                int index = 0;
                for (int d = 0; d < data.length;) {
                    for (int c = 0; c < channels; c++) {
                        int low = decodedAudio[d] - 128;
                        d++;
                        int high = decodedAudio[d] - 128;
                        d++;
                        finalAudio[c][index] = (high << 8) + (low & 0x00ff);
                    }
                    index++;
                }
            } else {
                int index = 0;
                for (int d = 0; d < data.length;) {
                    for (int c = 0; c < channels; c++) {
                        finalAudio[c][index] = decodedAudio[d] - 128;
                        d++;
                    }
                    index++;
                }
            }

            wavePanel.repaint(finalAudio, channels);

            double compressionRatio = Math.round((bitsBeforeEncoding / bitsAfterEncoding) * 100.0) / 100.0;
            ratioLabel.setText("Compression Ratio: " + compressionRatio);
            panel.add(ratioLabel);
            panel.add(audioPanel);
        } catch (Exception ex) {
            fileLabel.setText("Error encoding file.");
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

    private void EncodeHuffman(int[] input) {
        System.out.println("Performing Huffman encoding...");
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

        System.out.println("Finishing Huffman encoding...");
        System.out.println("Creating Huffman code, this can take a while...");
        try {
            System.out.println("Writing to file...");
            File encodedHuffman = new File("encodedHuffman.txt");
            if (encodedHuffman.createNewFile()) {
                System.out.println("File created: " + encodedHuffman.getName());
            } else {
                encodedHuffman.delete();
                encodedHuffman.createNewFile();
            }
            FileWriter writer = new FileWriter(encodedHuffman.getName());
            for(int i = 0; i < input.length; i++){
                String code = huffmanDictionary.get(input[i]);
                writer.write(code + " ");
            }
            writer.close();
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

    private int[] DecodeHuffman() {
        System.out.println("Performing decoding...");
        try {
            Path newPath = Paths.get("encodedHuffman.txt");
            String fileString = Files.readString(newPath);
            String[] fileArr = fileString.split("[ \n]");
            int[] result = new int[fileArr.length];
            for(int i = 0; i < fileArr.length; i++){
                result[i] = reverseDictionary.get(fileArr[i]);
            }
            return result;
        } catch (Exception ex) {
            fileLabel.setText("Error decoding file.");
            fileLabel.setForeground(Color.RED);
            Timer timer = new Timer(2000, event -> {
                fileLabel.setText("No File Selected");
                fileLabel.setForeground(Color.BLACK);
            });
            timer.setRepeats(false);
            timer.start();
            ex.printStackTrace();
        }
        return new int[0];
    }

    private static void getBits(HuffmanNode root, String s) {
        if (root.left == null && root.right == null) {
            bitsAfterEncoding += (root.item) * String.valueOf(s).length();
            huffmanDictionary.put(root.c, s);
            reverseDictionary.put(s, root.c);
            return;
        }
        getBits(root.left, s + "0");
        getBits(root.right, s + "1");
    }

    private void InitializeImagePanel(File file) {
        try {
            BufferedImage img = ImageIO.read(file);
            rgbArray = GetRGBArray(img);
            Color[][] originalColourArray = MakeColorArray(rgbArray);
            int width = img.getWidth(), height = img.getHeight();

            long bitsBeforeEncoding = 24 * width * height;
            bitsAfterEncoding = 0;

            System.out.println("Calculating DCT for red channel...");
            rgbArray.R = DCTEncoding("red");
            System.out.println("Calculating DCT for green channel...");
            rgbArray.G = DCTEncoding("green");
            System.out.println("Calculating DCT for blue channel...");
            rgbArray.B = DCTEncoding("blue");
            Color[][] compressedColour = MakeColorArray(rgbArray);

            double compressionRatio = ((bitsBeforeEncoding/bitsAfterEncoding)*100.0)/100.0;
            ratioLabel.setText("Compression Ratio: " + compressionRatio);

            originalPanel.repaint(originalColourArray, width, height);
            compressedPanel.repaint(compressedColour, width, height);
            panel.add(ratioLabel);
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

    private int[][] DCTEncoding(String colour){
        int[][] colourArr = new int[rgbArray.R.length][rgbArray.R[0].length];
        if(colour.equals("red")){
            colourArr = rgbArray.R; 
        }
        else if (colour.equals("green")){
            colourArr = rgbArray.G;
        }
        else if (colour.equals("blue")){
            colourArr = rgbArray.B;
        }
        for (int i = 0; i < colourArr.length-8; i += 8) {
            for (int j = 0; j < colourArr[0].length-8; j += 8) {
                int[][] temp = new int[8][8];
                for(int u = 0; u < 8; u++){
                    temp[u] = Arrays.copyOfRange(colourArr[i+u], j, j+8);
                }
                temp = CalculateDCT(temp);
                for(int v = 0; v < 8; v++){
                    for(int w = 0; w < 8; w++){
                        colourArr[i+v][j+w] = temp[v][w];
                    }
                }
            }
        }
        return colourArr;
    }

    private int[][] CalculateDCT(int[][] input) {
        int M = input.length;
        int N = input[0].length;
        double[][] temp = CalculateRowTransform(input, N, M);
        temp = CalculateColumnTransform(temp, N, M);
        temp = Quantization(temp);
        CalculateBits(temp);
        int[][] result = InverseDCT(temp);

        return result;
    }

    private static double[][] CalculateRowTransform(int[][] input, int N, int M) {
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

    private static double[][] CalculateColumnTransform(double[][] input, int N, int M) {
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

    private static double[][] Quantization(double[][] input) {
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

    private static int[][] InverseDCT(double[][] input) {
        int[][] result = new int[input.length][input[0].length];
        for (int i = 0; i <= input.length - 1; i++) {
            for (int j = 0; j <= input[0].length - 1; j++) {
                double temp = 0;
                double Cu;
                double Cv;

                for (int u = 0; u <= input.length - 1; u++) {
                    for (int v = 0; v <= input[0].length - 1; v++) {
                        if (u == 0)
                            Cu = (Math.sqrt(2)) / 2;
                        else
                            Cu = 1;
                        if (v == 0)
                            Cv = (Math.sqrt(2)) / 2;
                        else
                            Cv = 1;
                        temp += ((Cu * Cv) / 4) * Math.cos(((2 * i + 1) * u * Math.PI) / 16)
                                * Math.cos(((2 * j + 1) * v * Math.PI) / 16)
                                * input[u][v];
                    }
                }
                int res = (int) Math.round(temp);

                result[i][j] = Math.min(Math.max(res,0),255);
            }
        }
        return result;
    }

    private void CalculateBits(double[][] input){
        for(int i = 0; i < input.length; i++){
            for(int j = 0; j < input[0].length; j++){
                int curr = (int)Math.round(input[i][j]);
                if(curr == 0){
                    bitsAfterEncoding += 1;
                }
                else{
                    bitsAfterEncoding += 8;
                }
            }
        }
    }
}