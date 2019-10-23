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
            setIn(new BufferedReader(new InputStreamReader(getServer().getInputStream())));
            setOut(new BufferedWriter(new OutputStreamWriter(getServer().getOutputStream())));
            registerToServer();
        }catch (UnknownHostException e){
            System.out.println("Sock:"+e.getMessage());
        } catch (EOFException e){System.out.println("EOF:"+e.getMessage());
        } catch (IOException e){System.out.println("IO:"+e.getMessage());}

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

    private void deleteFile() {
    }

    private void requestSubSetFile() {
    }

    private void requestSummary() {
    }

    private void transferFile() {
    }

    private void listFiles() {
    }

    private void createFile() {
        if(server==null) {
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

    private String sendMessageToServer (String messageForServer) {
        String serverResponse;
        try {
            messageForServer = messageForServer + "\n";
            System.out.println("message to server = "+messageForServer);
            getOut().write(messageForServer);   // UTF is a string encoding; see Sec 4.3
            getOut().flush();
            serverResponse = getIn().readLine();
            String serverRequest = serverResponse.split(" ", 2)[0];
            if (serverRequest.equals("Success!")) {
                System.out.println("Server "+serverName+" responded: "+serverResponse);
                return serverResponse;
            }
        }catch (UnknownHostException e){
            System.out.println("Unknown Host! Sock:"+e.getMessage());
            return ("Fail! "+"Unknown Host! Sock:"+e.getMessage());
        } catch (EOFException e){
            System.out.println("EOF:"+e.getMessage());
            return ("Fail! "+"EOF:"+e.getMessage());
        } catch (IOException e){
            System.out.println("IO Error:"+e.getMessage());
            return ("Fail! "+"IO Error:"+e.getMessage());
        }
        return "Fail! Unknown error!";
    }

    public static void main (String args[]) {
        TCPClient client = new TCPClient();
        client.runClientInterface();
    }
}

