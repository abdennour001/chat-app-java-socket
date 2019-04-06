package com.main;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client extends Thread {
    private Socket socket;
    private String userName;
    ObjectInputStream in;
    ObjectOutputStream out;
    private int ID;
    private String time;
    
    public Client(Socket socket) {
        this.socket = socket;
        execute();
    }

    private void execute() {
        this.start();
    }

    @Override
    public void run() {
        try {
             in = new ObjectInputStream(socket.getInputStream());
             out = new ObjectOutputStream(socket.getOutputStream());
             ID = Method.addClient(this);
             // loop starting get message from client

        } catch (Exception e) {

        }
    }
}
