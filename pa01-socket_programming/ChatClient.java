import java.io.*;
import java.net.*;

public class ChatClient {
    
    Socket skt;
    BufferedReader userInput;
    DataInputStream receiveServer;
    DataOutputStream sendServer;
    String username;


    public ChatClient(String serverAddress, int serverPort){
        
        // establish connection 
        try {
            skt = new Socket(serverAddress, serverPort);
            userInput = new BufferedReader(new InputStreamReader(System.in));
            receiveServer = new DataInputStream(skt.getInputStream());
            sendServer = new DataOutputStream(skt.getOutputStream());

            
            // prompt for username
            String response = "";
            System.out.print("Enter your username: "); 
            do {
                username = userInput.readLine().trim();
                sendServer.writeUTF(username);
                response = receiveServer.readUTF();
            } while (!response.equals("true"));


            // main thread: send messages to server
            Thread sendThread = new Thread( () -> {
                try {
                    String line = ""; 
                    do {
                        line = userInput.readLine();
                        sendServer.writeUTF(line);
                    } while ( !(line.equals("Bye")) );
                } catch (IOException e1) {
                    System.err.println("Client error: " + e1.getMessage());
                }
            });  
            sendThread.start();


            // thread: read messages from server
            Thread readThread = new Thread( () -> {
                try {
                    String line;
                    do {
                        line = receiveServer.readUTF();
                        System.out.println(line);
                    } while ( !(line.equals("Bye")) );
                } catch (IOException e1) {
                    if (e1.getMessage() == null) {
                        System.out.println("Connection closed by server.");
                    } else {
                        System.err.println("Client error: " + e1.getMessage());
                    }
                }
            });
            readThread.start();


            // synchronize threads
            sendThread.join();
            readThread.join();

            // end connection
            skt.close();
            userInput.close();
            receiveServer.close();
            sendServer.close();
            System.out.println("Disconnected.");

        } catch (IOException e2) {
            System.err.println("Client error: " + e2.getMessage());
        } catch (InterruptedException e2) {
            System.err.println("Client error: " + e2.getMessage());
        }
    }


    // entry point
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java ChatClient <serverIP> <serverPort#>");
            return;
        } 
        
        try {
            String address = args[0];
            int port = Integer.parseInt(args[1]);
            ChatClient chatClient= new ChatClient(address, port);
        } catch (NumberFormatException e) {
            System.err.println("Invalid argument: port must be a number.");
        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}