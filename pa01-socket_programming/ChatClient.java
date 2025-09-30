import java.io.*;
import java.net.*;

public class ChatClient {
    
    Socket skt;
    DataInputStream input;
    DataOutputStream output;

    public ChatClient(String serverAddress, int serverPort) {
        try {
            skt = new Socket(serverAddress, serverPort);
            System.out.println("Client connected to server.");

            input = new DataInputStream(System.in);
            output = new DataOutputStream(skt.getOutputStream());

        } catch (Exception e) {
            System.err.println("An error occured with the client.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        
        ChatClient client = new ChatClient("localhost", 5000);
    }
}
