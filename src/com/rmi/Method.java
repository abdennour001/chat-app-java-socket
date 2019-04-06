package com.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Method extends Remote {

    public boolean checkName(String string) throws RemoteException;
}
