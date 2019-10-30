import java.net.*;
import java.io.*;

public class TCPServer {

    //This method creates a folder on the server where all files created by the clients are stored
    private static boolean createWorkingFolder() {
        boolean fielExists;
        File tempFile;
        try {
            tempFile = new File("Server Folder");

            //It checks if the folder is present and only if not it creates it
            fielExists = tempFile.exists();
            if (!fielExists) {
                if (tempFile.mkdir()){
                    printMessageToScreenAndFile("Server folder successfully created!");
                    //System.out.println("Server folder successfully created!");
                    return true;
                } else {
                    printMessageToScreenAndFile("Could not create the server folder!");
                    //System.out.println("Could not create the server folder!");
                    return false;
                }
            } else {
                printMessageToScreenAndFile("Folder already exists! No new folder necessary!");
                //System.out.println("Folder already exists! No new folder necessary!");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            printMessageToScreenAndFile("Could not crete Server Folder!");
            //System.out.println("Could not crete Server Folder!");
            return false;
        }
    }


    //This method prints a messages to the screen and also saves it to the server journal
    private static void printMessageToScreenAndFile(String message) {
        System.out.println(message);
        String serverName = null;
        String filename;
        File tempFile;
        try {
            serverName = InetAddress.getLocalHost().getHostName();
            filename = "Server_"+serverName+"_Journal.txt";
            tempFile = new File(filename);
            try (BufferedWriter writeFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile, true), "UTF-8"));) {

                message = message+"\n";
                writeFile.write(message);
                writeFile.flush();

            } catch (IOException e) {
                System.out.println("Couldn't save the message into "+serverName+" client journal");
                e.printStackTrace();
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    public static void main (String args[]) {

        if (createWorkingFolder()){
            try{
                int serverPort = 12345;
                ServerSocket listenSocket = new ServerSocket(serverPort);
                printMessageToScreenAndFile("Server "+InetAddress.getLocalHost().getHostName()+" is up and running on port "+serverPort+"!");
                //System.out.println("Server "+InetAddress.getLocalHost().getHostName()+" is up and running on port "+serverPort+"!");
                while(true) {
                    Socket clientSocket = listenSocket.accept();
                    //For each client that tries to connect, a new Connection instance is created and a new thread starts
                    Connection c = new Connection(clientSocket);
                    printMessageToScreenAndFile("New connection established!");
                    //System.out.println("New connection established");
                }
            } catch(IOException e) {System.out.println("Listen :"+e.getMessage());}
        }
    }
}
