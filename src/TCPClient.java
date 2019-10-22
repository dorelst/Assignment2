import java.net.*;
import java.io.*;
import java.util.Scanner;

public class TCPClient {

    private static String clientName="";
    private static String serverName="Doru-PC";

    private static String sendMessage(String message) {
        Socket s = null;
        String data="";
        try{
            int serverPort = 7896;
            s = new Socket("DStoian-LEN", serverPort);
            DataInputStream in = new DataInputStream( s.getInputStream());
            DataOutputStream out = new DataOutputStream( s.getOutputStream());
            out.writeUTF(message);   // UTF is a string encoding; see Sec 4.3
            data = in.readUTF();
            System.out.println("Received:"+ data) ;
            return data;
        }catch (UnknownHostException e){
            System.out.println("Sock:"+e.getMessage());
        } catch (EOFException e){System.out.println("EOF:"+e.getMessage());
        } catch (IOException e){System.out.println("IO:"+e.getMessage());
        } finally {if(s!=null) try {s.close();}catch (IOException e){/*close failed*/}}
        return data;
    }

    private static void runClientInterface() {
        try {
            clientName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        Socket server=null;
        String inputChoice;
        Scanner scanner = new Scanner(System.in);
        printMenuOptions();
        do {
            inputChoice = scanner.nextLine();

            switch (inputChoice) {
                case "1": server=connectToServer(server);
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
                case "8": server = closeConnectionToServer(server);
                    printMenuOptions();
                    break;
                case "9": printMenuOptions();
                    break;
            }
            
        } while (!inputChoice.equals("0"));
        //scanner.close();
    }

    private static Socket connectToServer(Socket server) {
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
            server = new Socket(serverName, serverPort);
            registerToServer(server);
        }catch (UnknownHostException e){
            System.out.println("Sock:"+e.getMessage());
        } catch (EOFException e){System.out.println("EOF:"+e.getMessage());
        } catch (IOException e){System.out.println("IO:"+e.getMessage());}

        return server;
    }

    private static void registerToServer(Socket server) {
        String username, password, registrationCredentials, serverResponse, inputChoice;
        Scanner scanner = new Scanner(System.in);

        do {
            System.out.println("Connected to Server. Please Register!");
            System.out.println("Username: ");
            username = scanner.nextLine();
            System.out.println("Password: ");
            password = scanner.nextLine();
            registrationCredentials = clientName+"_"+"1_"+username+"_"+password;

            try{
                DataInputStream in = new DataInputStream( server.getInputStream());
                DataOutputStream out = new DataOutputStream( server.getOutputStream());
                out.writeUTF(registrationCredentials);   // UTF is a string encoding; see Sec 4.3
                serverResponse = in.readUTF();
                String registrationStatus = serverResponse.split(" ", 2)[0];
                if (registrationStatus.equals("Success!")) {
                    System.out.println("Server "+serverName+" responded: "+serverResponse);
                    return;
                }
            }catch (UnknownHostException e){
                System.out.println("Unknown Host! Sock:"+e.getMessage());
                return;
            } catch (EOFException e){
                System.out.println("EOF:"+e.getMessage());
                return;
            } catch (IOException e){
                System.out.println("IO Error:"+e.getMessage());
                return;
            }

            System.out.println("Connection unsuccessful due to wrong registration issue.");
            System.out.println("Would you like to try again? (Y/N)");
            inputChoice = scanner.nextLine();
        } while ((!inputChoice.equals("n")) && (!inputChoice.equals("N")));
    }

    private static Socket closeConnectionToServer(Socket server) {
        if(server!=null) {
            try {
                server.close();
                server = null;
            }catch (IOException e){
                /*close failed*/
                System.out.println("Connection close failed!");
            }
            System.out.println("Connection closed successful! ");
        } else {
            System.out.println("There is no connection established to a server. Connect first!");
        }
        return server;
    }

    private static void deleteFile() {
    }

    private static void requestSubSetFile() {
    }

    private static void requestSummary() {
    }

    private static void transferFile() {
    }

    private static void listFiles() {
    }

    private static void createFile() {
    }

    private static void printMenuOptions() {
        System.out.println();
        System.out.println("1 - Connect to Server  ----------------+-- 6 - Request a subset of a file");
        System.out.println("2 - Create file -----------------------+-- 7 - Delete file");
        System.out.println("3 - List files on the Server ----------+-- 8 - Close connection to the Server");
        System.out.println("4 - Transfer file ---------------------+-- 9 - Print Main Menu");
        System.out.println("5 - Summary of a file -----------------+-- 0 - Exit");

    }

    public static void main (String args[]) {
        runClientInterface();
    }
}

