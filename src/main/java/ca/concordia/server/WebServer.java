package ca.concordia.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

//create the WebServer class to receive connections on port 5000. Each connection is handled by a master thread that puts the descriptor in a bounded buffer. A pool of worker threads take jobs from this buffer if there are any to handle the connection.
public class WebServer {

    // Creates an array that will contain a list of all connected clients
    public static ArrayList<ClientServiceThread> Clients = new ArrayList<ClientServiceThread>();

    // Lock to be used throughout the code
    public static final ReentrantLock Clients_lock = new ReentrantLock();

    public void start() throws java.io.IOException {

        // Create a server socket
        ServerSocket serverSocket = new ServerSocket(5000);

        // Create a new thread to keep listening for incoming clients
        new Thread(() -> acceptClients(serverSocket)).start();
    }

    private static void acceptClients(ServerSocket serverSocket) {
        // This function accepts clients and adds them to the list of connected clients.

        while (!serverSocket.isClosed()) {
            try {
                System.out.println("Waiting for a client to connect...");
                Socket clientSocket = serverSocket.accept();

                // Get the input and output stream of the client
                System.out.println("New client...");
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream out = clientSocket.getOutputStream();

                Clients_lock.lock();
                try {
                    // Create a new ClientServiceThread object
                    ClientServiceThread newClient = new ClientServiceThread(clientSocket, Clients);

                    // Add it to the list of clients
                    Clients.add(newClient);

                    // Create a new thread for the ClientServiceThread object that will handle POST
                    // and GET requests
                    newClient.start();
                } finally {
                    Clients_lock.unlock();
                }

            } catch (Exception ex) {
                System.out.println("Error accepting client connection: " + ex.getMessage());
            }

        }
    }

    private String processRequest(String request) {
        // Parse the request to extract relevant information
        // For simplicity, let's assume a basic format where the request contains details of the fund transfer

        // Example request format: "TRANSFER?sourceAccount=123&sourceValue=50&destinationAccount=456&destinationValue=50"

        String[] requestParts = request.split("\\?");
        if (requestParts.length != 2) {
            return "Invalid request";
        }

        String command = requestParts[0];
        String params = requestParts[1];

        if ("TRANSFER".equals(command)) {
            String[] paramPairs = params.split("&");
            int sourceAccount = 0, sourceValue = 0, destinationAccount = 0, destinationValue = 0;

            for (String paramPair : paramPairs) {
                String[] keyValue = paramPair.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];

                    switch (key) {
                        case "sourceAccount":
                            sourceAccount = Integer.parseInt(value);
                            break;
                        case "sourceValue":
                            sourceValue = Integer.parseInt(value);
                            break;
                        case "destinationAccount":
                            destinationAccount = Integer.parseInt(value);
                            break;
                        case "destinationValue":
                            destinationValue = Integer.parseInt(value);
                            break;
                    }
                }
            }

            AccountManager accountManager = new AccountManager();
            FundTransferProcessor transferProcessor = new FundTransferProcessor(accountManager);
            boolean transferResult = transferProcessor.processTransfer(sourceAccount, sourceValue, destinationAccount, destinationValue);

            if (transferResult) {
                return "Transfer successful";
            } else {
                return "Transfer failed";
            }
        } else {
            return "Invalid command";
        }
    }

    public static void main(String[] args) {
        // Start the server, if an exception occurs, print the stack trace
        WebServer server = new WebServer();
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Example: Simulating processing a request after the server has started
        String sampleRequest = "TRANSFER?sourceAccount=123&sourceValue=50&destinationAccount=456&destinationValue=50";
        server.processRequest(sampleRequest);

        // Optional: Add a delay or use a different mechanism to keep the program running
        try {
            Thread.sleep(5000); // Sleep for 5 seconds (adjust as needed)
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
