import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;


public class ChatServer {

    private ServerSocket server;
    private Map<String, ClientHandler> clients;
    private BlockingQueue<String> msgQueue; // shared
    private ExecutorService threadPool;


    // start server & delegate tasks
    public ChatServer(int port){
        try {
            server = new ServerSocket(port);
            System.out.println("Server running.");
            clients = new ConcurrentHashMap<>();
            msgQueue = new LinkedBlockingQueue<String>();
            threadPool = Executors.newFixedThreadPool(20);

            Dispatcher dispatcher = new Dispatcher(clients, msgQueue);
            threadPool.submit(dispatcher);

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }


    // listen for clients
    public void acceptClients() {
        try {
            while (true) {
                Socket connection = server.accept();
                ClientHandler client = new ClientHandler(connection, this);
                threadPool.submit(client);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }


    // check if username is already registered
    public Boolean checkUsernameTaken(String username) {
        return clients.containsKey(username);
    }


    // add username & ClientHandler after validation
    public void addClient(String username, ClientHandler client){
        clients.put(username, client);
        System.out.println(username + " connected.");
        try{
            msgQueue.put("Welcome, " + username + "!");
        } catch (InterruptedException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }


    // deregister client and disconnect
    public void removeClient(String username, ClientHandler client) {
        clients.remove(username, client);
        System.out.println(username + " disconnected.");
        dispatchMsg(username + " disconnected.");
    }


    // queue message to be broadcasted
    public void dispatchMsg(String msg) {
        try {
            msgQueue.offer(msg);
        } catch (NullPointerException e) {
            System.err.println("Server error: " + e.getMessage());
        } 
    }


    // release resources
    public void shutdown() {
        try {
            if (server != null && !server.isClosed()) {
                server.close();
            }
            if (threadPool != null && !threadPool.isShutdown()) {
                threadPool.shutdownNow();
            }
            System.out.println("Server shutdown complete.");
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }


    // create and start server
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java ChatServer <port>");
            return;
        }

        try {
            int port = Integer.parseInt(args[0]);
            ChatServer chatServer = new ChatServer(port);
            chatServer.acceptClients();

        } catch (NumberFormatException e) {
            System.err.println("Invalid argument: port must be a number.");
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}



// thread: manage single client's input/output
class ClientHandler implements Runnable{

    private Socket clientSkt;
    private ChatServer server;
    private DataInputStream input;
    private DataOutputStream output;
    String username;


    // initialize variables
    public ClientHandler(Socket connection, ChatServer server) {
        this.clientSkt = connection;
        this.server = server;

        try {
            input = new DataInputStream(clientSkt.getInputStream()) ;
            output = new DataOutputStream(clientSkt.getOutputStream());
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }


    // choose a valid username and register client
    public void register(){
        Boolean valid = false;
        try {
            do {
                username = input.readUTF().trim();
                if (username.isEmpty()) {
                    output.writeUTF("Username cannot be empty.\n");
                    output.flush();
                } else if (server.checkUsernameTaken(username)) {
                    output.writeUTF("Username is taken.");
                    output.flush();
                } else {
                    valid = true;
                }
                output.writeUTF(valid.toString());
                output.flush();
            } while (!valid);
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }

        server.addClient(username, this);
    }

    // send message directly to client
    public void sendToClient(String msg) {
        try {
            output.writeUTF(msg);
            output.flush();
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }


    // read from client socket to message queue
    public Boolean receiveFromClient() {
        try {
            String line = "";
            do {
                line = input.readUTF();
                server.dispatchMsg(username + ": " + line);
            } while (!line.equals("Bye"));

            // begin disconnecting from server
            server.removeClient(username, this);
            shutdown();
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
        return false;
    }

    public void shutdown() {
        try {
            clientSkt.close();
            input.close();
            output.close();
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public void run() {
        register();
        receiveFromClient();
    }
}


// broadcasts messages
class Dispatcher implements Runnable { 

    private Map<String, ClientHandler> clients;
    private BlockingQueue<String> msgQueue; 


    // initialize
    public Dispatcher(Map<String, ClientHandler> clients, BlockingQueue<String> msgQueue) {
        this.clients = clients;
        this.msgQueue = msgQueue;
    }


    public void run() {
        while (true) {
            try {
                String msg = msgQueue.take();
                for (ClientHandler client : clients.values()) {
                    client.sendToClient(msg);
                }
            } catch (InterruptedException e) {
                System.err.println("Server error: " + e.getMessage());
            }
        }
    }
}