import java.io.*;
import java.net.*;
import java.util.*;

/*
Some code found in this class was adapted from code found on stackoverflow.com
 */
public class ftpserver {

    private static ServerSocket welcomeSocket;
    public static void main(String args[]) throws Exception {
        final int port = 12000;
        int count = 1;
        try{
            welcomeSocket = new ServerSocket(port);
        }
        catch (IOException ioEx){
            System.out.println("Unable to set up port!");
            System.exit(1);
        }

        while (true) {
            //	ServerSocket welcomeSocket = new ServerSocket(port);
            Socket clientSocket = welcomeSocket.accept();

            ClientHandler client = new ClientHandler(clientSocket, count);
            client.start();
            System.out.println("Started connection with client "+count+".");//debugging line, remove later
            count = count +1;
	}
    }
}

class ClientHandler extends Thread {
    private FileOutputStream out;
    private FileInputStream fs;
    private String fileName;
    private Socket client;
    private static Socket dataSocket;
    private static DataOutputStream dataOutToClient;
    private Scanner input;
    private PrintWriter output;
    private ServerSocket s;
    private static DataInputStream dataFromClient;
    private static DataOutputStream outToClient;
    private static BufferedReader inFromClient;
    private String fromClient;
    private int dataPort;
    private int clientName;

    public ClientHandler(Socket socket, int count)
    {
        //Set up reference to associated socket...
        client = socket;
	    clientName = count;
        try{
            outToClient = new DataOutputStream(client.getOutputStream());
            inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
        }catch (IOException e){
            System.out.println("ClientHandler has failed to setup the input and output streams.");
        }
    }

    public void run()
    {
        String frstln;
        int port;

        String clientCommand;
        File curDir = new File(".");
        File[] fileList = curDir.listFiles();
	boolean running = true;
        try {
            fromClient = inFromClient.readLine();
        } catch (IOException ioEx) {
            System.out.println("Unable to set up port!");
        }

        StringTokenizer tokens = new StringTokenizer(fromClient);
        frstln = tokens.nextToken();
        port = Integer.parseInt(frstln);
        clientCommand = tokens.nextToken();

        while (running) {

            if (clientCommand.equals("list:")) {
                try {
                    dataSocket = new Socket(client.getInetAddress(), port);
                    DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());

                    //list everything in the current directory and send to client
                    String fileNames = "";
                    if (fileList != null) {

                        for (File f : fileList) {
                            if (f.exists()) {
                                fileNames = fileNames + f.getName() + " ";
                                System.out.println(f.getName());//Debugging line, remove later
                            }
                        }
                    } else {
                        System.out.println("file list null");
                    }

                    dataOutToClient.writeBytes(fileNames);
                    dataSocket.close();
                } catch (IOException ioEx) {
                    System.out.println("Unable to set up port!131");
                }

                System.out.println("Data Socket closed");
            }

            if (clientCommand.equals("retr:")) {
                try{
                    System.out.println("Start of retr." + "\n" + fromClient);//debugging line, remove later
                    dataSocket = new Socket(client.getInetAddress(), port);
                    BufferedReader dataInFromClient = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
                    DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());

                    System.out.println(dataSocket.isConnected());

                    //Read in the file name from the client.
                    String fileName = tokens.nextToken();
                    System.out.println(fileName);//debugging line, remove later
                    boolean exists = false;

                    System.out.println(dataSocket.isConnected());

                    try{
                        for (File f : fileList){
                            if (f.getName().equals(fileName)){
                                exists = true;
                                System.out.println("File found!");
                            }
                        }
                    } catch (NullPointerException e){
                        System.out.println("There are no files in the current directory.\n If you see this something is very wrong.");
                        e.printStackTrace();
                    }

                    if (exists){
                        outToClient.writeBytes("200 OK\n");
                        System.out.println("Sent 200 OK"); //Debugging line
                        File f = new File(fileName);
                        BufferedReader input = new BufferedReader(new FileReader(f));

                        try {
                            String fileLine = input.readLine();
                            System.out.println(fileLine);
                            while (fileLine != null) {
                                dataOutToClient.writeBytes(fileLine + "\n");
                                fileLine = input.readLine();
                                System.out.println(fileLine);
                            }
                        } catch (NoSuchElementException e){
                            System.out.println("Empty line reached.");//debugging line, remove later
                            dataOutToClient.writeBytes("EOF");
                        }
                        dataOutToClient.writeBytes("EOF");
                    } else {
                        outToClient.writeBytes("550\n");
                        System.out.println("Sent 550, file doesn't exist");//debugging line
                    }
                }catch (IOException e) {
                    System.out.println("IOException for retr:");

                }

                try {
                    System.out.println("Closing connection...");
                    dataSocket.close();
                    System.out.println("Connection closed.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (clientCommand.equals("stor:")) {
                //saves the file to the current directory.
                try {
                    dataSocket = new Socket(client.getInetAddress(), port);
                    BufferedReader dataInFromClient = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

                    System.out.println(dataSocket.isConnected());

                    String fileName = tokens.nextToken();

                    System.out.println(fileName);

                    String newFileName = fileName.replaceFirst("[.][^.]+$", "");
                    File f = new File(newFileName + 1 + ".txt");

                    //File f = new File(fileName);
                    BufferedWriter bw = new BufferedWriter(new FileWriter(f));

                    String fileLine = dataInFromClient.readLine();

                    while(fileLine != null){
                        bw.write(fileLine + "\n");
                        fileLine = dataInFromClient.readLine();
                    }
                    dataSocket.close();
                    bw.close();

                } catch (IOException e) {
                    System.out.println("Port couldn't be made.");
                }
            }
            if (clientCommand.equals("quit:")) {
                System.out.println("Client "+clientName+" disconnected.");

                try {
                    client.close();
                } catch (IOException ioEx) {
                    System.out.println("Unable to set up port!");
                }
		running = false;
            }else{
            	try {
                	clientCommand = inFromClient.readLine();

            	} catch (IOException ioEx) {
                	System.out.println("Command Fail");
            	}

            //This should get the next command and port that we want the data line to be on.
            //Then it loops to run that next command.
            	tokens = new StringTokenizer(clientCommand);

            	port = Integer.parseInt(tokens.nextToken());
            	clientCommand = tokens.nextToken();
		}
        }

    }

}

