import java.net.*;
import java.io.*;

public class TCPServer {

    private static boolean createWorkingDirectory() {
        boolean fielExists;
        File tempFile;
        try {
            tempFile = new File("Server Folder");
            fielExists = tempFile.exists();
            if (!fielExists) {
                if (tempFile.mkdir()){
                    System.out.println("Server folder successfully created!");
                    return true;
                } else {
                    System.out.println("Could not create the server folder!");
                    return false;
                }
            } else {
                System.out.println("Folder already exists! No new folder necessary!");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not crete Server Folder!");
            return false;
        }
    }


    public static void main (String args[]) {

        if (createWorkingDirectory()){
            try{
                int serverPort = 12345;
                ServerSocket listenSocket = new ServerSocket(serverPort);
                System.out.println("Server "+InetAddress.getLocalHost().getHostName()+" is up and running on port "+serverPort+"!");
                while(true) {
                    Socket clientSocket = listenSocket.accept();
                    Connection c = new Connection(clientSocket);
                    System.out.println("New connection established");
                }
            } catch(IOException e) {System.out.println("Listen :"+e.getMessage());}
        }
    }
}
