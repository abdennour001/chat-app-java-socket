package com.main;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Scanner;

public class ChatServer implements Runnable
{
    private ChatServerThread[] clients = new ChatServerThread[50];
    private GroupChat[] groupChats = new GroupChat[50];
    private ServerSocket server = null;
    private Thread       thread = null;
    private int clientCount = 0;
    private int groupChatCount = 0;
    private int port;

    public JPanel panelMain;
    private JButton startButton;
    private JButton stopButton;
    private JTextArea textArea1;
    private JLabel serverStatus;

    public ChatServer(int port)
    {
        createFolder("src/data");
        this.port = port;
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    createFolder("/src/data/");
                    textArea1.append("Binding to port " + port + ", please wait  ..." + "\n");
                    server = new ServerSocket(port);
                    textArea1.append("Server started: " + server  + "\n");
                    textArea1.append("------------------" + "\n");
                    serverStatus.setForeground(new Color(0, 255, 0));
                    serverStatus.setText("Server is online.");
                    start();
                }
                catch(IOException ioe) {
                    textArea1.append("Can not bind to port " + port + ": " + ioe.getMessage() + "\n");
                    textArea1.append("------------------" + "\n");
                }
            }
        });
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    textArea1.append("Stopping server now..." + "\n");
                    textArea1.append("Server is stopped." + "\n");
                    textArea1.append("------------------" + "\n");
                    serverStatus.setForeground(new Color(255, 0, 0));
                    serverStatus.setText("Server is offline.");
                    server.close();
                    stop();
                    int count = clientCount;
                    for (int i = 0; i < count; i++) {
                        remove(clients[0].getID());
                    }
                    removeFolder("src/data/");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public void run() {
        while (thread != null)
        {
            try {
                textArea1.append("Waiting for a client ..." + "\n");
                textArea1.append("------------------" + "\n");
                addThread(server.accept());
            } catch(IOException ioe) {
                textArea1.append("Server accept error: " + ioe + "\n");
                textArea1.append("------------------" + "\n");
                stop();
            }
        }
    }

    public void start() {
        if (thread == null) {
            createFolder("/src/data/");
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
    }

    private int findClient(int ID) {
        for (int i = 0; i < clientCount; i++)
        if (clients[i].getID() == ID)
            return i;
        return -1;
    }

    private int findClientByName(String name) {
        for (int i = 0; i < clientCount; i++) {
            if (clients[i].getUserName().equals(name))
                return i;
        }
        return -1;
    }

    private int findGroupChat(String groupName) {
        for (int i=0; i < groupChatCount; i++) {
            if (groupChats[i].getGroupName().equals(groupName))
                return i;
        }
        return -1;
    }

    public synchronized void handle(int ID, String input) {
        if (input.equals(".bye")) {
            clients[findClient(ID)].send(".bye");
            remove(ID);
        }
        else {
            // send msg to all the friends
            //clients[i].send(ID + ": " + input);
            //clients[0].send(input.split("//")[1] + " : " + input);

            int code = Integer.parseInt(input.split("//")[0]);
            String userSource=input.split("//")[1];
            String userDest = input.split("//")[2];
            String data = input.split("//")[3];
            switch (code) {
                case 0:
                    // new client
                    clients[clientCount-1].setUserName(userSource);
                    // create new folder to new client
                    createFolder("src/data/"+userSource);

                    // send it to all clients
                    for (int i=0; i<clientCount-1; i++) {
                        clients[i].send(code + "//" + userSource);
                        // create the file chat in all other clients
                        createFile("src/data/"+clients[i].getUserName()+"/"+userSource);
                    }

                    // send Client names to our new client
                    String clientList="3//"+ (clientCount-1) +"//";
                    for (int i=0; i<clientCount-1; i++) {
                        clientList += clients[i].getUserName() + "//";
                        createFile("src/data/"+userSource+"/"+clients[i].getUserName());
                    }
                    clients[clientCount-1].send(clientList);
                    break;
                case 1:
                    // ask the server for the chat file
                    if (userDest.contains("[group chat]")) {
                        String name=userDest.replace(" [group chat]", "");
                        StringBuilder chat= new StringBuilder();
                        try {
                            File fileChat=new File("src/data/"+name+"/"+name+".txt");
                            Scanner sc=new Scanner(fileChat);
                            while (sc.hasNext()) {
                                chat.append(sc.nextLine()+"\r\n");
                            }
                            // send the chat to the demanding
                            for (int i=0; i<clientCount; i++) {
                                if (clients[i].getUserName().equals(userSource)) clients[i].send("1//" + chat);
                            }
                            sc.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        StringBuilder chat= new StringBuilder();
                        try {
                            File fileChat=new File("src/data/"+userSource+"/"+userDest+".txt");
                            Scanner sc=new Scanner(fileChat);
                            while (sc.hasNext()) {
                                chat.append(sc.nextLine()+"\r\n");
                            }
                            // send the chat to the demanding
                            for (int i=0; i<clientCount; i++) {
                                if (clients[i].getUserName().equals(userSource)) clients[i].send("1//" + chat);
                            }
                            sc.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 2:
                    // regular sending msg
                    if (userDest.contains("[group chat]")) {
                        // sending to a group chat
                        String name=userDest.replace(" [group chat]", "");
                        GroupChat g = groupChats[findGroupChat(name.trim())];

                        // update files
                        writeLineToFile("src/data/"+name+"/"+name+".txt",userSource +": " + data);

                        StringBuilder chatGroup= new StringBuilder();

                        try {
                            File chatGroupSrc=new File("src/data/"+name+"/"+name+".txt");
                            Scanner scGroup=new Scanner(chatGroupSrc);

                            // get old messages
                            while(scGroup.hasNext()) {
                                chatGroup.append(scGroup.nextLine()+"\r\n");
                            }

                            scGroup.close();

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        for (int i=0; i<g.getMembersCount(); i++) {
                            g.getMembers()[i].send("7//" + userSource + "//" + userDest + "//" + chatGroup);
                        }
                    } else {
                        // update files
                        writeLineToFile("src/data/"+userSource+"/"+userDest+".txt","Me: " + data);
                        writeLineToFile("src/data/"+userDest+"/"+userSource+".txt", userSource+ ": " + data);

                        StringBuilder chatSource= new StringBuilder();
                        StringBuilder chatDest= new StringBuilder();
                        try {
                            File chatFileSource=new File("src/data/"+userSource+"/"+userDest+".txt");
                            File chatFileDest=new File("src/data/"+userDest+"/"+userSource+".txt");
                            Scanner scSource=new Scanner(chatFileSource);
                            Scanner scDest=new Scanner(chatFileDest);

                            // get old messages
                            while(scSource.hasNext()) {
                                chatSource.append(scSource.nextLine()+"\r\n");
                            }

                            while (scDest.hasNext()) {
                                chatDest.append(scDest.nextLine()+"\r\n");
                            }

                            scDest.close();
                            scSource.close();

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        // sending to regular user
                        for (int i=0; i<clientCount; i++) {
                            if (clients[i].getUserName().equals(userDest)) clients[i].send("2//"+userSource+"//"+userDest+"//"+chatDest);
                            if (clients[i].getUserName().equals(userSource)) clients[i].send("2//"+userSource+"//"+userDest+"//"+chatSource);

                            //if (clients[i].getUserName().equals(userDest) || clients[i].getUserName().equals(userSource)) clients[i].send("2//" + userSource + "//" + userSource + ": " + data);
                        }
                    }
                    break;
                case 6:
                    // create a group
                    // socket : [6//src//null//null//length//name//member_1//...//member_n]

                    int groupLength = Integer.parseInt(input.split("//")[4].trim());
                    String groupName = input.split("//")[5].trim();
                    GroupChat g=new GroupChat();
                    g.setGroupName(groupName);
                    // create new folder to new group
                    createFolder("src/data/"+groupName);
                    createFile("src/data/"+groupName+"/"+groupName);

                    for (int i=0; i<groupLength; i++) {
                        g.addMember(clients[findClientByName(input.split("//")[i+6].trim())]);
                        // send the group name to all clients
                        g.getMembers()[i].send("6//" + groupName);
                    }
                    groupChats[groupChatCount++] = g;
                    break;
                case 10:
                    // sign out
                    // remove client from all others
                    removeFolder("src/data/"+userSource);
                    // send it to all clients
                    for (int i=0; i<clientCount; i++) {
                        clients[i].send(code + "//" + userSource);
                        // remove the file chat in all other clients
                        removeFile("src/data/"+clients[i].getUserName()+"/"+userSource+".txt");
                    }
                    break;
            }
        }
    }

    public synchronized void remove(int ID)
    {  int pos = findClient(ID);
        if (pos >= 0)
        {  ChatServerThread toTerminate = clients[pos];
            textArea1.append("Removing client thread " + ID + " at " + pos  + "\n");
            textArea1.append("------------------" + "\n");
            if (pos < clientCount-1)
                for (int i = pos+1; i < clientCount; i++)
                    clients[i-1] = clients[i];
            clientCount--;
            try {
                toTerminate.close();
            }
            catch(IOException ioe) {
                textArea1.append("Error closing thread: " + ioe + "\n");
                textArea1.append("------------------" + "\n");
            }
            toTerminate.stop(); }
    }

    private void addThread(Socket socket) {
        if (clientCount < clients.length) {
            textArea1.append("Client accepted: " + socket + "\n");
            textArea1.append("------------------" + "\n");
            clients[clientCount] = new ChatServerThread(this, socket);
            try {
                clients[clientCount].open();
                clients[clientCount].start();
                clientCount++;
            } catch(IOException ioe) {
                textArea1.append("Error opening thread: " + ioe + "\n");
                textArea1.append("------------------" + "\n");
            }
        } else {
            textArea1.append("Client refused: maximum " + clients.length + " reached." + "\n");
            textArea1.append("------------------" + "\n");
        }
    }

    public ChatServerThread[] getClients() {
        return clients;
    }

    public static void main(String[] args) {
        /*ChatServer server = null;
        if (args.length != 1)
            System.out.println("Usage: java ChatServer port");
        else server = new ChatServer(5000);*/

        JFrame mainFrame=new JFrame("Server Control");
        mainFrame.setPreferredSize(new Dimension(460, 370));
        mainFrame.setContentPane(new ChatServer(5000).panelMain);
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (JOptionPane.showConfirmDialog(mainFrame, "Are you sure you want to close server?", "Close Server?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    removeFolder("src/data/");
                    System.exit(0);
                }
            }
        });
        mainFrame.pack();
        mainFrame.setVisible(true);

    }

    public void createFolder(String path) {
        File dir = new File(path);
        dir.mkdir();
    }

    public static void removeFolder(String path) {
        Path directory = Paths.get(path);
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    Files.delete(file); // this will work because it's always a File
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir); //this will work because Files in the directory are already deleted
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createFile(String path) {
        File file = new File(path + ".txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeLineToFile(path + ".txt", "[Chat body]");
    }

    public void removeFile(String path) {
        File file=new File(path);
        file.delete();
    }

    public void writeLineToFile(String path, String data) {
        try {
            BufferedWriter bw= new BufferedWriter(new FileWriter(new File(path), true));
            PrintWriter out=new PrintWriter(bw);
            out.println(data);
            out.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshFile(String path) {
        createFile(path);
    }
}