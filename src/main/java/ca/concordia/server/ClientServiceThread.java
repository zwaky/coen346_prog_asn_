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

        // TODO Cannot use synchronized. Find another way to stop Clients from being
        // updated by multiple threads simultaneuously
        synchronized (clients) {
            clients.remove(this);
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
        String account = null, value = null, toAccount = null, toValue = null;

        for (String param : params) {
            String[] parts = param.split("=");
            if (parts.length == 2) {
                String key = URLDecoder.decode(parts[0], "UTF-8");
                String val = URLDecoder.decode(parts[1], "UTF-8");

                switch (key) {
                    case "account":
                        account = val;
                        break;
                    case "value":
                        value = val;
                        break;
                    case "toAccount":
                        toAccount = val;
                        break;
                    case "toValue":
                        toValue = val;
                        break;
                }
            }
        }

        // Create the response
        String responseContent = "<html><body><h1>Thank you for using Concordia Transfers</h1>" +
                "<h2>Received Form Inputs:</h2>" +
                "<p>Account: " + account + "</p>" +
                "<p>Value: " + value + "</p>" +
                "<p>To Account: " + toAccount + "</p>" +
                "<p>To Value: " + toValue + "</p>" +
                "</body></html>";

        // Respond with the received form inputs
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + responseContent.length() + "\r\n" +
                "Content-Type: text/html\r\n\r\n" +
                responseContent;

        out.write(response.getBytes());
        out.flush();
    }

}
