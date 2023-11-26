package ca.concordia.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AccountManager {

    public static Map<String, Integer> loadAccounts() {
        Map<String, Integer> accounts = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("accounts"))) { // Provide the path to your
                                                                                       // accounts file
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");
                if (parts.length == 2) {
                    String accountID = parts[0];
                    int balance = Integer.parseInt(parts[1]);
                    accounts.put(accountID, balance);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return accounts;
    }
}
