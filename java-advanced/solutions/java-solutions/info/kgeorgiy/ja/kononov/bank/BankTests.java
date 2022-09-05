package info.kgeorgiy.ja.kononov.bank;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)

public class BankTests {
    private static Bank bank;

    private static Registry registry;

    private static final int PORT = 8888;

    private final int passportId = 12345;
    private final String name = "Ivan";
    private final String surname = "Petrov";
    private final String accountName = "MyAccount";


    @BeforeClass
    public static void beforeAll() throws RemoteException {
        registry = LocateRegistry.createRegistry(PORT);
    }

    @Before
    public void beforeEach() throws RemoteException {
        bank = new RemoteBank(PORT);
        Bank stub = (Bank) UnicastRemoteObject.exportObject(bank, PORT);
        registry.rebind("bank", stub);
    }


    @Test
    public void createAndCheckAccounts() throws RemoteException {
        for (int p = 0; p < 20; p++) {
            bank.createPerson(name + p, surname + p, passportId + p);
            Person remotePerson = bank.getRemotePerson(passportId + p);
            for (int i = 0; i < 50; i++) {
                Account account = bank.createAccount(Integer.toString(i), remotePerson);
                account.setAmount(i * 100 + 200);
            }
            for (int i = 0; i < 50; i++) {
                Account account = bank.getAccount(Integer.toString(i), remotePerson);
                assertNotNull(account);
                assertEquals(account.getAmount(), i * 100 + 200);
            }
        }
    }

    @Test
    public void createPersons() throws RemoteException {
        for (int i = 0; i < 100; i++) {
            Person person = bank.createPerson(name, surname, passportId);
            assertTrue(bank.containsPerson(name, surname, passportId));
        }
    }

    @Test
    public void checkLocalPersonAccounts() throws RemoteException {
        bank.createPerson(name, surname, passportId);
        Person firstLocalPerson = bank.getLocalPerson(passportId);
        Person secondLocalPerson = bank.getLocalPerson(passportId);
        Account account = bank.createAccount(Integer.toString(passportId), firstLocalPerson);
        account.setAmount(1);
        account = bank.createAccount(Integer.toString(passportId), secondLocalPerson);
        assertNull(account);
    }

    @Test
    public void testMultiThread() {
        ExecutorService service = Executors.newFixedThreadPool(20);
        service.submit(() -> {
            try {
                createPersons();
                settingAmount();
            } catch (RemoteException e) {
                Assert.fail(e.toString());
            }
        });
        service.shutdown();
        try {
            if (!service.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                service.shutdownNow();
            }
        } catch (InterruptedException e) {
            Assert.fail(e.toString());
        }
        service.shutdownNow();
    }

    @Test
    public void settingAmount() throws RemoteException {
        Person person = bank.createPerson(name, surname, passportId);
        int sum = 0;
        bank.createAccount("acc", person);
        for (int i = 0; i < 100; i++) {
            sum += i;
            Account account = bank.getAccount("acc", person);
            account.setAmount(account.getAmount() + i);
            assertEquals(account.getAmount(), sum);
        }
    }

    @Test
    public void checkGettingAccountByIdAndGettingPersonsAccount() throws RemoteException {
        Person person = bank.createPerson(name, surname, passportId);
        Account account1 = bank.createAccount("First", person);
        Account account2 = bank.createAccount("Second", person);
        account1.setAmount(1);
        account2.setAmount(2);
        Account accountGet1 = bank.getAccount("First", person);
        Account accountGet2 = bank.getAccount("Second", person);
        Iterator<String> iteratorSet = bank.getPersonsAccounts(person).iterator();
        Account accountFromSet1 = bank.getAccount(iteratorSet.next(), person);
        Account accountFromSet2 = bank.getAccount(iteratorSet.next(), person);
        assertEquals(account1.getAmount(), accountGet1.getAmount());
        assertEquals(account1.getAmount(), accountFromSet1.getAmount());
        assertEquals(account2.getAmount(), accountGet2.getAmount());
        assertEquals(account2.getAmount(), accountFromSet2.getAmount());
    }

}
