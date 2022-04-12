import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import java.io.File;
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

public class Project2Frame extends JFrame implements ActionListener{
    private JPanel panel;
    private JButton fileButton;
    private JLabel fileLabel;
    
    Project2Frame(){
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

            if(response == JFileChooser.APPROVE_OPTION){
                File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
                String fileName = file.getName();
                int i = fileName.lastIndexOf(".");

                if(i != 1){
                    String extension = fileName.substring(i);
                    if (extension.equals(".png")){
                        fileLabel.setText(fileName);

                        this.pack();
                        this.setLocationRelativeTo(null);
                    }
                    else if (extension.equals(".wav")){
                        fileLabel.setText(fileName);

                        this.pack();
                        this.setLocationRelativeTo(null);
                    }
                    else{
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
}
