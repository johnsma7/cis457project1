import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import javax.swing.*;

/*
Some code is modified from what was found off of StackOverflow.com
 */

class FTPClient {

    public static void main(String argv[]) throws Exception {
        String sentence;
        String modifiedSentence;
	BufferedReader inFromUser=null;
	StringTokenizer tokens=null;
        boolean isOpen = true;
        boolean clientgo = true;
	boolean valid = false;
        int port = 0;
       	int port1 = 0;

	while(!valid){
        	inFromUser = new BufferedReader(new InputStreamReader(System.in));
        	System.out.println("Enter connect <server name> <port>");
        	sentence = inFromUser.readLine();
        	tokens = new StringTokenizer(sentence);

        	if (sentence.startsWith("connect")) {
            		if(tokens.countTokens() != 3){
                		System.out.println("Improper format");
			}else{
				String[] temp = sentence.split(" ");
				if(Integer.parseInt(temp[2]) > 1024){
					valid = true;
				}else{
					System.out.println("Port must be greater than 1024.");
				}

			}	
		}else{
			System.out.println("Improper Format.");
		}
	}
          String serverName = tokens.nextToken(); // pass the connect command
          serverName = tokens.nextToken();
          port1 = Integer.parseInt(tokens.nextToken());
          System.out.println("You are connected to " + serverName);
	  Socket ControlSocket = new Socket(serverName, port1);//was serverName, port1
	  port = port1+ 1;
            System.out.println("");
            System.out.println("Choose from the following commands: ");
            System.out.println("list: supplies a list of files from the current server directory");
            System.out.println("retr: <filename.txt> retrieves the file if it is in the current server directory");
            System.out.println("stor: <filename.txt> passes the filename and stores it on the server side");
            System.out.println("quit: closes the client connection");

                        while (isOpen && clientgo) {
                DataOutputStream outToServer = new DataOutputStream(ControlSocket.getOutputStream());

                // DataInputStream inFromServer = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(ControlSocket.getInputStream()));
                sentence = inFromUser.readLine();


                //	String fromServer = inFromServer.readLine();
                //	StringTokenizer token = new StringTokenizer(fromServer);
                //	String frstIn= token.nextToken();
                //	port1 = Integer.parseInt(frstIn);
                //	System.out.println(port1);
                if (sentence.equals("list:")){

                    outToServer.writeBytes(port + " " + sentence + " " + '\n');
                    ServerSocket welcomeData = new ServerSocket(port);
                    Socket dataSocket = welcomeData.accept();

                    BufferedReader inData = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

                    System.out.println("");
                    System.out.println("Files on server:");

                    //prints out the list of files available from the server.
                    modifiedSentence = inData.readLine();//readUTF

                    String[] list = modifiedSentence.split(" ");

                    for (String s: list
                    ) {
                        System.out.println(s);
                    }

                    System.out.println("(End of files)");
                    welcomeData.close();
                    dataSocket.close();
                    System.out.println("\nWhat would you like to do next: \n retr: file.txt ||stor: file.txt  || quit: ");

                } else if (sentence.startsWith("retr: ")) {
                    //If the user wants to retrieve a file from the server.
		    System.out.println("Downloading file from server ...");
                    outToServer.writeBytes(port + " " + sentence + " " +'\n');
                    ServerSocket welcomeData = new ServerSocket(port);
                    Socket dataSocket = welcomeData.accept();

                    BufferedReader inData = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

                    BufferedWriter bw;// This is to write the file.

                    //Get the file name for use on the client side
                    String[] s = sentence.split(" ");
                    String fileName = s[1];

                    String statusCode = inFromServer.readLine();

                    if (statusCode.equals("200 OK")){
                        System.out.println("File Downloaded!");

                        /*
                        These lines are for testing when the client and server are in the same directory.*/
                        String newFileName = fileName.replaceFirst("[.][^.]+$", "");
                        File f = new File(newFileName + 1 + ".txt");

                        //File f = new File(fileName);
                        bw = new BufferedWriter(new FileWriter(f));

                        String fileLine = inData.readLine();

                        if (fileLine != null){
                            while(!fileLine.equals("EOF")){
                                bw.write(fileLine + "\n");
                                fileLine = inData.readLine();

                            }
                            dataSocket.close();
                            welcomeData.close();
                            bw.close();
                        }

                    } else if (statusCode.equals("550")) {
                        dataSocket.close();
                        welcomeData.close();
                    } else {
                        System.out.println("Something has gone wrong..." + statusCode);
                        dataSocket.close();
                        welcomeData.close();
                    }
                   System.out.println("\nWhat would you like to do next: \n retr: file.txt ||stor: file.txt  || quit: ");

                } else if (sentence.startsWith("stor: ")) {
                    //If the user wants to store a file on the server

                    String[] cList = sentence.split(" ");
                    String fileName = cList[1];

                    File curDir = new File(".");
                    File[] fileList = curDir.listFiles();
                    boolean exists = false;

                    for (File f : fileList){
                        if (f.getName().equals(fileName)){
                            exists = true;
                            break;
                        }
                    }

                    if (exists){
			System.out.println("");
                        System.out.println("Uploading file to server...");
                        File f = new File(fileName);
                        BufferedReader br = new BufferedReader(new FileReader(f));

                        outToServer.writeBytes(port + " " + sentence + " " +'\n');
                        ServerSocket welcomeData = new ServerSocket(port);
                        Socket dataSocket = welcomeData.accept();
                        DataOutputStream dataOutToServer = new DataOutputStream(dataSocket.getOutputStream());

                        String fileLine = br.readLine();
                        while(fileLine != null){

                            dataOutToServer.writeBytes(fileLine + "\n");
                            fileLine = br.readLine();
                        }

                        dataSocket.close();
                        welcomeData.close();
                        br.close();

			System.out.println("File uploaded!");

                    }else{

                        System.out.println("\nFile doesn't exist.");
		    }
                    System.out.println("\nWhat would you like to do next: \n retr: file.txt ||stor: file.txt  || quit: ");

                } else if (sentence.startsWith("quit:")) {
                    //If the user wants to end the application
                    outToServer.writeBytes(port + " " +sentence + " " + '\n');
                    System.out.println("Closing connection...");
                    System.out.println("Terminated");
                    isOpen = false;
                } else {
                    //If invalid input is given
                    System.out.println("\nNot a valid command\nWhat would you like to do next: \n list: || retr: file.txt || stor: file.txt || quit: ");
                }
                //....................................................
            }

            ControlSocket.close();

    }
}
