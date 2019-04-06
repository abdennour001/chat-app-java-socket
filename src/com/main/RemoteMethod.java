package com.main;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteMethod extends UnicastRemoteObject implements com.rmi.Method {

    public RemoteMethod() throws RemoteException {
    }

    @Override
    public boolean checkName(String userName) throws RemoteException {

        return true;
    }

}
