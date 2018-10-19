import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class FTPServer extends Thread{

    private Socket client;
    DataOutputStream dout;
    BufferedReader din;
    File curDir = new File(".");
    File[] files = curDir.listFiles();

    FTPServer (Socket commandSocket){
        //Get the connection from the multithreading class
        client = commandSocket;
    }

    public void run() {

        String fromClient;
        String[] fc = null;
        String command;
        int dataPort;

        //TODO : logic for retrieving and processing commands goes here
        try {
            while (true) {

                //Make the control out to client and in from client lines
                dout = new DataOutputStream(client.getOutputStream());
                din = new BufferedReader(new InputStreamReader(client.getInputStream()));

                //Receive a command from the client
                fromClient = din.readLine();
                fc = fromClient.split(" ");

                if (fc.length > 0 && !fc[0].equals("") ) {
                    //command must be list or quit as a file name was not received
                    dataPort = Integer.parseInt(fc[0]);
                    command = fc[1];

                    switch (command) {
                        case "list:":
                            list(dataPort);
                            break;
                        case "quit:":
                            quit();
                            break;
                        case "retr:":
                            retr(dataPort, fc[2]);
                            break;
                        case "stor:":
                            stor(dataPort, fc[2]);
                            break;
                        default:
                            dout.writeBytes("NC\n");//NC == No Command received or command not understood
                    }
                } else {
                    dout.writeBytes("NC\n");//NC == No Command received or command not understood
                }

                //Flush dout to make sure there are no lingering characters
                dout.flush();

            }



        } catch (IOException e) {
            System.out.println("Something went wrong when processing a command.");
            System.out.println(e);

        }
    }

    private void list(int dataPort) throws IOException{
        //Send something to the client to make sure it knows to get the data coming in.
        dout.writeBytes("FR\n");//FR == Fulfilling Request

        //Sets up the data connection
        Socket dataSocket = new Socket(client.getInetAddress(), dataPort);
        DataOutputStream dataOut = new DataOutputStream(dataSocket.getOutputStream());

        //The string that includes the file names as a single string.
        String fileList = "";

        //Adds the file names to the string separated by spaces
        if (files != null){
            for (File f:
                 files) {
                if(f.exists()){
                    fileList = fileList + f.getName() + " ";
                }
            }
        }

        //Sends the string to the client.
        dataOut.writeBytes(fileList + "\n");

        //Close the sockets and streams.
        dataOut.flush();
        dataOut.close();
        dataSocket.close();
    }

    private void retr(int dataPort, String fileName) throws IOException{
        //Send the client a confirmation of request received
        dout.writeBytes("FR\n");

        //Sets up the data connection
        Socket dataSocket = new Socket(client.getInetAddress(), dataPort);
        DataOutputStream dataOut = new DataOutputStream(dataSocket.getOutputStream());

        //Boolean to say whether the file exists or not
        boolean exists = false;

        //Search for the file and set exists
        for (File f:
             files) {
            if (f.getName().equals(fileName)) {
                exists = true;
            }
        }

        //If the file is real send 200 OK on the command line to the client
        if (exists) {

            //Send the 200 OK
            dout.writeBytes("200 OK\n");

            //Open the file and reader to read it in
            File f = new File(fileName);
            BufferedReader br = new BufferedReader(new FileReader(f));

            //Read in each line of the file and send it to the client
            String fileLine = br.readLine();

            while (fileLine != null) {
                dataOut.writeBytes(fileLine + "\n");
                fileLine = br.readLine();
            }

            //Send the EOF to the client
            dataOut.writeBytes("EOF\n");

            //close the streams and sockets
            br.close();
            dataOut.flush();
            dataOut.close();
            dataSocket.close();

        } else {
            //File doesn't exist, send 550
            dout.writeBytes("550\n");

        }
    }

    private void stor(int dataPort, String fileName) throws IOException{
        //Send the client a confirmation of command received
        dout.writeBytes("FR\n");

        //Set up the data connection
        Socket dataSocket = new Socket(client.getInetAddress(), dataPort);
        BufferedReader dataIn = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

        //Make the file that is going to be saved to the server
        //And make the writer to write the file
        File f = new File(fileName);
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));

        String fileLine = dataIn.readLine();

        while (fileLine != null) {
            bw.write(fileLine + "\n");
            fileLine = dataIn.readLine();
        }

        //Close the streams and socket
        bw.close();
        dataIn.close();
        dataSocket.close();
    }

    private void quit() throws IOException{
        //TODO: I don't know how this was implemented...
    }
}

