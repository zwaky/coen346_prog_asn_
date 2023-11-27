package ca.concordia.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

public class AccountManager {
    private static final Logger LOGGER = Logger.getLogger(AccountManager.class.getName());
    private List<Account> accounts;
    private final Lock lock = new ReentrantLock();


    public AccountManager() {
        this.accounts = new CopyOnWriteArrayList<>();
        loadAccounts();
    }

    private void loadAccounts() {
        try (BufferedReader reader = new BufferedReader(new FileReader("src\\main\\java\\ca\\concordia\\server\\accounts"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        int accountId = Integer.parseInt(parts[0].trim());
                        int balance = Integer.parseInt(parts[1].trim());
                        accounts.add(new Account(balance, accountId));
                    }
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.SEVERE, "Error parsing account data: " + line, e);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading accounts file", e);
        }
    }

    public void saveAccountsToFile() {
        lock.lock();
        try {
            try (PrintWriter writer = new PrintWriter(new FileWriter("src\\main\\java\\ca\\concordia\\server\\accounts"))) {
                for (Account account : accounts) {
                    writer.println(account.getID() + "," + account.getBalance());
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error writing accounts to file", e);
            }
        } finally {
            lock.unlock();
        }
    }

    public int getBalance(int accountId) {
        Account account = findAccountById(accountId);
        return (account != null) ? account.getBalance() : -1; // Return -1 if the account doesn't exist
    }

    public void withdraw(int accountId, int amount) {
        Account account = findAccountById(accountId);
        if (account != null) {
            account.withdraw(amount);
        }
    }

    public void deposit(int accountId, int amount) {
        Account account = findAccountById(accountId);
        if (account != null) {
            account.deposit(amount);
        }
    }

    public Account findAccountById(int accountId) {
        for (Account account : accounts) {
            if (account.getID() == accountId) {
                return account;
            }
        }
        return null;
    }
}
