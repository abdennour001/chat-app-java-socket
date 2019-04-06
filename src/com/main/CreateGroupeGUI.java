package com.main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CreateGroupeGUI {
    public JTextField textField1;
    public JPanel panelMain;
    private JButton createButton;
    private JPanel members;
    public JCheckBox[] memebrsCheckBox = new JCheckBox[50];
    public int count=0;

    public CreateGroupeGUI(JList listFriend) {

        int l = listFriend.getModel().getSize();
        for (int i=0; i<l; i++) {
            String name = (String) ((DefaultListModel) listFriend.getModel()).get(i);
            if (!name.contains("[group chat]")) {
                JCheckBox c=new JCheckBox(name);
                memebrsCheckBox[i] = c;
                count++;
                members.setLayout(new BoxLayout(members, BoxLayout.Y_AXIS));
                members.add(c);
            }
        }

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChatClient.clientGUI.setSend(true);
                ChatClient.clientGUI.setStatus(6);
                ChatClient.clientGUI.setReady(true);
                ChatClient.clientGUI.createGroup.setVisible(false);
            }
        });
    }
}
