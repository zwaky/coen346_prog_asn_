package ca.concordia.server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ClientServiceThread extends Thread {
    private Socket connectionSocket; // Client's connection socket
    private ArrayList<ClientServiceThread> clients; // List of all clients connected to the server. Do we really want
                                                    // this? Privacy?
    private DataOutputStream outToClient;

    public ClientServiceThread(Socket connectionSocket, ArrayList<ClientServiceThread> clients) {
        this.connectionSocket = connectionSocket;
        this.clients = clients;
        try {
            this.outToClient = new DataOutputStream(connectionSocket.getOutputStream());

        } catch (IOException e) {
            System.out.println("Error getting output stream: " + e.getMessage());
        }
    }

    public void run() {
        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

            String clientSentence;

            while (true) {

                // Get input from client
                clientSentence = inFromClient.readLine();

                if (clientSentence != null) {

                    if (clientSentence.startsWith("GET")) {
                        // Handle GET request
                        handleGetRequest(outToClient);
                    } else if (clientSentence.startsWith("POST")) {
                        // Handle POST request
                        handlePostRequest(inFromClient, outToClient);
                    }

                    // Disconnect client at the end of the request
                    this.disconnectClient();

                } else {
                    // Client disconnected. Input is NULL
                    this.disconnectClient();
                    break;
                }
            }

        } catch (IOException ex) {
            this.disconnectClient();
        }
    }

    public void disconnectClient() {
        try {
            outToClient.close();
            connectionSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing the connection socket for client ");
        }

        WebServer.Clients_lock.lock();
        try {
            clients.remove(this);
        } finally {
            WebServer.Clients_lock.unlock();

        }
    }

    private static void handleGetRequest(OutputStream out) throws IOException {
        // Respond with a basic HTML page
        System.out.println("Handling GET request");
        String response = "HTTP/1.1 200 OK\r\n\r\n" +
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<title>Concordia Transfers</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<h1>Welcome to Concordia Transfers</h1>\n" +
                "<p>Select the account and amount to transfer</p>\n" +
                "\n" +
                "<form action=\"/submit\" method=\"post\">\n" +
                "        <label for=\"account\">Account:</label>\n" +
                "        <input type=\"text\" id=\"account\" name=\"account\"><br><br>\n" +
                "\n" +
                "        <label for=\"value\">Value:</label>\n" +
                "        <input type=\"text\" id=\"value\" name=\"value\"><br><br>\n" +
                "\n" +
                "        <label for=\"toAccount\">To Account:</label>\n" +
                "        <input type=\"text\" id=\"toAccount\" name=\"toAccount\"><br><br>\n" +
                "\n" +
                "        <label for=\"toValue\">To Value:</label>\n" +
                "        <input type=\"text\" id=\"toValue\" name=\"toValue\"><br><br>\n" +
                "\n" +
                "        <input type=\"submit\" value=\"Submit\">\n" +
                "    </form>\n" +
                "</body>\n" +
                "</html>\n";
        out.write(response.getBytes());
        out.flush();
    }

    private static void handlePostRequest(BufferedReader in, OutputStream out) throws IOException {
        System.out.println("Handling post request");
        StringBuilder requestBody = new StringBuilder();
        int contentLength = 0;
        String line;

        // Read headers to get content length
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            if (line.startsWith("Content-Length")) {
                contentLength = Integer.parseInt(line.substring(line.indexOf(' ') + 1));
            }
        }

        // Read the request body based on content length
        for (int i = 0; i < contentLength; i++) {
            requestBody.append((char) in.read());
        }

        System.out.println(requestBody.toString());
        // Parse the request body as URL-encoded parameters
        String[] params = requestBody.toString().split("&");
        String sourceAccountId = null, sourceValue = null, destinationAccountId = null, destinationValue = null;

        for (String param : params) {
            String[] parts = param.split("=");
            if (parts.length == 2) {
                String key = URLDecoder.decode(parts[0], "UTF-8");
                String val = URLDecoder.decode(parts[1], "UTF-8");

                switch (key) {
                    case "account":
                        sourceAccountId = val;
                        break;
                    case "value":
                        sourceValue = val;
                        break;
                    case "toAccount":
                        destinationAccountId = val;
                        break;
                    case "toValue":
                        destinationValue = val;
                        break;
                }
            }
        }
        // Process deposit and withdrawal
        if (sourceAccountId != null && sourceValue != null && destinationAccountId != null) {
            int sourceAccountIdInt = Integer.parseInt(sourceAccountId);
            int sourceValueInt = Integer.parseInt(sourceValue);
            int destinationAccountIdInt = Integer.parseInt(destinationAccountId);
            // int destinationValueInt = Integer.parseInt(destinationValue);

            AccountManager accountManager = new AccountManager();

            if (sourceAccountId.equals(destinationAccountId) // Ensure you aren't sending to same account
                    || accountManager.getBalance(sourceAccountIdInt) <= sourceValueInt // Ensure sufficient
                    || accountManager.findAccountById(destinationAccountIdInt) == null // Make sure the account exists
                    || accountManager.findAccountById(sourceAccountIdInt) == null // Make sure the account exists
            ) {
                // Invalid request
                String responseContent = "<html><body><h1>Invalid Request</h1></body></html>";

                // Respond with an error message
                String response = "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: " + responseContent.length() + "\r\n" +
                        "Content-Type: text/html\r\n\r\n" +
                        responseContent;

                out.write(response.getBytes());
                out.flush();
            } else {
                // Withdraw from source account
                accountManager.withdraw(sourceAccountIdInt, sourceValueInt);

                // Deposit to destination account
                accountManager.deposit(destinationAccountIdInt, sourceValueInt);

                // Save account changes to file
                accountManager.saveAccountsToFile();

                // Create the response
                String responseContent = "<html><body><h1>Transaction Successful</h1>" +
                        "<p>Withdrawn from Account " + sourceAccountIdInt + ": " + sourceValueInt + "</p>" +
                        "<p>Deposited to Account " + destinationAccountIdInt + ": " + sourceValueInt + "</p>" +
                        "</body></html>";

                // Respond with the transaction details
                String response = "HTTP/1.1 200 OK\r\n" +
                        "Content-Length: " + responseContent.length() + "\r\n" +
                        "Content-Type: text/html\r\n\r\n" +
                        responseContent;

                out.write(response.getBytes());
                out.flush();
            }

        } else {
            // Invalid request
            String responseContent = "<html><body><h1>Invalid Request</h1></body></html>";

            // Respond with an error message
            String response = "HTTP/1.1 400 Bad Request\r\n" +
                    "Content-Length: " + responseContent.length() + "\r\n" +
                    "Content-Type: text/html\r\n\r\n" +
                    responseContent;

            out.write(response.getBytes());
            out.flush();
        }
    }
}
