package ca.concordia.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AccountManager {
    private List<Account> accounts;

    public AccountManager() {
        this.accounts = loadAccounts();
    }

    private List<Account> loadAccounts() {
        List<Account> loadedAccounts = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("accounts"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    int accountId = Integer.parseInt(parts[0].trim());
                    int balance = Integer.parseInt(parts[1].trim());
                    loadedAccounts.add(new Account(balance, accountId));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return loadedAccounts;
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

    public static void main(String[] args) {
        AccountManager accountManager = new AccountManager();
        // Use accountManager methods to interact with accounts
    }
}
