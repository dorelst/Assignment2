For Assignment 2 I created three classes: TCPServer and Connection to run the server, and TCPClient to run the client.
The server can accept multiple concurrent requests and this is done by Connection class that extends Thread class.

TCPServer is the class that contains the main method for the server and it instantiates Connection class when a new connection is established. The server port where the server listens for connections is 12345 and is set by serverPort variable (line 70).
TCPServer class also creates a folder "Server Folder" where all the files are created and serverJournal text file (outside Server Folder) where the incoming and outgoing messages from the client are stored.
Connection class opens two streams, and input (in) and output (out) ones in its constructor.
Connection class run method is the one that runs the server and receives and sends messages to the client.
For each task of the assignment I created a method to handle the function requested: registerClient, createFile, listFileOnServer, transferFile, summaryOfAFile, requestSubsetOfAFile, deleteFile and closeConnection.
Also utilities method where created to generates arbitrary failures for transfer file and subset of a file (randomly generated), to generate checksums and encryption. Currently the probability for an arbitrary failure to happen is set to 10% (lines 341, 342)

TCPClient is the class that contains the main method for the client. The default name of the server where the client tries to connect is set in its constructor (line 21) and is in-csci-rrpc04.cs.iupui.edu. When the Client starts it has the option to change this default name and connect to different server. When client first starts it creates a "Client Folder" where all files will be stored and client journal file where all outgoing and incoming messages from the server are stored.
The port to which the client tries to connect to the server is set in connectToServer method and is controlled by serverPort variable (line 169).
As I did for the server, the TCPClient class has a method that handles all the tasks requested by the assignment: registerToServer, createFile, listFiles, transferFile, requestSummary, requestSubSetFile, deleteFile, closeConnectionToServer.
Additional utilities methods were created to help sending and receiving messages from and to the server, generate checksums and encryption and save server communication messages in the journal file.
