import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    
    Socket skt;
    DataInputStream input;
    DataOutputStream output;
    private static final int PORT = 5000;

    public Client(String address, int port) {
        try {
            skt = new Socket(address, port);
            System.out.println("Client connected to server.");

            input = new DataInputStream(System.in);
            output = new DataOutputStream(skt.getOutputStream());

            // prompt for username
            
        } catch (Exception e) {
            System.err.println("An error occured with the client.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        
        Client client = new Client("localhost", PORT);
    }
}
