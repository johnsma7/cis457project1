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
        int count = 0;
        try{
            welcomeSocket = new ServerSocket(port);
        }
        catch (IOException ioEx){
            System.out.println("Unable to set up port!");
            System.exit(1);
        }

        while (true) {

            //	ServerSocket welcomeSocket = new ServerSocket(port);
            System.out.println("loop top");//debugging line, remove later
            Socket clientSocket = welcomeSocket.accept();

            ClientHandler client = new ClientHandler(clientSocket);
            client.start();
            System.out.println("Started a new client. ");//debugging line, remove later
        }
    }
}

class ClientHandler extends Thread {
    private FileOutputStream out;
    private FileInputStream fs;
    private String fileName;
    private Socket client;
    private static Socket dataSocket;
    private Scanner input;
    private PrintWriter output;
    private ServerSocket s;
    private static DataInputStream dataFromClient;
    private static DataOutputStream outToClient;
    private static BufferedReader inFromClient;
    private String fromClient;
    private int dataPort;

    public ClientHandler(Socket socket)
    {
        //Set up reference to associated socket...
        client = socket;

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

        try {
            fromClient = inFromClient.readLine();
        } catch (IOException ioEx) {
            System.out.println("Unable to set up port!");
        }

        StringTokenizer tokens = new StringTokenizer(fromClient);
        frstln = tokens.nextToken();
        port = Integer.parseInt(frstln);
        clientCommand = tokens.nextToken();
        System.out.println(clientCommand);//Debugging line remove later

        while (true) {

            System.out.println(clientCommand + " Inside loop");//Debugging line remove later

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

                    //Read in the file name from the client.
                    String fileName = tokens.nextToken();
                    System.out.println(fileName);//debugging line, remove later
                    boolean exists = false;

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
                        outToClient.writeBytes("200 OK");
                        System.out.println("Sent 200 OK"); //Debugging line
                        File f = new File(fileName);
                        input = new Scanner(f);
                        try {
                            String fileLine = input.nextLine();

                            while (fileLine != null) {
                                dataOutToClient.writeBytes(fileLine);
                                fileLine = input.nextLine();
                            }
                        } catch (NoSuchElementException e){
                            System.out.println("Empty line reached.");//debugging line, remove later
                            dataOutToClient.writeBytes("eof");
                        }
                        dataOutToClient.writeBytes("eof");
                    } else {
                        outToClient.writeBytes("550");
                        System.out.println("Sent 550, file doesn't exist");//debugging line
                    }
                }catch (IOException e) {
                    System.out.println("IOException for retr:");
                }

                try {
                    dataSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

                if (clientCommand.equals("stor")) {
                    //saves the file to the current directory.
                    try {
                        dataSocket = new Socket(client.getInetAddress(), port);

                        System.out.println("Unable to set up port!");

                        dataFromClient = new DataInputStream(dataSocket.getInputStream());

                        System.out.println("Unable to set up port!");

                        byte[] b = new byte[1024];
                        String fileName = tokens.nextToken(); // This assumes we send the file name via the command line right after stor:

                        out = new FileOutputStream(fileName);

                        System.out.println("Unable to set up port!");

                        dataFromClient.read(b);

                        System.out.println("Unable to set up port!");


                        out.write(b);

                        System.out.println("Unable to set up port!");


                        out.close();
                        System.out.println("Unable to set up port!");
                        dataSocket.close();
                        System.out.println("Unable to set up port!");
                        outToClient.writeBytes("File stored.");

                    } catch (IOException ioEx) {
                        System.out.println("Unable to set up port!");
                    }
                }
                if (clientCommand.equals("quit:")) {
                    System.out.println("Closing the server...");
                    System.exit(1);


                try {
                    client.close();
                } catch (IOException ioEx) {
                    System.out.println("Unable to set up port!");
                }
            }

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

    private static void sendBytes(FileInputStream fs, DataOutputStream data) throws Exception {
        byte[] buffer = new byte[1024];
        int bytes = 0;

        while ((bytes = fs.read(buffer)) != -1) {
            data.write(buffer, 0, bytes);
        }
    }

}






