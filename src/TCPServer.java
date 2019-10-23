import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class TCPServer {
    public static void main (String args[]) {
        try{
            int serverPort = 7896;
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
class Connection extends Thread {
    private Map<String, String> usersList = new HashMap<>();
    private BufferedReader in;
    private BufferedWriter out;
    private Socket clientSocket;
    public Connection (Socket aClientSocket) {
        try {
            clientSocket = aClientSocket;
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            this.start();
        } catch(IOException e) {System.out.println("Connection:"+e.getMessage());}
    }
    public void run(){
        String serverResponse, clientRequest="";
        do {
            try {           // an echo server
                String data = in.readLine();
                //System.out.println("data = "+data);
                if (data != null) {
                    serverResponse = respondToClient(data);
                    System.out.println("server response = "+serverResponse);
                    clientRequest = serverResponse.split(" ", 2)[0];
                    serverResponse = serverResponse + "\n";
                    out.write(serverResponse);
                    out.flush();
                }

            } catch (EOFException e) {
                System.out.println("EOF: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("IO: " + e.getMessage());
            }
        } while (!clientRequest.equals("Closed!"));

        try {
            clientSocket.close();
        } catch (IOException e) {/*close failed*/}

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
            case 5: return(summaryOfAFile(incomingData));
            case 6: return(requestSubsetOfAFile(incomingData));
            case 7: return(deleteFile(incomingData));
            case 8: return(closeConnection(incomingData));
        }
        return "Fail! Unknown Error!";
    }

    private String closeConnection(String[] incomingData) {
        return "Closed! Connection from "+incomingData[0]+" successfully closed!";
    }

    private String deleteFile(String[] incomingData) {
        return "";
    }

    private String requestSubsetOfAFile(String[] incomingData) {
        return "";
    }

    private String summaryOfAFile(String[] incomingData) {
        return "";
    }

    private String transferFile(String[] incomingData) {
        return "";
    }

    private String listFilesOnServer(String[] incomingData) {
        return "";
    }

    private String createFile(String[] incomingData) {

        return "Success! File "+incomingData[2]+".txt created!";
    }

    private String registerClient(String[] incomingData) {
        usersList.put(incomingData[2], incomingData[3]);
        String message="";
        try {
            message = "Success! User "+incomingData[0]+" registered to "+InetAddress.getLocalHost().getHostName()+" server!";
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return message;
    }
}