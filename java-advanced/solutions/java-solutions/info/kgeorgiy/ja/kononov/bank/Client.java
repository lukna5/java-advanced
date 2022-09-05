package info.kgeorgiy.ja.kononov.bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public final class Client {
    /**
     * Utility class.
     */

    public static void main(final String... args) throws RemoteException {
        final Bank bank;
        try {
            bank = (Bank) Naming.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        }

        final String accountId = args.length >= 1 ? args[0] : "geo";
        String firstName = args[1];
        String secondName = args[2];
        int passportId = Integer.parseInt(args[3]);
        int adding = Integer.parseInt(args[4]);
        Person person = bank.getRemotePerson(passportId);
        if (person == null) {
            System.out.println("Created account with id: " + passportId);
            person = bank.createPerson(firstName, secondName, passportId);
        }
        Account account = bank.getAccount(accountId, person);
        if (account == null) {
            System.out.println("Creating account:");
            account = bank.createAccount(accountId, person);
        } else {
            System.out.println("Account exists");
        }
        printInfoAdding(account, adding);
    }

    private static void printInfoAdding(Account account, int adding) {
        try {
            System.out.println(account.getInfo());
            System.out.println("Adding money");
            account.setAmount(account.getAmount() + adding);
            System.out.println("Money: " + account.getAmount());
        } catch (RemoteException e) {
            System.out.println("Can't get info about this account");
        }
    }
}
