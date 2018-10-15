import java.io.*;
import java.net.*;
import java.util.*;

/*
Some code found in this class was adapted form code found on stackoverflow.com

 */
public class ftpserver {
    public static void main(String args[]) throws Exception {
        int port = 12000;

		while (true) {

			ServerSocket welcomeSocket = new ServerSocket(port);


			ClientHandler client = new ClientHandler(welcomeSocket);
			client.run();

		}
	}
}

class ClientHandler extends Thread {

	private Socket client;
	private Scanner input;
	private PrintWriter output;
	private ServerSocket s;

	public ClientHandler(Socket socket)
	{
		//Set up reference to associated socket...
		client = socket;
	}

	public void run()
	{
		String received;
		String frstln;
        int port = 12000;
        int port1 = 12002;

        String fromClient;
        String clientCommand;
        byte[] data;
        File curDir = new File(".");
        File[] fileList = curDir.listFiles();

        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

        fromClient = inFromClient.readLine();

            StringTokenizer tokens = new StringTokenizer(fromClient);
            frstln = tokens.nextToken();
            port = Integer.parseInt(frstln);
            clientCommand = tokens.nextToken();

        if (clientCommand.equals("list:")) {
		    System.out.println(port);
            Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);

			DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
			//list everything in the current directory and send to client

            String fileNames = "";

            if (fileList != null) {

                for (File f : fileList) {
                    if (f.exists()) {
                        fileNames = fileNames + f.getName() + " ";
			            System.out.println(f.getName());
                    }
                }
            } else {
			    System.out.println("file list null");
            }

            dataOutToClient.writeBytes(fileNames);

            dataSocket.close();
            System.out.println("Data Socket closed");
		}

        if (clientCommand.equals("retr:")) {
            Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
            DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());

            byte[] b;
            String fileName = inFromClient.readLine();
            boolean exists = false;
            int byteSize = 0;
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].getName() == fileName) {
                    exists = true;
                    byteSize = (int) fileList[i].length();
                    b = new byte[byteSize];
                }

            }

            if (exists && byteSize > 0) {
                //if there is a file...
                FileInputStream fs = new FileInputStream(fileName);
                sendBytes(fs, dataOutToClient);

            } else {
                dataOutToClient.writeBytes("File doesn't exist");
            }

            dataSocket.close();
            System.out.println("Data socket closed");
        }

        if (clientCommand.equals("stor")){
            //saves the file to the current directory.
            Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
            DataInputStream dataFromClient = new DataInputStream(dataSocket.getInputStream());

            byte[] b = new byte[1024];
            String fileName = tokens.nextToken(); // This assumes we send the file name via the command line right after stor:
            FileOutputStream out = new FileOutputStream(fileName);

            dataFromClient.read(b);
            out.write(b);
            out.close();
            dataSocket.close();
            outToClient.writeBytes("File stored.");

        }
	    if (clientCommand.equals("quit:")){
		    System.out.println("Closing the server...");
		    break;
	    }
	    welcomeSocket.close();
	}

    private static void sendBytes(FileInputStream fs, DataOutputStream data) throws Exception {
        byte[] buffer = new byte[1024];
        int bytes = 0;

        while ((bytes = fs.read(buffer)) != -1) {
            data.write(buffer, 0, bytes);
        }
    }

}






