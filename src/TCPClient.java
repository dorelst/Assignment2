import com.sun.xml.internal.ws.policy.EffectiveAlternativeSelector;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class TCPClient {

    private String clientName;
    private String serverName;
    private Socket server;
    private BufferedReader in;
    private BufferedWriter out;

    public TCPClient() {
        this.clientName = "";
        this.serverName = "ZMS-21577-F01";
        //this.serverName = "Doru-PC";

        this.server = null;
        this.in = null;
        this.out = null;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public Socket getServer() {
        return server;
    }

    public void setServer(Socket server) {
        this.server = server;
    }

    public BufferedReader getIn() {
        return in;
    }

    public void setIn(BufferedReader in) {
        this.in = in;
    }

    public BufferedWriter getOut() {
        return out;
    }

    public void setOut(BufferedWriter out) {
        this.out = out;
    }

    /*    private static String sendMessage(String message) {
        Socket server = null;
        String data = "";
        try {
            int serverPort = 7896;
            //server = new Socket("DStoian-LEN", serverPort);
            server = new Socket("ZMS-21577-F01", serverPort);
            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
            out.write(message);   // UTF is a string encoding; see Sec 4.3
            data = in.readLine();
            System.out.println("Received:" + data);
            return data;
        } catch (UnknownHostException e) {
            System.out.println("Sock:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        } finally {
            if (server != null) try {
                server.close();
            } catch (IOException e) {*//*close failed*//*}
        }
        return data;
    }*/


    private void runClientInterface() {

        if (createWorkingDirectory()) {
            try {
                clientName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            //testCreateFile("test1.txt");

            Socket server=null;
            String inputChoice;
            Scanner scanner = new Scanner(System.in);
            printMenuOptions();
            do {
                inputChoice = scanner.nextLine();

                switch (inputChoice) {
                    case "1": connectToServer();
                        printMenuOptions();
                        break;
                    case "2": createFile();
                        printMenuOptions();
                        break;
                    case "3": listFiles();
                        printMenuOptions();
                        break;
                    case "4": transferFile();
                        printMenuOptions();
                        break;
                    case "5": requestSummary();
                        printMenuOptions();
                        break;
                    case "6": requestSubSetFile();
                        printMenuOptions();
                        break;
                    case "7": deleteFile();
                        printMenuOptions();
                        break;
                    case "8": server = closeConnectionToServer();
                        printMenuOptions();
                        break;
                    case "9": printMenuOptions();
                        break;
                }

            } while (!inputChoice.equals("0"));
            //scanner.close();
        }
    }

    private static boolean createWorkingDirectory() {
        boolean fielExists;
        File tempFile;
        try {
            tempFile = new File("Client Folder");
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

/*
    private void testCreateFile(String filename) {
        File tempFile = new File("Client Folder", filename);
        boolean fileExists = tempFile.exists();

        if (fileExists) {
            System.out.println("File already created!");
        } else {

            try {
                FileOutputStream fos = new FileOutputStream(tempFile);
                System.out.println("fos created");
                OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                System.out.println("osw created");
                BufferedWriter writeFile = new BufferedWriter(osw);
                System.out.println("bw created");
                System.out.println("File Created!");
                writeFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
*/

    private String sendMessageToServer(String messageForServer) {

        String serverResponse;

        try {
            messageForServer = messageForServer + "\n";
            System.out.println("message to server = " + messageForServer);
            String serverRequestType = messageForServer.split("_", 3)[1];

            getOut().write(messageForServer);
            getOut().flush();

            serverResponse = getIn().readLine();
            //System.out.println("ServerResponse = "+serverResponse);
            String[] sb = serverResponse.split(" ", 3);
            String foundFileOnServer = sb[0] + " " + sb[1];

            if (((serverRequestType.equals("4")) || (serverRequestType.equals("6"))) && (foundFileOnServer.equals("File exists!"))) {
                System.out.println("Server " + serverName + ": " + serverResponse);
                if (serverRequestType.equals("4")) {
                    serverResponse = receiveFile(messageForServer);
                } else {
                    serverResponse = receiveSubsetOfAFile(messageForServer);
                }

/*
                    String fileName = messageForServer.split("_",3)[2];
                    fileName = fileName.substring(0,fileName.length()-1);
                    File tempFile = new File("Client Folder", fileName);
                    boolean fileExists = tempFile.exists();

                    if (fileExists) {
                        System.out.println("There is already a file on this client with the same name as the one you try to transfer from the server.");
                        System.out.println("Change the name of the file present on this client or choose another file to transfer from the server!");
                        return "Fail! Duplicate name on the client";
                    } else {

                        //testCreateFile("test5.txt");
                        try (BufferedWriter writeFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8"));) {
*/
/*
                            FileOutputStream fos = new FileOutputStream(tempFile);
                            System.out.println("fos created");
                            OutputStreamWriter osw = new OutputStreamWriter (fos, "UTF-8");
                            System.out.println("osw created");
                            BufferedWriter writeFile = new BufferedWriter(osw);
                            System.out.println("bw created");
*//*

                            //BufferedWriter writeFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8"));

                            String line = getIn().readLine();
                            System.out.println("line = "+line);
                            while ((line != null) && (line.length()>0)) {
                                line = line + "\n";
                                writeFile.write(line);
                                writeFile.flush();
                                line = getIn().readLine();
                            }

                            serverResponse = getIn().readLine();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    System.out.println("Server "+serverName+" responded: "+serverResponse);
                    return serverResponse;
*/
                return serverResponse;

            } else {
                System.out.println("Server " + serverName + ": " + serverResponse);
                return serverResponse;
            }
/*
            serverResponse = "";
            stringBuffer = getIn().readLine(true);
            while (stringBuffer != null) {
                System.out.println("strinbuffer = "+stringBuffer);
                serverResponse = stringBuffer;
                stringBuffer = getIn().readLine();
            }
*/
/*
            String serverRequest = serverResponse.split(" ", 2)[0];
            if (serverRequest.equals("Success!")) {
                System.out.println("Server "+serverName+" responded: "+serverResponse);
                return serverResponse;
            }
*/
        } catch (UnknownHostException e) {
            System.out.println("Unknown Host! Sock:" + e.getMessage());
            return ("Fail! " + "Unknown Host! Sock:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
            return ("Fail! " + "EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO Error:" + e.getMessage());
            return ("Fail! " + "IO Error:" + e.getMessage());
        }

    }

    private String receiveSubsetOfAFile(String messageForServer) {
        String serverResponse;
        String fileName = messageForServer.split("_",3)[2];
        fileName = fileName.substring(0,fileName.length()-1);
        File tempFile = new File("Client Folder", fileName);
        boolean fileExists = tempFile.exists();

        if (fileExists) {
            System.out.println("There is already a file on this client with the same name as the one you try to transfer from the server.");
            System.out.println("Change the name of the file present on this client or choose another file to transfer from the server!");
            return "Fail! Duplicate name on the client";
        } else {

            try (BufferedWriter writeFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8"));) {
                String line = getIn().readLine();
                while ((line != null) && (line.length()>0)) {
                    line = line + "\n";
                    writeFile.write(line);
                    writeFile.flush();
                    line = getIn().readLine();
                }

                serverResponse = getIn().readLine();

            } catch (IOException e) {
                e.printStackTrace();
                return "Fail! Error transferring the file!";
            }

            System.out.println("Client "+getClientName()+": File successfully transferred to the client!");
            System.out.println("Server "+serverName+": "+serverResponse);
            return serverResponse;

        }

    }

    private String receiveFile(String messageForServer) {

        String serverResponse;
        String fileName = messageForServer.split("_",3)[2];
        fileName = fileName.substring(0,fileName.length()-1);
        File tempFile = new File("Client Folder", fileName);
        boolean fileExists = tempFile.exists();

        if (fileExists) {
            System.out.println("There is already a file on this client with the same name as the one you try to transfer from the server.");
            System.out.println("Change the name of the file present on this client or choose another file to transfer from the server!");
            return "Fail! Duplicate name on the client";
        } else {

            try (BufferedWriter writeFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8"));) {
                String line = getIn().readLine();
                while ((line != null) && (line.length()>0)) {
                    line = line + "\n";
                    writeFile.write(line);
                    writeFile.flush();
                    line = getIn().readLine();
                }

                serverResponse = getIn().readLine();

            } catch (IOException e) {
                e.printStackTrace();
                return "Fail! Error transferring the file!";
            }

            System.out.println("Client "+getClientName()+": File successfully transferred to the client!");
            System.out.println("Server "+serverName+": "+serverResponse);
            return serverResponse;

        }


    }

    private void connectToServer() {
        String inputChoice="";
        Scanner scanner = new Scanner(System.in);
        System.out.println("Default Server name is: "+serverName);
        System.out.println("Would you like to connect to another Server? (Y/N)");

        do {
            inputChoice = scanner.nextLine();
            if ((inputChoice.equals("y")) || (inputChoice.equals("Y"))) {
                System.out.println("Please input new server name:");
                serverName = scanner.nextLine();
                System.out.println("New server name is: "+serverName);
                System.out.println("Would you like to connect to another Server? (Y/N)");
            }
        } while ((!inputChoice.equals("n")) && (!inputChoice.equals("N")));

        try{
            int serverPort = 7896;
            setServer(new Socket(serverName, serverPort));
            createStreamToServer();
            //testCreateFile("test2.txt");
/*
            setIn(new BufferedReader(new InputStreamReader(getServer().getInputStream())));
            setOut(new BufferedWriter(new OutputStreamWriter(getServer().getOutputStream())));
*/
            registerToServer();
        }catch (UnknownHostException e){
            System.out.println("Sock:"+e.getMessage());
        } catch (EOFException e){System.out.println("EOF:"+e.getMessage());
        } catch (IOException e){System.out.println("IO:"+e.getMessage());}

    }

    private boolean createStreamToServer() {
        try {
            setIn(new BufferedReader(new InputStreamReader(getServer().getInputStream())));
            setOut(new BufferedWriter(new OutputStreamWriter(getServer().getOutputStream())));
            return true;
        } catch (IOException e) {
            System.out.println("Cannot create a stream to server "+getServerName()+"!");
            e.printStackTrace();
            return false;
        }
    }

    private boolean closeStreamToServer() {
        try {
            getIn().close();
            getOut().close();
            return true;
        } catch (IOException e) {
            System.out.println("Cannot close the stream to server "+getServerName()+"!");
            e.printStackTrace();
            return false;
        }

    }

    private void registerToServer() {
        String username, password, messageForServer, serverResponse, inputChoice="n";
        Scanner scanner = new Scanner(System.in);

        do {
            System.out.println("Connected to Server. Please Register!");
            System.out.println("Username: ");
            username = scanner.nextLine();
            System.out.println("Password: ");
            password = scanner.nextLine();
            messageForServer = clientName+"_"+"1_"+username+"_"+password;
            serverResponse = sendMessageToServer(messageForServer);
            String resultOfServerOperation = serverResponse.split(" ", 2)[0];
            if (resultOfServerOperation.equals("Fail!")){
                System.out.println("Connection unsuccessful due to wrong registration issue.");
                System.out.println("Would you like to try again? (Y/N)");
                inputChoice = scanner.nextLine();
            }
        } while ((!inputChoice.equals("n")) && (!inputChoice.equals("N")));

    }

    private Socket closeConnectionToServer() {
        if (getServer() == null) {
            System.out.println("No connection established! Please connect first to a server!");
            return null;
        }
        String messageForServer = clientName + "_" + "8";
        sendMessageToServer(messageForServer);
        try {

            closeStreamToServer();
            getServer().close();
            setServer(null);

        } catch (IOException e) {
            System.out.println("Connection close failed!");
        }
        System.out.println("Connection closed successful!");
        return server;
    }

    private void deleteFile() {
        if(getServer()==null) {
            System.out.println("No connection established! Please connect first to a server!");
            return;
        }
        Scanner scanner = new Scanner(System.in);
        String fileName;
        String messageForServer;

        System.out.println("Please input the file name to be deleted from the server: ");
        do {
            fileName = scanner.nextLine();
            if (fileName.equals("")) {
                System.out.println("Empty name not accepted! Try again!");
            }
        } while (fileName.equals(""));

        messageForServer = clientName+"_"+"7"+"_"+fileName;
        sendMessageToServer(messageForServer);

    }

    private void requestSubSetFile() {
        if (getServer() == null) {
            System.out.println("No connection established! Please connect first to a server!");
            return;
        }
        Scanner scanner = new Scanner(System.in);
        String fileName;
        String messageForServer;

        System.out.println("Please input the file name for which a subset is requested: ");
        do {
            fileName = scanner.nextLine();
            if (fileName.equals("")) {
                System.out.println("Empty name not accepted! Try again!");
            }
        } while (fileName.equals(""));

        messageForServer = clientName + "_" + "6" + "_" + fileName;
        sendMessageToServer(messageForServer);

    }

    private void requestSummary() {
        if(getServer()==null) {
            System.out.println("No connection established! Please connect first to a server!");
            return;
        }
        Scanner scanner = new Scanner(System.in);
        String fileName;
        String messageForServer;

        System.out.println("Please input the file name for which you would like a summary: ");
        do {
            fileName = scanner.nextLine();
            if (fileName.equals("")) {
                System.out.println("Empty name not accepted! Try again!");
            }
        } while (fileName.equals(""));

        messageForServer = clientName+"_"+"5"+"_"+fileName;
        sendMessageToServer(messageForServer);

    }

    private void transferFile() {

        if(getServer()==null) {
            System.out.println("No connection established! Please connect first to a server!");
            return;
        }
        Scanner scanner = new Scanner(System.in);
        String fileName;
        String messageForServer;
        boolean invalidFileName = true;
        boolean tryToTransferFile = true;
        do {

            System.out.println("Please input the file name to be transferred: ");
            do {
                fileName = scanner.nextLine();
                if (fileName.equals("")) {
                    System.out.println("Empty name not accepted! Try again!");
                }
            } while (fileName.equals(""));

            File tempFile = new File("Client Folder", fileName);
            boolean fileExists = tempFile.exists();

            if (fileExists) {
                System.out.println("There is already a file on this client with the same name as the one you try to transfer from the server.");
                System.out.println("Change the name of the file present on this client or choose another file to transfer from the server!");
                String inputChoice;
                do {
                    System.out.println("Would you like to transfer another file? (Y/N)");
                    inputChoice = scanner.nextLine();
                } while ((!inputChoice.equals("n")) && (!inputChoice.equals("N")) && (!inputChoice.equals("y")) && (!inputChoice.equals("Y")));
                switch (inputChoice) {
                    case "n": tryToTransferFile = false;
                    break;
                    case "N": tryToTransferFile = false;
                    break;
                    case "y": tryToTransferFile = true;
                    break;
                    case "Y": tryToTransferFile = true;
                }
            } else {
                invalidFileName = false;
                tryToTransferFile = false;
            }
        } while (tryToTransferFile);

        if (!invalidFileName){
            messageForServer = clientName+"_"+"4"+"_"+fileName;
            sendMessageToServer(messageForServer);
        }

    }

    private void listFiles() {
        if(getServer()==null) {
            System.out.println("No connection established! Please connect first to a server!");
            return;
        }
        String messageForServer = clientName+"_"+"3";
        sendMessageToServer(messageForServer);

    }

    private void createFile() {
        if(getServer()==null) {
            System.out.println("No connection established! Please connect first to a server!");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        String fileName;
        String messageForServer;

        System.out.println("Please input the file name to be created: ");
        do {
            fileName = scanner.nextLine();
            if (fileName.equals("")) {
                System.out.println("Empty name not accepted! Try again!");
            }
        } while (fileName.equals(""));

        messageForServer = clientName+"_"+"2"+"_"+fileName;
        sendMessageToServer(messageForServer);

    }

    private void printMenuOptions() {
        System.out.println();
        System.out.println("1 - Connect to Server  ----------------+-- 6 - Request a subset of a file");
        System.out.println("2 - Create file -----------------------+-- 7 - Delete file");
        System.out.println("3 - List files on the Server ----------+-- 8 - Close connection to the Server");
        System.out.println("4 - Transfer file ---------------------+-- 9 - Print Main Menu");
        System.out.println("5 - Summary of a file -----------------+-- 0 - Exit");

    }


    public static void main (String args[]) {
        TCPClient client = new TCPClient();
        client.runClientInterface();
    }
}

