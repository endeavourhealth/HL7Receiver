package org.endeavourhealth.hl7test;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
//import org.endeavourhealth.transform.hl7v2.Hl7v2Transform;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainDialog {
    private JPanel panelMain;
    private JTextArea testPane1TextArea;
    private JButton transformButton;
    private JTextArea testPaneTextArea2;
    private JSplitPane splitPane;
    private JButton parseAndGenerateButton;
    private JButton saveButton;
    private JButton preTransformButton;
    private JFrame frame;

    public MainDialog(JFrame frame) {
        this.frame = frame;
        transformButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    testPaneTextArea2.setText("");
                    testPaneTextArea2.repaint();
                    testPaneTextArea2.revalidate();
                    //testPaneTextArea2.setText(Hl7v2Transform.transform(testPane1TextArea.getText()));
                } catch (Exception e1) {
                    testPaneTextArea2.setText("[" + e1.getClass() + "] " + e1.getMessage() + "\r\n" + ExceptionUtils.getStackTrace(e1));
                }
            }
        });

        preTransformButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    testPaneTextArea2.setText("");
                    testPaneTextArea2.repaint();
                    testPaneTextArea2.revalidate();
                    //testPaneTextArea2.setText(Hl7v2Transform.preTransformOnly(testPane1TextArea.getText()).replace("\r", "\r\n"));
                } catch (Exception e1) {
                    testPaneTextArea2.setText("[" + e1.getClass() + "] " + e1.getMessage() + "\r\n" + ExceptionUtils.getStackTrace(e1));
                }
            }
        });

        parseAndGenerateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    testPaneTextArea2.setText("");
                    testPaneTextArea2.repaint();
                    testPaneTextArea2.revalidate();
                    //testPaneTextArea2.setText(Hl7v2Transform.parseAndRecompose(testPane1TextArea.getText()).replace("\r", "\r\n"));
                } catch (Exception e1) {
                    testPaneTextArea2.setText("[" + e1.getClass() + "] " + e1.getMessage() + "\r\n" + ExceptionUtils.getStackTrace(e1));
                }
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    FileUtils.writeStringToFile(new File(System.getProperty("user.home") + "/Desktop/left.txt"), testPane1TextArea.getText().trim(), "UTF-8");
                    FileUtils.writeStringToFile(new File(System.getProperty("user.home") + "/Desktop/right.txt"), testPaneTextArea2.getText().trim(), "UTF-8");
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(null, "Exception:  " + e1.getMessage());
                }

            }
        });

        panelMain.addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent e) {
                splitPane.setDividerLocation(frame.getWidth() / 2);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                splitPane.setDividerLocation(frame.getWidth() / 2);
            }

            @Override
            public void componentShown(ComponentEvent e) {
                splitPane.setDividerLocation(frame.getWidth() / 2);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                splitPane.setDividerLocation(frame.getWidth() / 2);
            }
        });


    }

    public JPanel getPanelMain() {
        return panelMain;
    }
}
