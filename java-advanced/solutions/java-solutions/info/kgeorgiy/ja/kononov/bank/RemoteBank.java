package info.kgeorgiy.ja.kononov.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Person> persons = new ConcurrentHashMap<>();

    private final ConcurrentMap<Integer, Set<String>> hostOfAccounts = new ConcurrentHashMap<>();


    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public Account createAccount(final String id, Person person) throws RemoteException {
        // check
        Integer passportId = person.getId();
        final Account account = new RemoteAccount(id);
        if (accounts.putIfAbsent(passportId + ":" + id, account) == null) {
            if (!hostOfAccounts.containsKey(passportId)) {
                hostOfAccounts.put(passportId, new ConcurrentSkipListSet<>());
            }
            UnicastRemoteObject.exportObject(account, port);
            hostOfAccounts.get(passportId).add(id);
            if (person instanceof LocalPerson) {
                ((LocalPerson) person).addAcc(id, account);
            }
            System.out.println("Creating account by id " + id);
            return account;
        } else {
            // already have
            return null;
        }
    }

    @Override
    public boolean containsPerson(String firstName, String secondName, int passportId) throws RemoteException {
        if (firstName == null || secondName == null || passportId < 0) {
            return false;
        }

        Person res = persons.get(passportId);
        return res.getFirstName().equals(firstName) && res.getSecondName().equals(secondName);
    }

    @Override
    public Person createPerson(String firstName, String secondName, int passportId) throws RemoteException {
        // check
        Person person = persons.get(passportId);
        if (person != null) {
            return null;
        }
        person = new RemotePerson(firstName, secondName, passportId);
        try {
            persons.put(passportId, person);
            hostOfAccounts.put(passportId, new ConcurrentSkipListSet<>());
            UnicastRemoteObject.exportObject(person, port);
            return person;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Account getAccount(final String id, Person person) throws RemoteException {
        if (id == null || person == null) {
            return null;
        }
        Account account = accounts.get(person.getId() + ":" + id);
        if (account == null) {
            return null;
        }
        if (person instanceof LocalPerson) {
            return ((LocalPerson) person).getAccount(id);
        }
        System.out.println("Retrieving account " + id);
        return account;
    }


    @Override
    public LocalPerson getLocalPerson(int passport) throws RemoteException {
        Person person = persons.get(passport);
        if (person == null) {
            return null;
        }
        Map<String, Account> localAccounts = getCopyPersonAccountMap(person);
        return new LocalPerson(person.getFirstName(), person.getSecondName(), person.getId(), localAccounts);
    }

    public Map<String, Account> getCopyPersonAccountMap(Person person) throws RemoteException {
        Map<String, Account> map = new ConcurrentHashMap<>();
        getPersonsAccounts(person).forEach(id -> {
            try {
                Account account = getAccount(id, person);
                map.put(id, new RemoteAccount(account.getId(), account.getAmount()));
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
        return map;
    }

    @Override
    public RemotePerson getRemotePerson(int passport) throws RemoteException {
        if (passport < 0 || !persons.containsKey(passport)) {
            return null;
        }
        return (RemotePerson) persons.get(passport);
    }

    @Override
    public Set<String> getPersonsAccounts(Person person) throws RemoteException {
        if (person == null) {
            return null;
        }
        if (person instanceof LocalPerson) {
            return ((LocalPerson) person).getAccounts();
        }
        return hostOfAccounts.get(person.getId());
    }
}
