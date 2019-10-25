import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

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
            try {
                String data = in.readLine();
                //System.out.println("data = "+data);
                if (data != null) {
                    serverResponse = respondToClient(data);
                    System.out.println("server response = " + serverResponse);
                    clientRequest = serverResponse.split(" ", 2)[0];
                    serverResponse = serverResponse+"\n";
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
        File fileToBeDeleted = new File("Server Folder",incomingData[2]);
        boolean fileExists = fileToBeDeleted.exists();

        if (fileExists) {
            boolean isFileDeleted = fileToBeDeleted.delete();
            if (isFileDeleted) {
                return "Success! File "+incomingData[2]+" successfully deleted from the server!";
            } else {
                return "Fail! File "+incomingData[2]+" couldn't be deleted from the server!";
            }

        } else {
            return "Fail! File "+incomingData[2]+" doesn't exist on the server!";
        }

    }

    private String requestSubsetOfAFile(String[] incomingData) {
        File fileToSendSeubset = new File("Server Folder",incomingData[2]);
        boolean fileExists = fileToSendSeubset.exists();

        if (fileExists) {

            try (BufferedReader readFile = new BufferedReader(new InputStreamReader(new FileInputStream(fileToSendSeubset), "UTF-8"))) {
                out.write("File exists! Begin sending a subset of it!\n");
                out.flush();
                String line;
                line = readFile.readLine();
                while ((line != null) && (line.length()>0)) {
                    int sendTheLine = (int)(Math.random()*4);
                    if (sendTheLine == 2) {
                        out.write(line);
                        out.flush();
                        out.write("\n");
                        out.flush();
                    }
                    line = readFile.readLine();
                }
                line="\n";
                out.write(line);
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            return "Fail! File "+incomingData[2]+" doesn't exist on the server!";
        }


        return "Success! File transferred!";
    }

    private String summaryOfAFile(String[] incomingData) {
        File fileForSummary = new File("Server Folder",incomingData[2]);
        boolean fileExists = fileForSummary.exists();

        if (fileExists) {
            int numberOfLines=0;
            int numberOfWords=0;

            try (BufferedReader readFile = new BufferedReader(new InputStreamReader(new FileInputStream(fileForSummary), "UTF-8"))) {

                String line;
                while ((line=readFile.readLine()) != null) {
                    numberOfLines++;
                    String[] words = line.split(" ");
                    numberOfWords = numberOfWords + words.length;
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String message = "Success! This is the requested summary: file "+incomingData[2]+" has "+numberOfLines+" lines, and "+numberOfWords+" words!";
            return message;

        } else {
            return "Fail! File "+incomingData[2]+" doesn't exist on the server!";
        }

    }

    private String transferFile(String[] incomingData) {
        File fileToBeTransfered = new File("Server Folder",incomingData[2]);
        boolean fileExists = fileToBeTransfered.exists();

        if (fileExists) {

            try (BufferedReader readFile = new BufferedReader(new InputStreamReader(new FileInputStream(fileToBeTransfered), "UTF-8"))) {
                out.write("File exists! Begin transfer!\n");
                out.flush();
                String line;
                line = readFile.readLine();
                while ((line != null) && (line.length()>0)) {
                    out.write(line);
                    out.flush();
                    out.write("\n");
                    out.flush();
                    line = readFile.readLine();
                }
                line="\n";
                out.write(line);
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            return "Fail! File "+incomingData[2]+" doesn't exist on the server!";
        }


        return "Success! File transferred!";
    }

    private String listFilesOnServer(String[] incomingData) {
        File folder = new File("Server Folder");
        String[] files = folder.list();
        String message="";
        if ((files != null) && (files.length != 0)) {
            for (String file:files) {
                message=message+file+", ";
            }
            message = message.substring(0,message.length()-2);
        } else {
            message = "No files present on the server!";
        }
        return message;
    }

    private String createFile(String[] incomingData) {
        String message;
        boolean fielExists;
        File tempFile;
        try  {
            //tempFile = new File("Server Folder"+File.separator+incomingData[2]);
            tempFile = new File("Server Folder",incomingData[2]);
            fielExists = tempFile.exists();

            if (!fielExists) {
                //BufferedWriter writeFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Server Folder"+File.separator+incomingData[2]), "UTF-8"));
                BufferedWriter writeFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8"));
                String [] randomWords = new String[]{"aaaaaaa", "bbbbbbb", "ccccccc", "ddddddd", "eeeeeee", "fffffff", "ggggggg", "hhhhhhh", "iiiiiii", "jjjjjjj"};

                int numberOfLines = (int)(Math.random()*5)+5;
                for(int i = 0; i<numberOfLines; i++) {
                    String line = "";
                    for (int j = 0; j<5; j++) {
                        int wrd = (int)(Math.random()*9);
                        line = j<4? line+randomWords[wrd]+" ":line+randomWords[wrd]+"\r\n";
                    }
                    writeFile.write(line);
                    writeFile.flush();
                }
                message = "Success! File "+incomingData[2]+" created!";
                writeFile.close();
            } else {
                message = "Fail! Another file with name "+incomingData[2]+" already present. Please change the name!";
            }

        } catch (IOException e) {
            e.printStackTrace();
            message = "Fail! File "+incomingData[2]+" could not be created!";
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