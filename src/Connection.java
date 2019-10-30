import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

class Connection extends Thread {
    private Map<String, String> usersList = new HashMap<>();
    private BufferedReader in;
    private BufferedWriter out;
    private Socket clientSocket;
    private String serverName;

    public Connection (Socket aClientSocket) {
        try {
            clientSocket = aClientSocket;
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            this.start();
        } catch(IOException e) {System.out.println("Connection:"+e.getMessage());}
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerName() {
        return serverName;
    }

    //The method that controls workflow for the server and it's used to run multiple threads
    public void run() {
        String serverResponse, clientRequest = "";
        try {
            setServerName(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        //The do-while loops until a close connection is received from the client
        do {
            try {
                String messageFromClient = in.readLine();
                if (messageFromClient != null) {
                    //The message from the client is processed here and a response is sent back to the client
                    serverResponse = respondToClient(messageFromClient);
                    printMessageToScreenAndFile("Server "+getServerName()+" response = " + serverResponse);
                    //System.out.println("Server "+getServerName()+" response = " + serverResponse);
                    clientRequest = serverResponse.split(" ", 2)[0];
                    serverResponse = serverResponse+"\n";
                    out.write(serverResponse);
                    out.flush();
                }

            } catch (EOFException e) {
                printMessageToScreenAndFile("Fail! EOF: " + e.getMessage());
                //System.out.println("Fail! EOF: " + e.getMessage());
            } catch (IOException e) {
                printMessageToScreenAndFile("Fail! IO: " + e.getMessage());
                //System.out.println("Fail! IO: " + e.getMessage());
                break;
            }
        } while (!clientRequest.equals("Closed!"));

        try {
            clientSocket.close();
        } catch (IOException e) {
            /*close failed*/
            printMessageToScreenAndFile("Fail! Could not close the connection! IO: " + e.getMessage());
            //System.out.println("Fail! Could not close the connection! IO: " + e.getMessage());
        }
        printMessageToScreenAndFile("Success! Connection closed!");
        //System.out.println("Success! Connection closed!");
    }

    //This method sorts client requests and send them to the proper method to be processed
    private String respondToClient(String data) {
        String[] incomingData = data.split("_");
        int clientChoice = Integer.parseInt(incomingData[1]);
        System.out.println("ClientChoice = "+clientChoice);
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

    //This method register the Client to the server by saving the user name and password provided
    private String registerClient(String[] incomingData) {
        usersList.put(incomingData[2], incomingData[3]);
        String message="";
        try {
            message = "Success! Client "+incomingData[0]+" registered to "+ InetAddress.getLocalHost().getHostName()+" server under user "+incomingData[2]+"!";
        } catch (UnknownHostException e) {
            message = "Success! Client "+incomingData[0]+" registered to server under user "+incomingData[2]+", but server name couldn't be retrieve!";
            e.printStackTrace();
        }
        return message;
    }

    //This method creates a file with the name provided by the clients and it populates it with random text
    private String createFile(String[] incomingData) {
        String message;
        boolean fileExists;
        File tempFile;
        try  {
            //tempFile = new File("Server Folder"+File.separator+incomingData[2]);
            tempFile = new File("Server Folder",incomingData[2]);
            fileExists = tempFile.exists();

            //If there is no file present on the server with the same name as the one provided by the client, a new file is created
            if (!fileExists) {
                //BufferedWriter writeFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Server Folder"+File.separator+incomingData[2]), "UTF-8"));
                BufferedWriter writeFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8"));
                String [] randomWords = new String[]{"Xaaaaaa", "Xbbbbbb", "Xcccccc", "Xdddddd", "Xeeeeee", "Xffffff", "Xgggggg", "Xhhhhhh", "Xiiiiii", "Xjjjjjj"};

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
                message = "Success! File "+incomingData[2]+" created by "+incomingData[0]+" client!";
                writeFile.close();
            } else {
                message = "Fail! Another file with name "+incomingData[2]+" already present. "+incomingData[0]+" change the name, please!";
            }

        } catch (IOException e) {
            e.printStackTrace();
            message = "Fail! File "+incomingData[2]+" could not be created by "+incomingData[0]+" client!";
        }

        return message;
    }

    //This method list the files present on teh server (in Server Folder)
    private String listFilesOnServer(String[] incomingData) {
        File folder = new File("Server Folder");
        String[] files = folder.list();
        String message="List of files for client "+incomingData[0]+": ";
        if ((files != null) && (files.length != 0)) {
            for (String file:files) {
                message=message+file+", ";
            }
            message = message.substring(0,message.length()-2);
        } else {
            message = "No files present on the server! "+incomingData[0]+" try later, or create a file, please!";
        }
        return message;
    }

    //This method transfer the file requested by the client to the client
    private String transferFile(String[] incomingData) {
        File fileToBeTransferred = new File("Server Folder",incomingData[2]);

        //It checks if the file exists on the server and if it does it's sent to the client
        boolean fileExists = fileToBeTransferred.exists();
        if (fileExists) {

            try (BufferedReader readFile = new BufferedReader(new InputStreamReader(new FileInputStream(fileToBeTransferred), "UTF-8"))) {
                out.write("File exists! Begin transfer!\n");
                out.flush();
                //The checksum for the requested file is calculated and sent to the client
                String checksum = generateCheckSum(incomingData[2]);
                checksum = checksum+"\n";
                out.write(checksum);
                out.flush();
                String line;
                line = readFile.readLine();
                while ((line != null) && (line.length()>0)) {
                    //The arbitraryFailure can randomly change or leave it unchanged the string read from the file.
                    line = arbitraryFailure(line);
                    //The string ready to be sent to the client is encrypted
                    line = encryptOutGoingMessage(line);
                    line = line + "\n";
                    out.write(line);
                    out.flush();
//                    out.write("\n");
//                    out.flush();
                    line = readFile.readLine();
                }
                line="\n";
                out.write(line);
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            return "Fail! File not present! "+incomingData[2]+" requested by "+incomingData[0]+" client doesn't exist on the server! ";
        }

        return "Success! File "+incomingData[2]+" requested by "+incomingData[0]+" client transferred!";
    }

    //This method sends a summary for the file requested by the client
    private String summaryOfAFile(String[] incomingData) {
        File fileForSummary = new File("Server Folder",incomingData[2]);

        //It checks if the file requested by the client is present on the server and if found a summary of it it's sent to the client
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

            } catch (IOException e) {
                e.printStackTrace();
            }

            return "Success! This is the requested summary, for client "+incomingData[0]+": file "+incomingData[2]+" has "+numberOfLines+" lines, and "+numberOfWords+" words!";

        } else {
            return "Fail! Client "+incomingData[0]+" asked a summary for file "+incomingData[2]+", but the file doesn't exist on the server!";
        }

    }

    //This method sends a subset of the file requested by the client
    private String requestSubsetOfAFile(String[] incomingData) {
        File fileToSendSubset = new File("Server Folder",incomingData[2]);

        //It checks if the file requested by the client is present on the server and if found a subset of it it's sent to the client
        boolean fileExists = fileToSendSubset.exists();
        if (fileExists) {

            try (BufferedReader readFile = new BufferedReader(new InputStreamReader(new FileInputStream(fileToSendSubset), "UTF-8"))) {
                out.write("File exists! Begin sending a subset of it!\n");
                out.flush();
                StringBuilder subsetOfFile = new StringBuilder();
                String line;
                line = readFile.readLine();
                while ((line != null) && (line.length()>0)) {
                    //It randomly decides if a line will be sent or not
                    int sendTheLine = (int)(Math.random()*4);
                    if (sendTheLine == 2) {
                        //Builds the strings that is sent to the client, before any changes done by arbitraryFailure
                        subsetOfFile.append(line);
                        subsetOfFile.append("\n");
                        //The arbitraryFailure can randomly change or leave it unchanged the string read from the file.
                        line = arbitraryFailure(line);
                        //The string ready to be sent to the client is encrypted
                        line = encryptOutGoingMessage(line);
                        line = line + "\n";
                        out.write(line);
                        out.flush();
 //                       out.write("\n");
 //                       out.flush();
                    }
                    line = readFile.readLine();
                }
                //This signals the client the sending of the subset is done
                line="\n";
                out.write(line);
                out.flush();

                System.out.println("Subset sent: ");
                System.out.println(subsetOfFile.toString());

                //The checksum for the string is calculated and sent to the client
                String checksum = calculateChecksumForString(subsetOfFile.toString());
                out.write(checksum);
                out.flush();

                //This signals the client the end of answer to the client for the subset request
                line="\n";
                out.write(line);
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            return "Fail! Client "+incomingData[0]+" requested a subset for file "+incomingData[2]+", but the file doesn't exist on the server!";
        }

        return "Success! Subset of the file "+incomingData[2]+" transferred to "+incomingData[0]+" client!";
    }

    //This methods deletes a file from the server
    private String deleteFile(String[] incomingData) {
        File fileToBeDeleted = new File("Server Folder",incomingData[2]);

        //It checks if the file specified by the client is present on the server and if found it is deleted
        boolean fileExists = fileToBeDeleted.exists();
        if (fileExists) {
            boolean isFileDeleted = fileToBeDeleted.delete();
            if (isFileDeleted) {
                return "Success! File "+incomingData[2]+" successfully deleted from the server by "+incomingData[0]+" client!";
            } else {
                return "Fail! File "+incomingData[2]+" couldn't be deleted from the server by "+incomingData[0]+" client!";
            }

        } else {
            return "Fail! Client "+incomingData[0]+" tried to delete the file "+incomingData[2]+", but the file doesn't exist on the server!";
        }

    }

    //This method just signals run method the client requested to close the connection and run method will close it
    private String closeConnection(String[] incomingData) {
        return "Closed! Connection from "+incomingData[0]+" successfully closed!";
    }

    //This method randomly generates an arbitrary failure that alters the string sent to the client
    private String arbitraryFailure(String messageToBeSent) {
        int sendCorruptedMessage = (int)(Math.random()*10);
        if (sendCorruptedMessage == 6) {
            System.out.println("Corruption just happened!");
            return messageToBeSent.substring(1);
        } else {
            return messageToBeSent;
        }
    }

    //This method calculates the checksum for the file sent
    private String generateCheckSum(String filename) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            System.out.println("Calculate checksum for file : "+filename);
            File tempFile = new File("Server Folder", filename);
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

            System.out.println("Checksum length = "+result.length()+" and the checksum1 is: ");
            System.out.println(result);

            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    //This method calculates the checksum for the subset sent
    private String calculateChecksumForString (String messageForClient) {
        String result = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(messageForClient.getBytes("UTF-8"));
            result = DatatypeConverter.printHexBinary(hash).toLowerCase();
            System.out.println("checksum lenght = "+result.length()+" and result is:");
            System.out.println(result);
            return result;
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    //This method encrypt the message sent to the client for transfer a file or a subset
    private String encryptOutGoingMessage(String message) {
        String secret = "qwertyuiopasdfgh";
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        SecretKey secretKey;

        try {
            Cipher myCipher = Cipher.getInstance("AES");
            secretKey = new SecretKeySpec(Arrays.copyOf(decodedKey, 16), "AES");
            myCipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] bytesToBeEncrypted = message.getBytes("UTF-8");
            byte[] encryptedBytes = myCipher.doFinal(bytesToBeEncrypted);
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return "";
    }

    //This method print to the screen a message and save it to server journal, too
    private void printMessageToScreenAndFile(String message) {
        System.out.println(message);

        String filename = "Server_"+getServerName()+"_Journal.txt";
        File tempFile = new File(filename);

        try (BufferedWriter writeFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile, true), "UTF-8"));) {

            message = message+"\n";
            writeFile.write(message);
            writeFile.flush();

        } catch (IOException e) {
            System.out.println("Couldn't save the message into "+getServerName()+" client journal");
            e.printStackTrace();
        }

    }


}