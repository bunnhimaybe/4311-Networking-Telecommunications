import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Server {
    private Socket skt;
    private ServerSocket server;
    private DataInputStream input;
    private PrintWriter output;
    private static final int PORT = 5000;

    public Server(int port){
        try {
            // setup
            server = new ServerSocket(port); 
            System.out.println("Server running.");
            // connect to clients
            skt = server.accept();
            System.out.println("Connection made.");

            // send messages 
            output = new PrintWriter(skt.getOutputStream(), true);
            // read messages
            input = new DataInputStream(
                new BufferedInputStream(skt.getInputStream()) );
            
            // authentication


            String msg = "";
            while (!msg == "Bye") {
                msg = input.readUTF();
            }

            
            // disconnect
            System.out.println("Client connection closed.");
            skt.close();
            input.close();
            output.close();
        } catch (Exception e) {
            System.err.println("An error occurred with the server.");
            e.printStackTrace();
        }
    }

    public

    public static void main(String[] args) {
        Server server = new Server(PORT);
    }
}