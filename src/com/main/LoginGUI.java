package com.main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LoginGUI {
    private JTextField textField1;
    private JButton signInButton;
    public JPanel panelMain;
    private JPasswordField passwordField1;
    private JLabel errorLabel;

    public LoginGUI() {
        errorLabel.setVisible(false);
        signInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // user name control
                    if (textField1.getText().equals("")) {
                        showStatus("Please put your name.");
                        return;
                    } else {
                        if (textField1.getText().trim().length() > 10) {
                            showStatus("Enter user name again, maximum size is 10 characters.");
                            return;
                        }
                    }
                    // password control
                    if (passwordField1.getText().equals("")) {
                        showStatus("Enter your password.");
                        return;
                    } else {
                        // other password controls
                        if (textField1.getText().trim().length() > 10) {
                            showStatus("Enter password again, maximum size is 10 characters.");
                            return;
                        }
                    }
                    String username = textField1.getText().trim();

                    // trim thing here, and pass data

                    ChatClient.loginFrame.setVisible(false);

                    JFrame mainFrame=new JFrame("App chat");
                    mainFrame.setPreferredSize(new Dimension(550, 400));
                    ClientGUI c=new ClientGUI();
                    mainFrame.setContentPane(c.panelMain);
                    mainFrame.setResizable(false);
                    mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                    mainFrame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            if (JOptionPane.showConfirmDialog(mainFrame, "Please leave chat by sign out", "Bad leaving!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                                System.exit(0);
                            }
                        }
                    });
                    mainFrame.pack();
                    mainFrame.setVisible(true);

                    // start Client session
                    ChatClient client = null;
                    client = new ChatClient("0.0.0.0", 5000, c, username);

                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            }
        });
    }

    private void showStatus(String error) {
        errorLabel.setText(error);
        errorLabel.setVisible(true);
    }


    /*public static void main(String[] args) {
        JFrame mainFrame=new JFrame("Login");
        mainFrame.setPreferredSize(new Dimension(340, 170));
        mainFrame.setContentPane(new LoginGUI().panelMain);
        mainFrame.setResizable(false);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }*/
}
