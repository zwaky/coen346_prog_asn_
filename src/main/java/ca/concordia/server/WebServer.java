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

    public static void main(String[] args) {
        // Start the server, if an exception occurs, print the stack trace
        WebServer server = new WebServer();
        try {
            server.start();
            
        } catch (IOException e) {
            e.printStackTrace();
        }      
    }
}
