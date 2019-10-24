import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class TCPServer {

    private static boolean createWorkingDirectory() {
        boolean fielExists = true;
        File tempFile = null;
        try {
            tempFile = new File("Servers Folder");
            fielExists = tempFile.exists();
            System.out.println("File = " + tempFile + " exists? = " + fielExists + " and is a file? = " + tempFile.isFile());
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

    public void run() {
        String serverResponse, clientRequest = "";
        do {
            try {           // an echo server
                String data = in.readLine();
                //System.out.println("data = "+data);
                if (data != null) {
                    serverResponse = respondToClient(data);
                    System.out.println("server response = " + serverResponse);
                    clientRequest = serverResponse.split(" ", 2)[0];
                    serverResponse = serverResponse + "\n";
                    out.write(serverResponse);
                    out.flush();
                }

            } catch (EOFException e) {
                System.out.println("EOF: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("IO: " + e.getMessage());
                System.out.println("Closing connection!");
                break;
            }
        } while (!clientRequest.equals("Closed!"));

        try {
            clientSocket.close();
        } catch (IOException e) {
            /*close failed*/
            System.out.println("IO: " + e.getMessage());
            System.out.println("Could not close connection!");
        }
        System.out.println("Connection closed!");
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
        File folder = new File("Servers Folder");
        String[] files = folder.list();
        String message="";
        if ((files != null) && (files.length != 0)) {
            for (String file:files) {
                message=message+file+", ";
            }
            message = message.substring(0,message.length()-2)+"\n";
        } else {
            message = "No files present on the server!";
        }
        return message;
    }

    private String createFile(String[] incomingData) {
        String message;
        boolean fielExists=true;
        File tempFile=null;
        try  {
            //tempFile = new File("Server Folder"+File.separator+incomingData[2]+".txt");
            tempFile = new File("Servers Folder",incomingData[2]+".txt");
            fielExists = tempFile.exists();
            System.out.println("File = "+tempFile+" exists? = "+fielExists+" and is a file? = "+tempFile.isFile());

            if (!fielExists) {
                BufferedWriter writeFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Servers Folder"+File.separator+incomingData[2]+".txt"), "UTF-8"));
                //BufferedWriter writeFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8"));
                String [] randomWords = new String[]{"aaaaaaa", "bbbbbbb", "ccccccc", "ddddddd", "eeeeeee", "fffffff", "ggggggg", "hhhhhhh", "iiiiiii", "jjjjjjj"};

                for(int i = 0; i<10; i++) {
                    String line = "";
                    for (int j = 0; j<5; j++) {
                        int wrd = (int)(Math.random()*9);
                        line = j<4? line+randomWords[wrd]+" ":line+randomWords[wrd]+"\r\n";
                    }
                    writeFile.write(line);
                    writeFile.flush();
                }
                message = "Success! File "+incomingData[2]+".txt created!";
            } else {
                message = "Fail! Another file with name "+incomingData[2]+".txt already present. Please change the name!";
            }

        } catch (IOException e) {
            e.printStackTrace();
            message = "Fail! File "+incomingData[2]+".txt could not be created!";
        }

        return message;
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