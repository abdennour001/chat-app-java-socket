package com.main;

import com.sun.xml.internal.bind.v2.model.core.ID;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ChatClient implements Runnable
{
    public static JFrame loginFrame;
    public static ClientGUI clientGUI = null;
    private Socket socket              = null;
    private Thread thread              = null;
    private DataInputStream  console   = null;
    private DataOutputStream streamOut = null;
    private ChatClientThread client    = null;
    private BufferedWriter writer;
    private String userName="Abdennour";

    public ChatClient(String serverName, int serverPort, ClientGUI c, String username) {
        System.out.println("Establishing connection. Please wait ...");
        try {
            socket = new Socket(serverName, serverPort);
            writer= new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            System.out.println("Connected: " + socket);
            clientGUI = c;
            this.userName = username;
            clientGUI.title.setText("Signed in as [ "+ username +" ]");
            start();
        } catch(UnknownHostException uhe) {
            System.out.println("Host unknown: " + uhe.getMessage());
        } catch(IOException ioe) {
            System.out.println("Unexpected exception: " + ioe.getMessage());
        }
    }


    public void run() {
        while (thread != null) {
            try {
                //System.out.println();
                System.out.flush();
                if (clientGUI.isSend() && clientGUI.isReady()) {
                    String socketMsg= userName;
                    if (clientGUI.getStatus() == 0) {
                        socketMsg = "0" + "//" + socketMsg + "//" + "null" + "//" + "null";
                    } else {
                        if (clientGUI.getStatus() == 1) {
                            socketMsg = "1" + "//" + socketMsg + "//" + clientGUI.listFriend.getSelectedValue() + "//" + "null";
                        } else {
                            if (clientGUI.getStatus() == 2) {
                                socketMsg = "2" + "//" + socketMsg + "//" + clientGUI.listFriend.getSelectedValue() + "//" + clientGUI.msgField.getText();
                                //writer.write(msg + "\r\n");
                                //clientGUI.msgArea.append(msg);
                                clientGUI.msgField.setText("");
                            } else {
                                if (clientGUI.getStatus() == 6) {
                                    // create a group
                                    int l=clientGUI.createGroupeGUI.count;
                                    int numberOfMem=0; String mem="";
                                    for (int i=0; i<l; i++) {
                                        if (clientGUI.createGroupeGUI.memebrsCheckBox[i].isSelected()) {
                                            mem += clientGUI.createGroupeGUI.memebrsCheckBox[i].getText()+"//";
                                            numberOfMem++;
                                        }
                                    }
                                    socketMsg = "6"+"//"+socketMsg+"//"+"null"+"//"+"null//"+(numberOfMem+1)+"//"+clientGUI.createGroupeGUI.textField1.getText()+"//"+mem;
                                    socketMsg += userName+"//";
                                } else if (clientGUI.getStatus() == 10) {
                                    // sign out
                                    socketMsg = "10" + "//" + socketMsg + "//" + "null" + "//" + "null";
                                }
                            }
                        }
                    }
                    streamOut.writeUTF(socketMsg);
                    clientGUI.setSend(false);
                    clientGUI.setReady(false);
                }
                //streamOut.writeUTF(console.readLine());
                streamOut.flush();
            } catch(IOException ioe) {
                System.out.println("Sending error: " + ioe.getMessage());
                stop();
            }
        }
    }

    private String messageToString(String msg) {
        String string="";
        string = msg;
        return string;
    }

    public void handle(String msg) {
        if (msg.equals(".bye")) {
            System.out.println("Good bye. Press RETURN to exit ...");
            stop();
        }
        else {
            // handle the message from the server
            //System.out.println(msg);
            int code = Integer.parseInt(msg.split("//")[0]);
            switch (code) {
                case 0:
                    // add the new client to the old clients
                    clientGUI.addFriend(msg.split("//")[1]);
                    break;
                case 1:
                    // show the chat
                    clientGUI.msgArea.setText(msg.split("//")[1]);
                    break;
                case 3:
                    // adding the old clients to the new client
                    int number = Integer.parseInt(msg.split("//")[1]);
                    for (int i=0; i<number; i++) {
                        clientGUI.addFriend(msg.split("//")[i+2]);
                    }
                    break;
                case 2:
                    // regular sending
                    /*if (msg.split("//")[1].equals(this.userName)) {
                        clientGUI.msgArea.append("Me: " + msg.split("//")[2].split(":")[1] + "\r\n\n");
                    } else {
                        clientGUI.msgArea.setText(msg.split("//")[2] + "\r\n\n");
                    }*/
                    if (clientGUI.listFriend.getSelectedValue() != null) {
                        if (userName.equals(msg.split("//")[1]) || clientGUI.listFriend.getSelectedValue().equals(msg.split("//")[1])) {
                            clientGUI.msgArea.setText("");
                            clientGUI.msgArea.setText(msg.split("//")[3]);
                        }
                    }
                    break;
                case 6:
                    // add the group to the chat
                    clientGUI.addFriend(msg.split("//")[1].trim() + " [group chat]");
                    break;
                case 7:
                    if (clientGUI.listFriend.getSelectedValue() != null) {
                        if (userName.equals(msg.split("//")[1]) || clientGUI.listFriend.getSelectedValue().equals(msg.split("//")[2])) {
                            clientGUI.msgArea.setText("");
                            clientGUI.msgArea.setText(msg.split("//")[3]);
                        }
                    }
                    break;
                case 10:
                    if (clientGUI.listFriend.getSelectedValue().equals(msg.split("//")[1])) clientGUI.msgArea.setText("");
                    clientGUI.removeFriend(msg.split("//")[1]);
                    break;
            }
            //System.out.println(msg);
        }
    }

    public void start() throws IOException {

        //console   = new DataInputStream(System.in);
        streamOut = new DataOutputStream(socket.getOutputStream());
        if (thread == null) {
            client = new ChatClientThread(this, socket);
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
        try {
            if (console   != null)  console.close();
            if (streamOut != null)  streamOut.close();
            if (socket    != null)  socket.close();
        } catch(IOException ioe) {
            System.out.println("Error closing ..."); }
            client.close();
            client.stop();
        }
    public static void main(String[] args) {
        /*ChatClient client = null;
        if (args.length != 2)
            System.out.println("Usage: java ChatClient host port");
        else
            client = new ChatClient("0.0.0.0", 5000);*/

        LoginGUI l=new LoginGUI();
        loginFrame=new JFrame("Login");
        loginFrame.setPreferredSize(new Dimension(340, 170));
        loginFrame.setContentPane(l.panelMain);
        loginFrame.setResizable(false);
        loginFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        loginFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (JOptionPane.showConfirmDialog(loginFrame, "Are you sure you want leave login?", "Close Login?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
        loginFrame.pack();
        loginFrame.setVisible(true);
    }
}