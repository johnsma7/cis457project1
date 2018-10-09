import java.io.*;
import java.net.*;
import java.util.*;
import java.*;
//.................import??
//.................

/*
Some code found in this class was adapted form code found on stackoverflow.com

 */
public class ftpserver {
    public static void main(String args[]) throws Exception {
        int port = 12000;


        String fromClient;
        String clientCommand;
        byte[] data;
        File curDir = new File(".");
        File[] fileList = curDir.listFiles();

        ServerSocket welcomeSocket = new ServerSocket(port);
        String frstln;

        while (true) {


            Socket connectionSocket = welcomeSocket.accept();

            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

            fromClient = inFromClient.readLine();

            StringTokenizer tokens = new StringTokenizer(fromClient);
            frstln = tokens.nextToken();
            port = Integer.parseInt(frstln);
            clientCommand = tokens.nextToken();

            if (clientCommand.equals("list:")) {

                Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
                DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
                //list everything in the current directory and send to client

                String fileNames = "";

                for (File f : fileList) {
                    if (f.exists()) {
                        fileNames = fileNames + f.getName() + " ";
                    }
                }

                dataOutToClient.writeBytes(fileNames);

                dataSocket.close();
                System.out.println("Data Socket closed");
            }

            //......................

            if (clientCommand.equals("retr:")) {
                Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
                DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());

                String fileName = inFromClient.readLine();
                boolean exists = false;

                for (int i = 0; i < fileList.length; i++) {
                    if (fileList[i].getName() == fileName)
                        exists = true;
                }

                if (exists) {
                    FileInputStream fs = new FileInputStream(fileName);
                    sendBytes(fs, dataOutToClient);
                } else {
                    dataOutToClient.writeBytes("File doesn't exist");
                }

                dataSocket.close();
                System.out.println("Data socket closed");
            }

            if (clientCommand.equals(""))
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
