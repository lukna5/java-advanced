package info.kgeorgiy.ja.kononov.bank;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;

public class LocalPerson implements Person, Serializable {
    private final String firstName;
    private final String secondName;
    private final int passportId;
    private final Map<String, Account> accountMap;

    public LocalPerson(String firstName, String secondName, int passportId, Map<String, Account> accountMap) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.passportId = passportId;
        this.accountMap = accountMap;
    }

    public void addAcc(String id, Account account){
        accountMap.put(id, account);
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

    public Set<String> getAccounts(){
        return accountMap.keySet();
    }

    public Account getAccount(String id){
        return accountMap.get(id);
    }
}
