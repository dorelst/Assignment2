import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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
                String messageFromClient = in.readLine();
                if (messageFromClient != null) {
                    serverResponse = respondToClient(messageFromClient);
                    System.out.println("Server response = " + serverResponse);
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

    private String registerClient(String[] incomingData) {
        usersList.put(incomingData[2], incomingData[3]);
        String message="";
        try {
            message = "Success! User "+incomingData[0]+" registered to "+ InetAddress.getLocalHost().getHostName()+" server!";
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return message;
    }

    private String createFile(String[] incomingData) {
        String message;
        boolean fileExists;
        File tempFile;
        try  {
            //tempFile = new File("Server Folder"+File.separator+incomingData[2]);
            tempFile = new File("Server Folder",incomingData[2]);
            fileExists = tempFile.exists();

            if (!fileExists) {
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

    private String transferFile(String[] incomingData) {
        File fileToBeTransferred = new File("Server Folder",incomingData[2]);
        boolean fileExists = fileToBeTransferred.exists();

        if (fileExists) {

            try (BufferedReader readFile = new BufferedReader(new InputStreamReader(new FileInputStream(fileToBeTransferred), "UTF-8"))) {
                out.write("File exists! Begin transfer!\n");
                out.flush();
                String checksum = generateCheckSum(incomingData[2]);
                checksum = checksum+"\n";
                out.write(checksum);
                out.flush();
                String line;
                line = readFile.readLine();
                while ((line != null) && (line.length()>0)) {
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

            } catch (IOException e) {
                e.printStackTrace();
            }

            String message = "Success! This is the requested summary: file "+incomingData[2]+" has "+numberOfLines+" lines, and "+numberOfWords+" words!";
            return message;

        } else {
            return "Fail! File "+incomingData[2]+" doesn't exist on the server!";
        }

    }

    private String requestSubsetOfAFile(String[] incomingData) {
        File fileToSendSubset = new File("Server Folder",incomingData[2]);
        boolean fileExists = fileToSendSubset.exists();

        if (fileExists) {

            try (BufferedReader readFile = new BufferedReader(new InputStreamReader(new FileInputStream(fileToSendSubset), "UTF-8"))) {
                out.write("File exists! Begin sending a subset of it!\n");
                out.flush();
                StringBuilder subsetOfFile = new StringBuilder();
                String line;
                line = readFile.readLine();
                while ((line != null) && (line.length()>0)) {
                    int sendTheLine = (int)(Math.random()*4);
                    if (sendTheLine == 2) {
                        subsetOfFile.append(line);
                        subsetOfFile.append("\n");
                        line = encryptOutGoingMessage(line);
                        line = line + "\n";
                        out.write(line);
                        out.flush();
 //                       out.write("\n");
 //                       out.flush();
                    }
                    line = readFile.readLine();
                }
                line="\n";
                out.write(line);
                out.flush();

                System.out.println("Subset sent: ");
                System.out.println(subsetOfFile.toString());
                String checksum = calculateChecksumForString(subsetOfFile.toString());
                out.write(checksum);
                out.flush();

                line="\n";
                out.write(line);
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            return "Fail! File "+incomingData[2]+" doesn't exist on the server!";
        }

        return "Success! Subset of the file "+incomingData[2]+" transferred!";
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

    private String closeConnection(String[] incomingData) {
        return "Closed! Connection from "+incomingData[0]+" successfully closed!";
    }

    private String generateCheckSum(String filename) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String checksum = calculateChecksum(filename, md);
            System.out.println("Checksum length = "+checksum.length()+" and the checksum1 is: ");
            System.out.println(checksum);

            return checksum;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String calculateChecksum (String filename, MessageDigest md) {
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
        return result.toString();
    }

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

}