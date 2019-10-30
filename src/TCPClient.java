
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.net.*;
import java.io.*;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;

public class TCPClient {

    private String clientName;
    private String serverName;
    private Socket server;
    private BufferedReader in;
    private BufferedWriter out;

    public TCPClient() {
        //this.serverName = "ZMS-21577-F01";
        //this.serverName = "LAPTOP-GDTMA4IQ";
        //this.serverName = "Doru-PC";
        this.serverName = "in-csci-rrpc04.cs.iupui.edu";

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


    private void runClientInterface() {

        //Retrieving the host name where the client is running
        try {
           setClientName(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        //Before connecting to server, the client tries to create it's working directory "Client Folder"
        if (createWorkingFolder()) {
            String inputChoice;
            Scanner scanner = new Scanner(System.in);
            //printMenuOptions displays a basic text interface for the user to chose from
            printMenuOptions();
            //This is the place where users inputs are processed the appropriate methods are called
            do {
                inputChoice = scanner.nextLine();

                switch (inputChoice) {
                    case "1": registerToServer();
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
            scanner.close();
        }
    }

    //This method creates the working folder for the client, only if it doesn't exist
    private boolean createWorkingFolder() {
        boolean fileExists;
        File tempFile;
        try {
            tempFile = new File("Client Folder");
            fileExists = tempFile.exists();
            if (!fileExists) {
                if (tempFile.mkdir()){
                    printMessageToScreenAndFile("Client "+getClientName()+" folder successfully created!");
                    //System.out.println("Client "+getClientName()+" folder successfully created!");
                    return true;
                } else {
                    printMessageToScreenAndFile("Could not create the "+getClientName()+" client folder!");
                    return false;
                }
            } else {
                printMessageToScreenAndFile("Folder already exists on "+getClientName()+" client! No new folder necessary!");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            printMessageToScreenAndFile("Could not crete Client Folder on "+getClientName()+" client!");
            return false;
        }
    }

    //This method connects the client to the server
    private void registerToServer() {
        //The client is connected to server if there isn't any active connection
        if (getServer() == null) {
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
                int serverPort = 12345;
                setServer(new Socket(serverName, serverPort));
                createStreamToServer();
                //If the connection is successful the user is asked to sign in
                SignInToServer();
            }catch (UnknownHostException e){
                System.out.println("Sock:"+e.getMessage());
            } catch (EOFException e){System.out.println("EOF:"+e.getMessage());
            } catch (IOException e){System.out.println("IO:"+e.getMessage());}
        } else {
            printMessageToScreenAndFile("You are already connected to "+getServerName()+" server! To connect to another server, disconnect first from the current one!");
        }

    }

    //This method signs in the user to server
    private void SignInToServer() {
        String username, password, messageForServer, serverResponse, inputChoice="n";
        Scanner scanner = new Scanner(System.in);

        do {
            printMessageToScreenAndFile("Connected to "+getServerName()+" server! Please Register!");
            System.out.println("Username: ");
            username = scanner.nextLine();
            System.out.println("Password: ");
            password = scanner.nextLine();
            //The message send to the server follows the format: clientName+"_"+[operationCode]+"_"+[arguments/message]
            messageForServer = clientName+"_"+"1_"+username+"_"+password;
            //sendMessageToServer method handles all the messages send to server
            serverResponse = sendMessageToServer(messageForServer);
            String resultOfServerOperation = serverResponse.split(" ", 2)[0];
            if (resultOfServerOperation.equals("Fail!")){
                printMessageToScreenAndFile("Connection to "+getServerName()+" unsuccessful due to registration issues.");
                System.out.println("Would you like to try again? (Y/N)");
                inputChoice = scanner.nextLine();
            }
        } while ((!inputChoice.equals("n")) && (!inputChoice.equals("N")));

    }

    //This method creates the request for the server to create a file
    private void createFile() {
        //Each method, except register to server, checks if there is a connection to the server, before proceeding
        if(getServer()==null) {
            printMessageToScreenAndFile("No connection established! Please connect first to a server before creating a file!");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        String fileName;
        String messageForServer;

        System.out.println("Please input the file name to be created: ");
        //This is doing a very basic check to see if the input name is empty or not
        do {
            fileName = scanner.nextLine();
            if (fileName.equals("")) {
                System.out.println("Empty name not accepted! Try again!");
            }
        } while (fileName.equals(""));

        messageForServer = clientName+"_"+"2"+"_"+fileName;
        sendMessageToServer(messageForServer);

    }

    //This method creates the request for the server to list the files it has
    private void listFiles() {
        if(getServer()==null) {
            printMessageToScreenAndFile("No connection established! Please connect first to a server before requesting a list of files!");
            return;
        }
        String messageForServer = clientName+"_"+"3";
        sendMessageToServer(messageForServer);

    }

    //This method creates the request for the server to transfer a file to the client
    private void transferFile() {

        if(getServer()==null) {
            printMessageToScreenAndFile("No connection established! Please connect first to a server before transferring a file!");
            return;
        }
        Scanner scanner = new Scanner(System.in);
        String fileName;
        String messageForServer;
        boolean invalidFileName = true;
        boolean tryToTransferFile = true;
        String inputChoice;
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
            //It checks if there is a already a file with the same name on the client as the one intended to be transferred from the server
            if (fileExists) {
                printMessageToScreenAndFile("There is already a file on this client with the same name as the one you try to transfer from the server.");
                printMessageToScreenAndFile("Change the name of the file present on this client or choose another file to transfer from the server!");
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

        //If the file can be created on the client it proceeds further with the transfer
        if (!invalidFileName){
            tryToTransferFile = false;
            messageForServer = clientName+"_"+"4"+"_"+fileName;
            do {
                String[] sb = sendMessageToServer(messageForServer).split(" ", 4);
                String serverResponse = sb[0]+sb[1]+sb[2];
                //If the file got transferred with corruption the user has the option to retry
                if (serverResponse.equals("Fail!CorruptedFile!")) {
                    do {
                        System.out.println("Would you like to try again? (Y/N)");
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
                            break;
                    }

                } else {
                    //If the file was transferred successfully it exists the loop
                    tryToTransferFile = false;
                }
            } while (tryToTransferFile);
        }

    }

    //This method creates the request for the server to send a summary of file it has
    private void requestSummary() {
        if(getServer()==null) {
            printMessageToScreenAndFile("No connection established! Please connect first to a server before requesting a summary of a file!");
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

    //This method creates the request for the server to send a subset of a file it has
    private void requestSubSetFile() {
        if (getServer() == null) {
            printMessageToScreenAndFile("No connection established! Please connect first to a server before requesting a subset of a file!");
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

        boolean tryToTransferFile = false;
        String inputChoice;

        messageForServer = clientName+"_"+"6"+"_"+fileName;

        do {
            String serverResponse = sendMessageToServer(messageForServer).split(" ", 2)[0];
            //If the subset got transferred with corruption the user has the option to retry
            if (serverResponse.equals("Fail!")) {
                do {
                    System.out.println("Would you like to try again? (Y/N)");
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
                        break;
                }

            } else {
                tryToTransferFile = false;
            }
        } while (tryToTransferFile);
    }

    //This method creates the request for the server to delete a file it has
    private void deleteFile() {
        if(getServer()==null) {
            printMessageToScreenAndFile("No connection established! Please connect first to a server before requesting deletion of a file!");
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

    //This method close the connection and assign the value null to the server variable to signal there isn't any connection established
    private Socket closeConnectionToServer() {
        if (getServer() == null) {
            printMessageToScreenAndFile("No connection established! Please connect first to a server before trying to close the connection!");
            return null;
        }
        String messageForServer = clientName + "_" + "8";
        sendMessageToServer(messageForServer);
        try {

            closeStreamToServer();
            getServer().close();
            setServer(null);

        } catch (IOException e) {
            printMessageToScreenAndFile("Closing connection to "+getServerName()+" server failed! "+e.getMessage());
            e.getMessage();
        }
        System.out.println("Connection closed successful!");
        printMessageToScreenAndFile("Connection to "+getServerName()+" server closed successfully!");
        return server;
    }

    //This method prints a text interface for the user to chose from
    private void printMenuOptions() {
        System.out.println("\n1 - Connect to Server  ----------------+-- 6 - Request a subset of a file");
        System.out.println("2 - Create file -----------------------+-- 7 - Delete file");
        System.out.println("3 - List files on the Server ----------+-- 8 - Close connection to the Server");
        System.out.println("4 - Transfer file ---------------------+-- 9 - Print Main Menu");
        System.out.println("5 - Summary of a file -----------------+-- 0 - Exit");

    }

    //This method handles the communication with the server for every client request
    private String sendMessageToServer(String messageForServer) {

        String serverResponse;

        try {
            messageForServer = messageForServer + "\n";
            printMessageToScreenAndFile("Client "+getClientName()+" message to "+getServerName()+" server = " + messageForServer);

            //It sends and wait for the Server to respond
            getOut().write(messageForServer);
            getOut().flush();
            serverResponse = getIn().readLine();

            //System.out.println("Server Response = "+serverResponse);
            String[] sb = serverResponse.split(" ", 3);
            String foundFileOnServer = sb[0] + " " + sb[1];
            String serverRequestType = messageForServer.split("_", 3)[1];

            //If the request to the server was to transfer a file or receive a subset and the file was found on the server than further processing is needed
            if (((serverRequestType.equals("4")) || (serverRequestType.equals("6"))) && (foundFileOnServer.equals("File exists!"))) {
                printMessageToScreenAndFile("Server " + serverName + " responded: " + serverResponse);
                //Additional message is expected from server and this is handled by the two methods bellow
                if (serverRequestType.equals("4")) {
                    serverResponse = receiveFile(messageForServer);
                } else {
                    serverResponse = receiveSubsetOfAFile(messageForServer);
                }

                return serverResponse;

            } else {
                printMessageToScreenAndFile("Server " + serverName + " responded: " + serverResponse);
                return serverResponse;
            }
        } catch (UnknownHostException e) {
            printMessageToScreenAndFile("Fail! Unknown Host! Sock:" + e.getMessage());
            return ("Fail! " + "Unknown Host! Sock:" + e.getMessage());
        } catch (EOFException e) {
            printMessageToScreenAndFile("Fail! EOF:" + e.getMessage());
            return ("Fail! " + "EOF:" + e.getMessage());
        } catch (IOException e) {
            printMessageToScreenAndFile("Fail! IO Error:" + e.getMessage());
            return ("Fail! " + "IO Error:" + e.getMessage());
        }

    }

    //This method handles the receiving of a file transferred from the server
    private String receiveFile(String messageForServer) {

        String serverResponse, serverChecksum;
        String fileName = messageForServer.split("_", 3)[2];
        fileName = fileName.substring(0, fileName.length() - 1);
        File tempFile = new File("Client Folder", fileName);

        try (BufferedWriter writeFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8"));) {
            //The first thing the client expect to receive on file is transferred is the server checksum for that file
            serverChecksum = getIn().readLine();

            //After the checksum is received the client is waiting for the file to be transferred
            String line = getIn().readLine();
            while ((line != null) && (line.length() > 0)) {
                line = decryptReceivedMessage(line);
                line = line + "\r\n";
                writeFile.write(line);
                writeFile.flush();
                line = getIn().readLine();
            }

            //After the file is transferred the client gets the confirmation response from the server
            serverResponse = getIn().readLine();

        } catch (IOException e) {
            e.printStackTrace();
            return "Fail! Error transferring the file!";
        }

        //For the file received by the client a checksum is calculated
        String checksum = generateCheckSum(fileName);

        //The client checks the checksum for the received file and the checksum from the server match
        if (checksum.equals(serverChecksum)) {
            printMessageToScreenAndFile("Client " + getClientName() + ": Success! File successfully transferred to the client!\n");
            printMessageToScreenAndFile("The checksum calculated from the received answer has the length = " + checksum.length() + " and is:");
            printMessageToScreenAndFile(checksum);
            printMessageToScreenAndFile("The checksum received has a length of " + serverChecksum.length() + " and is:");
            printMessageToScreenAndFile(serverChecksum);
            printMessageToScreenAndFile("Server " + serverName + ": " + serverResponse);
            return serverResponse;
        } else {
            printMessageToScreenAndFile("Client " + getClientName() + ": Fail! File " + fileName + " transferred but with errors!\n");
            printMessageToScreenAndFile("The checksum calculated from the received answer has the length = " + checksum.length() + " and is:");
            printMessageToScreenAndFile(checksum);
            printMessageToScreenAndFile("The checksum received has a length of " + serverChecksum.length() + " and is:");
            printMessageToScreenAndFile(serverChecksum);
            printMessageToScreenAndFile("Fail! Corrupted File! " + fileName + " transferred but with errors. Checksums don't match!");

            //If the file was received with errors then it's deleted from the client in order to be ready to receive it again in case users chose to
            boolean isFileDeleted = tempFile.delete();
            if (isFileDeleted) {
                printMessageToScreenAndFile("Corrupted file successfully deleted from client " + getClientName() + "!");
            } else {
                printMessageToScreenAndFile("Client " + getClientName() + " couldn't delete corrupted file " + fileName + "!");
            }
            return "Fail! Corrupted File! " + fileName + " transferred but with errors. Checksums don't match!";
        }

    }

    //This method handles the receiving of a subset of file from from server
    private String receiveSubsetOfAFile(String messageForServer) {
        String serverResponse;
        //The subset of a file is save as StringBuilder and is not saved on the client as a file, since it's just a random chunk from the server
        StringBuilder subsetOfFile = new StringBuilder();
        String serverChecksum;
        String filename = messageForServer.split("_",3)[2];
        filename = filename.substring(0, filename.length()-1);

        //The client expects first to receive the file subset from the server
        try {
            String line = getIn().readLine();
            while ((line != null) && (line.length() > 0)) {
                line = decryptReceivedMessage(line);
                line = line + "\n";
                subsetOfFile.append(line);
                line = getIn().readLine();
            }

            //After the subset is received the client expects the checksum calculated by the server for the subset sent
            serverChecksum = getIn().readLine();

            //After the checksum is received the client expects the server confirmation message
            serverResponse = getIn().readLine();

        } catch (IOException e) {
            e.printStackTrace();
            return "Fail! Error transferring the file!";
        }

        //The client calculates its own checksum for the subset received
        String checksum = calculateChecksumForString(subsetOfFile.toString());

        //The client checks the checksum for the received subset and the checksum from the server match
        if (checksum.equals(serverChecksum)) {
            printMessageToScreenAndFile("Client " + getClientName() + ": Success! Subset of the "+filename+" file successfully received by the client!\n");
            printMessageToScreenAndFile("The checksum calculated from the received answer has the length = "+checksum.length()+" and is:");
            printMessageToScreenAndFile(checksum);
            printMessageToScreenAndFile("The checksum received has a length of "+serverChecksum.length()+" and is:");
            printMessageToScreenAndFile(serverChecksum);
            printMessageToScreenAndFile("The subset is:");
            printMessageToScreenAndFile(subsetOfFile.toString());
            printMessageToScreenAndFile("Server " + serverName + ": " + serverResponse);

        } else {
            printMessageToScreenAndFile("Client " + getClientName() + ": Fail! Message received but with errors!\n");
            printMessageToScreenAndFile("Subset received length = "+subsetOfFile.length()+" and is:");
            printMessageToScreenAndFile(subsetOfFile.toString());
            printMessageToScreenAndFile("The checksum calculated from the received answer has the length = "+checksum.length()+" and is:");
            printMessageToScreenAndFile(checksum);
            printMessageToScreenAndFile("The checksum received has a length of "+serverChecksum.length()+" and is:");
            printMessageToScreenAndFile(serverChecksum);

            return "Fail! Error receiving subset for the file "+filename+"!";
        }

        return serverResponse;
    }

    //This method creates the sending and receiving streams to and from the server
    private boolean createStreamToServer() {
        try {
            setIn(new BufferedReader(new InputStreamReader(getServer().getInputStream())));
            setOut(new BufferedWriter(new OutputStreamWriter(getServer().getOutputStream())));
            return true;
        } catch (IOException e) {
            printMessageToScreenAndFile("Cannot create a stream to server "+getServerName()+"!");
            e.printStackTrace();
            return false;
        }
    }

    //This closes the sending and receiving streams to and from the server
    private boolean closeStreamToServer() {
        try {
            getIn().close();
            getOut().close();
            return true;
        } catch (IOException e) {
            printMessageToScreenAndFile("Cannot close the stream to server "+getServerName()+"!");
            e.printStackTrace();
            return false;
        }

    }

    //This method calculates a checksum based on MD5 algorithm for a file
    private String generateCheckSum(String filename) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            System.out.println("Calculate checksum for file : " + filename);
            File tempFile = new File("Client Folder", filename);
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(tempFile))) {
                byte[] buffer = new byte[1024];
                int nread;
                while ((nread = bis.read(buffer)) != -1) {
                    md.update(buffer, 0, nread);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // bytes to hex
            StringBuilder result = new StringBuilder();
            for (byte b : md.digest()) {
                result.append(String.format("%02x", b));
            }

            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    //This method calculates a checksum based on MD5 algorithm for a String
    private String calculateChecksumForString (String messageForClient) {
        String result = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(messageForClient.getBytes("UTF-8"));
            result = DatatypeConverter.printHexBinary(hash).toLowerCase();
            return result;
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    //This method decrypt a string
    private String decryptReceivedMessage (String message) {
        String secret = "qwertyuiopasdfgh";
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        SecretKey secretKey;

        try {
            Cipher myCipher = Cipher.getInstance("AES");
            secretKey = new SecretKeySpec(Arrays.copyOf(decodedKey, 16), "AES");
            myCipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] bytesToBeDecrypted = Base64.getDecoder().decode(message);
            byte[] decryptedBytes = myCipher.doFinal(bytesToBeDecrypted);
            return new String(decryptedBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return "";
    }

    //This method print a message on the screen and also saves it in the Client journal
    private void printMessageToScreenAndFile(String message) {
        System.out.println(message);

        String filename = "Client_"+getClientName()+"_Journal.txt";
        File tempFile = new File(filename);

        try (BufferedWriter writeFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile, true), "UTF-8"));) {

            message = message+"\n";
            writeFile.write(message);
            writeFile.flush();

        } catch (IOException e) {
            System.out.println("Couldn't save the message into " + getClientName() + " client journal");
            e.printStackTrace();
        }

    }


    public static void main (String args[]) {
        TCPClient client = new TCPClient();
        client.runClientInterface();
    }
}

