import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TCPServer {
    public static void main (String args[]) {
        try{
            int serverPort = 7896;
            ServerSocket listenSocket = new ServerSocket(serverPort);
            System.out.println("Server "+InetAddress.getLocalHost().getHostName()+" is up and running!");
            while(true) {
                Socket clientSocket = listenSocket.accept();
                Connection c = new Connection(clientSocket);
                System.out.println("New connection established");
            }
        } catch(IOException e) {System.out.println("Listen :"+e.getMessage());}
    }
}
class Connection extends Thread {
    Map<String, String> usersList = new HashMap<>();
    private DataInputStream in;
    private DataOutputStream out;
    private Socket clientSocket;
    public Connection (Socket aClientSocket) {
        try {
            clientSocket = aClientSocket;
            in = new DataInputStream( clientSocket.getInputStream());
            out =new DataOutputStream( clientSocket.getOutputStream());
            this.start();
        } catch(IOException e) {System.out.println("Connection:"+e.getMessage());}
    }
    public void run(){
        String serverResponse;
        try {           // an echo server
            String data = in.readUTF();
            serverResponse = respondToClient(data);
            out.writeUTF(serverResponse);
        } catch(EOFException e) {System.out.println("EOF: "+e.getMessage());
        } catch(IOException e) {System.out.println("IO: "+e.getMessage());
        } finally { try {clientSocket.close();}catch (IOException e){/*close failed*/}}
    }

    private String respondToClient(String data) {
        String[] incomingData = data.split("_");
        int clientChoice = Integer.parseInt(incomingData[1]);
        System.out.println("clientChoice = "+clientChoice);
        if ((clientChoice<0) || (clientChoice>8)) {
            return "Failed - No such an option available to process on Server";
        }
        switch (clientChoice) {
            case 1: return(registerClient(incomingData));
            case 2: return(createFile(incomingData));
            case 3: return(listFilesOnServer(incomingData));
            case 4: return(transferFile(incomingData));
            case 5: return(sumarryOfAFile(incomingData));
            case 6: return(requestSubsetOfAFile(incomingData));
            case 7: return(deleteFile(incomingData));
            case 8: return(closeConnection(incomingData));
        }
        return "Fail! Unknown Error!";
    }

    private String closeConnection(String[] incomingData) {
        return "";
    }

    private String deleteFile(String[] incomingData) {
        return "";
    }

    private String requestSubsetOfAFile(String[] incomingData) {
        return "";
    }

    private String sumarryOfAFile(String[] incomingData) {
        return "";
    }

    private String transferFile(String[] incomingData) {
        return "";
    }

    private String listFilesOnServer(String[] incomingData) {
        return "";
    }

    private String createFile(String[] incomingData) {
        return "";
    }

    private String registerClient(String[] incomingData) {
        usersList.put(incomingData[2], incomingData[3]);
        String message="";
        try {
            message = "Success! User "+incomingData[0]+" registered to "+InetAddress.getLocalHost().getHostName()+" server!";
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println("register message = "+message);
        return message;
    }
}