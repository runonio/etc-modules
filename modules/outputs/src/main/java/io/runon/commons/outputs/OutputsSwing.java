package io.runon.commons.outputs;

import io.runon.commons.utils.FileUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * @author macle
 */
public class OutputsSwing {
    private final JTextField srcDirField = new JTextField("",30);
    private final JTextField outDirField = new JTextField("",30);

    private final JTextField extensionField = new JTextField("",10);


    private final JFrame frame;


    private final SpringLayout springL = new SpringLayout();

    private JPanel lastPanel;

    public OutputsSwing(){
        String os = System.getProperty("os.name").toLowerCase();

        frame = new JFrame("src outputs");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(springL);
//        frame.setLayout(new GridLayout(4,0));
//        frame.setLayout(new GridLayout(4,1));
        setRow("src", srcDirField);
        setRow("out", outDirField);
        setExtension();
        setCreateBtn();
        
        frame.setSize(560, 200);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void setRow(String name, JTextField textField){
        JLabel jLabel = new JLabel(name + " dir path: ");
        JButton button = new JButton("path search");
        button.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = fileChooser.showOpenDialog(frame);
            if(option == JFileChooser.APPROVE_OPTION){
                File file = fileChooser.getSelectedFile();
                textField.setText(file.getAbsolutePath());
            }
        });
        JPanel panel = new JPanel();
        panel.add(jLabel);
        panel.add(textField);
        panel.add(button);

        if(lastPanel != null){
            springL.putConstraint(SpringLayout.SOUTH, panel, Spring.constant(35), SpringLayout.SOUTH, lastPanel);
        }
        lastPanel = panel;


        frame.add(panel);
    }
    
    private void setExtension(){
        JLabel jLabel = new JLabel("file extension: ");
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.setSize(540, 20);
        panel.add(jLabel);
        panel.add(extensionField);
        extensionField.setText("xlsx");
        springL.putConstraint(SpringLayout.SOUTH, panel, Spring.constant(35), SpringLayout.SOUTH, lastPanel);
        lastPanel = panel;
        frame.add(panel, "West");
    }

    private void setCreateBtn(){
        JButton button = new JButton("create");
        button.addActionListener(e -> {
            OutDirFileInfoToExcel outDirFileInfoToExcel = new OutDirFileInfoToExcel();
            String outPath = outDirField.getText()+ "/src_files." + extensionField.getText().trim();
            if(FileUtil.isFile(outPath)){
                outPath = FileUtil.makeName(new File(outPath));
            }
            outDirFileInfoToExcel.out(srcDirField.getText(), outPath);

            OutProgramTableMapToExcel outProgramTableMapToExcel = new OutProgramTableMapToExcel();
            outPath = outDirField.getText()+ "/src_programs." +  extensionField.getText().trim();
            if(FileUtil.isFile(outPath)){
                outPath = FileUtil.makeName(new File(outPath));
            }
            outProgramTableMapToExcel.out(srcDirField.getText(), outPath);

        });
        JPanel panel = new JPanel();
        panel.add(button);

        springL.putConstraint(SpringLayout.SOUTH, panel, Spring.constant(35), SpringLayout.HORIZONTAL_CENTER, lastPanel);


        frame.add(panel);
    }




    public static void main(String[] args) {
        new OutputsSwing();
    }

}
