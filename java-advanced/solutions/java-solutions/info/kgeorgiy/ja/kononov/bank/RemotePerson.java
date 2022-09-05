package info.kgeorgiy.ja.kononov.bank;

import java.rmi.RemoteException;

public class RemotePerson implements Person {
    private final String firstName;
    private final String secondName;
    private final int passportId;

    public RemotePerson(String firstName, String secondName, int passportId) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.passportId = passportId;
    }

    @Override
    public int getId() throws RemoteException {
        return passportId;
    }

    @Override
    public String getFirstName() throws RemoteException {
        return firstName;
    }

    @Override
    public String getSecondName() throws RemoteException {
        return secondName;
    }
}
