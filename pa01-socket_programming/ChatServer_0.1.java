/**
 * @author Nhi Pham
 */

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;



/**
 * Create server socket, listen for clients, delegate tasks.
 */
public class ChatServer {
    private ServerSocket serverSkt; 
    private List<ClientHandler> clients;
    private BlockingQueue<String> msgQueue;
    
    public ChatServer(int port) {
        try {
            serverSkt = new ServerSocket(port);
            System.out.println("Server running.");
            clients = new ArrayList<>();
            msgQueue = new LinkedBlockingQueue<String>();

            // Dispatcher consumes and broadcast messages
            Dispatcher dispatcher = new Dispatcher(this);
            new Thread(dispatcher).start();

            // accept clients & handle with ClientHandler
            while (true) {
                Socket connection = serverSkt.accept();
                ClientHandler client = new ClientHandler(connection, this);
                clients.add(client);

                Thread thr = new Thread(client);
                thr.start();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            System.err.println("Server error: " + e1.getMessage());
        }
    }


    /**
     * Disconnects client.
     */
    public ClientHandler removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Client disconnected.");
        return client;
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



/**
 * Handles input/output for one client socket and produces messages.
 */
class ClientHandler implements Runnable {
    private Socket clientSkt;
    private ChatServer server;
    private DataInputStream input;
    private DataOutputStream output; 
    private BlockingQueue<String> msgQueue;
    // receive sockets
    public ClientHandler(Socket socket, ChatServer server, 
        BlockingQueue<String> msgQueue) {

        this.clientSkt = socket;
        this.server = server;
        this.msgQueue = msgQueue;
    }

    /**
     * Adds message to queue.
     */
    public void sendMsg(String message) {
        msgQueue.offer(message);
    }

    /**
     * Handle client input/output
     */
    public void run() {
        try {            
            input = new DataInputStream( 
                new BufferedInputStream(clientSkt.getInputStream()) );
            output = new DataOutputStream( 
                new BufferedOutputStream(clientSkt.getOutputStream()) );
            
            // login/welcome message
            output.writeUTF("Welcome to the chat server!");
            output.writeUTF("Please enter a username.");

            // read messages
            String line = "";
            while (!line.equals("Bye")) {
                line = input.readUTF();
                sendMsg(line);
            }
            
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
            server.removeClient(this);
        }
    }
}



/**
 * Pushes messages from the queue to the dispatcher and consumes messages.
 */
class Dispatcher implements Runnable {
    BlockingQueue<String> msgQueue;
    
    public void run() {

    }
}