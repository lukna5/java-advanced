package info.kgeorgiy.ja.kononov.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface    Bank extends Remote {
    /**
     * Creates a new account with specified identifier if it does not already exist.
     *
     * @param id account id
     * @return created or existing account.
     */
    Account createAccount(final String id, Person person) throws RemoteException;

    /**
     * Returns account by identifier.
     *
     * @return account with specified identifier or {@code null} if such account does not exist.
     */
    boolean containsPerson(String firstName, String secondName, int passportId) throws RemoteException;
    Person createPerson(String firstName, String secondName, int passportId) throws RemoteException;
    Account getAccount(String id, Person person) throws RemoteException;

    LocalPerson getLocalPerson(int passport) throws RemoteException;

    RemotePerson getRemotePerson(int passport) throws RemoteException;

    Set<String> getPersonsAccounts(Person person) throws RemoteException;

}
