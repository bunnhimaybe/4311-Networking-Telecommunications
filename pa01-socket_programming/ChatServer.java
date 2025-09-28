import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatServer {
    private Socket skt;
    private ServerSocket serverSkt;
    private DataInputStream input;
    private PrintWriter output;
    private int port; 

    public ChatServer(int port){

        try {

            // setup
            serverSkt = new ServerSocket(port); 
            System.out.println("Server running.");
            
            // client connections
            skt = serverSkt.accept();
            System.out.println("Connection made.");

            // read messages
            input = new DataInputStream(
                new BufferedInputStream(skt.getInputStream()) );

            // send messages 
            output = new PrintWriter(skt.getOutputStream(), true);
                        
            // sign-in

            // message disconnect 
            String msg = "";
            while (!(msg == "Bye")) {
                msg = input.readUTF();
            }

            // disconnect
            System.out.println("Client connection closed.");
            skt.close();
            input.close();
            output.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred with the server.");
        }
    }

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: java ChatServer <port>");
            return;
        } 
        
        try {
            int port = Integer.parseInt(args[0]);
            ChatServer chatServer = new ChatServer(port);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.out.println("Invalid argument: port must be a number.");
        }

        }
    }
}