package info.kgeorgiy.ja.kononov.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Person extends Remote {
    int getId () throws RemoteException;
    String getFirstName() throws RemoteException;
    String getSecondName() throws RemoteException;
}
