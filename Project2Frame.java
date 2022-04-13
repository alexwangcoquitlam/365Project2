import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

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

public class Project2Frame extends JFrame implements ActionListener {
    private JPanel panel;
    private JButton fileButton;
    private JLabel fileLabel;

    // Audio
    private JPanel audioPanel;
    private JLabel ratioLabel;

    private static double bitsAfterEncoding = 0;

    Project2Frame() {
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

        audioPanel = new JPanel();
        audioPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        ratioLabel = new JLabel();
        ratioLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        audioPanel.add(ratioLabel);

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

                        this.pack();
                        this.setLocationRelativeTo(null);
                    } else if (extension.equals(".wav")) {
                        fileLabel.setText(fileName);

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

    }

    private void InitializeAudioPanel(File file) {
        try {
            AudioInputStream fileStream = AudioSystem.getAudioInputStream(file);
            byte[] data = new byte[fileStream.available()];
            fileStream.read(data);

            double bitsBeforeEncoding = data.length*8;

            int[] finalAudio = new int[data.length];

            for(int i = 0; i < data.length; i++){
                finalAudio[i] = (int)data[i] & 0xFF;
            }

            CreateHuffman(finalAudio);

            double compressionRatio = Math.round((bitsBeforeEncoding/bitsAfterEncoding)*100.0) / 100.0;
            ratioLabel.setText("Compression Ratio: " + compressionRatio);
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
            bitsAfterEncoding += (root.item)*String.valueOf(s).length();

            return;
        }
        getBits(root.left, s + "0");
        getBits(root.right, s + "1");
    }
}
