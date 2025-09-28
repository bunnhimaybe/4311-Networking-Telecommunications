import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    
    Socket skt;
    DataInputStream input;
    DataOutputStream output;
    private int port = 5000;

    public void Client(String address, int port) {
        try {
            skt = new Socket(address, port);
            System.out.println("Client connected to server.");

            input = new DataInputStream(System.in);
            output = new DataOutputStream(skt.getOutputStream());

            login();

        } catch (Exception e) {
            System.err.println("An error occured with the client.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        
        Client client = new Client("localhost", PORT);
    }
}
