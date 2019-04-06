package com.main;

import javafx.scene.input.KeyCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;

public class ClientGUI {
    private JButton sendButton;
    public JTextField msgField;
    public JTextArea msgArea;
    public JList listFriend;
    public JPanel panelMain;
    private JLabel chatName;
    private JButton createNewGroupButton;
    private JButton signOutButton;
    public JLabel title;
    public CreateGroupeGUI createGroupeGUI;
    private boolean send=false;
    private int status=0;
    private boolean ready=true;
    public JFrame createGroup;

    public ClientGUI() {
        setSend(true);
        listFriend.clearSelection();
        listFriend.setSelectedIndex(0);
        if (listFriend.getSelectedValue() != null) {
            send=true;
            status=1;
            ready=true;
        }
        listFriend.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listFriend.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // list change listener
                chatName.setText("[" + listFriend.getSelectedValue().toString() + "]");
                // ask for the server to show the chat
                send=true;
                status=1;
                ready=true;
            }
        });
        msgField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    send = true;
                    status=2;
                    ready=true;
                }
                super.keyTyped(e);
            }
        });
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                send = true;
                status=2;
                ready=true;
            }
        });
        createNewGroupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createGroup=new JFrame("Create New Group");
                createGroupeGUI = new CreateGroupeGUI(listFriend);
                createGroup.setPreferredSize(new Dimension(460, 260));
                createGroup.setContentPane(createGroupeGUI.panelMain);
                createGroup.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                createGroup.setResizable(false);
                createGroup.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        if (JOptionPane.showConfirmDialog(createGroup, "Are you sure you want leave?", "Close?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                            send=true;
                            status=10;
                            ready=true;
                            System.exit(0);
                        }
                    }
                });
                createGroup.pack();
                createGroup.setVisible(true);
            }
        });
        signOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                        if (JOptionPane.showConfirmDialog(panelMain, "Are you sure you want to leave chat?", "Sign Out?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                            send = true;
                            status=10;
                            ready=true;
                            System.exit(0);
                        }
            }
        });
    }

    public void addFriend(String name) {
        ((DefaultListModel) listFriend.getModel()).addElement(name);
    }

    public void removeFriend(String name) {
        ((DefaultListModel) listFriend.getModel()).removeElement(name);
    }

    public boolean isSend() {
        return send;
    }

    public void setSend(boolean s) {
        send = s;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int s) {
        this.status = s;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

}
