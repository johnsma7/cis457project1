import java.io.*;
import java.net.*;
import java.util.*;

/*
Some code found in this class was adapted form code found on stackoverflow.com

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
            System.out.println("loop top");
            Socket clientSocket = welcomeSocket.accept();

            count = count + 2;
            ClientHandler client = new ClientHandler(clientSocket, (port + count));
            client.run();
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

    public ClientHandler(Socket socket, int port)
    {
        //Set up reference to associated socket...
        client = socket;
        dataPort = port;
    }

    public void run()
    {

        try {
            String received;
            String frstln;
            int port = 12000;
            int port1 = 12002;

            String clientCommand;
            byte[] data;
            File curDir = new File(".");
            File[] fileList = curDir.listFiles();
            try {
                outToClient = new DataOutputStream(client.getOutputStream());
            } catch (IOException ioEx) {
                System.out.print("Unable to set up port!");
            }
            try {
                inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
            } catch (IOException ioEx) {
                System.out.println("Unable to set up port!");
            }
            try {
                //outToClient.writeBytes("" + dataPort);
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

                System.out.println(clientCommand);//Debugging line remove later


                if (clientCommand.equals("list:")) {
                    try {
                        outToClient.writeBytes("" + port);
                        dataSocket = new Socket(client.getInetAddress(), port);
                    } catch (IOException ioEx) {
                        System.out.println("Unable to set up port!");
                    }
                    try {
                        dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
                    } catch (IOException ioEx) {
                        System.out.println("Unable to set up port!");
                    }
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
                    try {
                        dataOutToClient.writeBytes(fileNames);
                    } catch (IOException ioEx) {
                        System.out.println("Unable to set up port!");
                    }
                    try {
                        dataSocket.close();
                    } catch (IOException ioEx) {
                        System.out.println("Unable to set up port!");
                    }

                    System.out.println("Data Socket closed");
                }

                if (clientCommand.equals("retr:")) {

                    dataSocket = new Socket(client.getInetAddress(), port);
                    DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
                    BufferedInputStream dataInFromClient = new BufferedInputStream(dataSocket.getInputStream());

                    String fileName = inFromClient.readLine();
                    Boolean exists = false;

                    for (File f : fileList){
                        if (f.getName().equals(fileName)){
                            exists = true;
                        }
                    }

                    if (exists){
                        outToClient.writeBytes("200 OK");
                        File f = new File(fileName);
                        input = new Scanner(f);
                        String fileLine = input.nextLine();

                        while(fileLine != null) {
                            dataOutToClient.writeBytes(fileLine);
                            fileLine = input.nextLine();
                        }

                        dataOutToClient.writeBytes("eof");
                    } else {
                        outToClient.writeBytes("550");
                    }

                    dataSocket.close();
                }

                if (clientCommand.equals("stor")) {
                    //saves the file to the current directory.
                    try {
                        dataSocket = new Socket(client.getInetAddress(), port);
                    } catch (IOException ioEx) {
                        System.out.println("Unable to set up port!");
                    }

                    try {
                        dataFromClient = new DataInputStream(dataSocket.getInputStream());
                    } catch (IOException ioEx) {
                        System.out.println("Unable to set up port!");
                    }
                    byte[] b = new byte[1024];
                    String fileName = tokens.nextToken(); // This assumes we send the file name via the command line right after stor:
                    try {
                        out = new FileOutputStream(fileName);
                    } catch (IOException ioEx) {
                        System.out.println("Unable to set up port!");
                    }

                    try {
                        dataFromClient.read(b);
                    } catch (IOException ioEx) {
                        System.out.println("Unable to set up port!");
                    }
                    try {
                        out.write(b);
                    } catch (IOException ioEx) {
                        System.out.println("Unable to set up port!");
                    }
                    try {
                        out.close();
                    } catch (IOException ioEx) {
                        System.out.println("Unable to set up port!");
                    }
                    try {
                        dataSocket.close();
                    } catch (IOException ioEx) {
                        System.out.println("Unable to set up port!");
                    }
                    try {
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

        } catch (Exception e) {
            System.out.println("TOTAL FAILURE");
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






