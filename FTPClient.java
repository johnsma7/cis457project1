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
        boolean isOpen = true;
        int number = 1;
        boolean notEnd = true;
        String statusCode;
        boolean clientgo = true;
        int port = 12000, port1 = 0;//was 0
        int dataAdd = 1;
        Scanner sc = new Scanner(System.in);


        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter connect <server name> <port number>");
	sentence = inFromUser.readLine();
        StringTokenizer tokens = new StringTokenizer(sentence);
        if (sentence.startsWith("connect")) {
            if(tokens.countTokens() < 2){
                System.out.println("Improper format");
                throw new NoSuchElementException();
            }
	    System.out.println("Choose from the following commands:");
	    System.out.println("list: displays files in the current server directory");
	    System.out.println("retr: <filename.txt> retrieves the file if it is in the current server directory");
	    System.out.println("stor: <filename.txt> passes the filename and stores it on the server side");
	    System.out.println("quit: closes the client connection");

            String serverName = tokens.nextToken();
            serverName = tokens.nextToken();// pass the connect command
            port1 = Integer.parseInt(tokens.nextToken());
            Socket ControlSocket = new Socket(serverName, port1);
            System.out.println("You are connected to " + serverName);

            while (isOpen && clientgo) {
                DataOutputStream outToServer = new DataOutputStream(ControlSocket.getOutputStream());

                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(ControlSocket.getInputStream()));
                sentence = inFromUser.readLine();

                if (sentence.equals("list: ")) {

                    outToServer.writeBytes(port + " " + sentence + " " + '\n');
                 
                    ServerSocket welcomeData = new ServerSocket(port);
                    Socket dataSocket = welcomeData.accept();
            
                    BufferedReader inData = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

                    //prints out the list of files available from the server.
                    modifiedSentence = inData.readLine();//readUTF
                    System.out.println(modifiedSentence);

                    String[] list = modifiedSentence.split(" ");

                    for (String s: list
                    ) {
                        System.out.println(s);
                    }

                    welcomeData.close();
                    dataSocket.close();
                    System.out.println("\nWhat would you like to do next: \n retr: file.txt ||stor: file.txt  || quit: ");

                } else if (sentence.startsWith("retr: ")) {
                    //If the user wants to retrieve a file from the server.
                    port = port + dataAdd;
                    outToServer.writeBytes(port + " " + sentence + " " +'\n');
                    ServerSocket welcomeData = new ServerSocket(port);
                    Socket dataSocket = welcomeData.accept();
                    PrintWriter pw;

                    tokens = new StringTokenizer(sentence);
                    String command = tokens.nextToken();// purely to get "retr:" out of the way
                    String fileName = tokens.nextToken();// desired file's name

                    BufferedReader inData = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

                    outToServer.writeBytes(fileName);
                    File f = new File(fileName);
                    pw = new PrintWriter(f);

                    if (inFromServer.readLine().equals("200 OK")){
                        System.out.println("200 OK");
                        String fileLine = inData.readLine();

                        while(fileLine.equals("eof")){
                            System.out.println("eof");
                            pw.println(fileLine);
                            fileLine = inData.readLine();
                        }
                        dataSocket.close();

                    } else if (inFromServer.readLine().equals("550")) {
                        dataSocket.close();
                        System.out.println("\nNot a valid command\nWhat would you like to do next: \n list: || retr: file.txt || stor: file.txt || quit: ");
                    }


                } else if (sentence.startsWith("stor: ")) {
                    //If the user wants to store a file on the server

                } else if (sentence.startsWith("quit: ")) {
                    //If the user wants to end the application
                    outToServer.writeBytes(port + " " +sentence + " " + '\n');
                    System.out.println("Closing the server...");
                    isOpen = false;
                } else {
                    //If invalid input is given
                    System.out.println("\nNot a valid command\nWhat would you like to do next: \n list: || retr: file.txt || stor: file.txt || quit: ");
                }
                //....................................................
            }

            ControlSocket.close();
        }
        else{
            System.out.println("Improper input");
        }
    }
}
