/**
 * @author Nhi Pham
 */

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;



public class ChatServer {

    private ServerSocket serverSkt; 
    private Map<String, ClientHandler> clients;
    private BlockingQueue<String> msgQueue;
    private List<Thread> threads;

    public ChatServer(int port) {
        try {
            serverSkt = new ServerSocket(port);
            System.out.println("Server running.");
            clients = new ConcurrentHashMap<>();
            msgQueue = new LinkedBlockingQueue<String>();
            threads = new ArrayList<Thread>();

            // dispatcher to broadcast messages
            Thread dispatcher = new Thread(() -> {
                try {
                    while (true) {
                        String msg = msgQueue.take();
                        broadcastMsg(msg);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            dispatcher.setDaemon(true);
            dispatcher.start();

            // accept clients & handle I/O with ClientHandler
            while (true) {
                Socket connection = serverSkt.accept();
                ClientHandler client = new ClientHandler(connection, this, msgQueue);

                Thread clientThread = new Thread(client);
                threads.add(clientThread);
                clientThread.start();
            }

        } catch (IOException e1) {
            e1.printStackTrace();
            System.err.println("Server error: " + e1.getMessage());
        }
    }


    public void removeClient(String username, ClientHandler client) {
        clients.remove(username);
        System.out.println("Client disconnected.");
    }
    
    public void addClient(String username, ClientHandler client) {
        clients.put(username, client);
        msgQueue.add("Welcome " + username + "!");
    }

    public boolean checkUsername(String username) {
        return clients.containsKey(username);
    }

    public void broadcastMsg(String msg) {
        for (ClientHandler client : clients.values()) {
            client.sendToClient(msg);
        }
    }


    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java ChatServer <port>");
            return;
        } 
        
        try {
            int port = Integer.parseInt(args[0]);
            ChatServer chatServer = new ChatServer(port);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.err.println("Invalid argument: port must be a number.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Server error: " + e.getMessage());
        }
    }
}



class ClientHandler implements Runnable {

    private Socket clientSkt;
    private ChatServer server;
    private DataInputStream input;
    private DataOutputStream output; 
    private BlockingQueue<String> msgQueue;
    private String username;
    

    public ClientHandler(Socket socket, ChatServer server, 
        BlockingQueue<String> msgQueue) {

        this.clientSkt = socket;
        this.server = server;
        this.msgQueue = msgQueue;
        this.username = null;
    }


    public void sendToClient(String msg) {
        try{
            output.writeUTF(msg);
            output.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
            System.err.println("An error occured with the server.");
        }
    }
    

    public void run() {
        try {
            input = new DataInputStream( 
                new BufferedInputStream(clientSkt.getInputStream()) );
            output = new DataOutputStream( 
                new BufferedOutputStream(clientSkt.getOutputStream()) );
            
            
            // login/welcome message

            sendToClient("Welcome to the chat server!");
            sendToClient("Please enter a username.");

            do {
                username = input.readUTF().trim();

                if (username.isEmpty()) {
                    output.writeUTF("Username cannot be empty. Please enter a username: ");
                    output.flush();
                } else if (server.checkUsername(username)) {
                    output.writeUTF("Username is taken. Please choose another: ");
                    output.flush();
                }
            } while (server.checkUsername(username) || username.isEmpty() );

            server.addClient(username, this);


            // read messages
            String line = "";
            do {
                line = input.readUTF();
                msgQueue.put(username + " has disconnected.");
            } while ( !(line.equals("Bye")) );
            


            input.close();
            output.close();

        } catch (IOException e2) {
            e2.printStackTrace();
            System.err.println("Server error: " + e2.getMessage());
        } catch (Exception e2) {
            e2.printStackTrace();
            System.err.println("Server error: " + e2.getMessage());
        } finally {
            // disconnect from server
            server.removeClient(username, this);
        }
    }
}