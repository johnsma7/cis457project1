package ClientSpace;

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
        sentence = inFromUser.readLine();
        StringTokenizer tokens = new StringTokenizer(sentence);
        System.out.println("Hello");

        if (sentence.startsWith("connect")) {
            if(tokens.countTokens() < 2){
                System.out.println("Improper format");
                throw new NoSuchElementException();

            }
            String serverName = tokens.nextToken();
            serverName = tokens.nextToken();// pass the connect command
            port1 = Integer.parseInt(tokens.nextToken());
            Socket ControlSocket = new Socket(serverName, port1);
            System.out.println("You are connected to " + serverName);

            while (isOpen && clientgo) {
                System.out.println("loop top");// Debugging line, remove
                DataOutputStream outToServer = new DataOutputStream(ControlSocket.getOutputStream());

                //DataInputStream inFromServer = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(ControlSocket.getInputStream()));
                sentence = inFromUser.readLine();

                if (sentence.equals("list: ")) {

                    port = port + dataAdd;
                    outToServer.writeBytes(port + " " + sentence + " " + '\n');
                    System.out.println(sentence+ "hello world");//Debugging line, remove later
                    ServerSocket welcomeData = new ServerSocket(port);
                    Socket dataSocket = welcomeData.accept();
                    System.out.println("fkdjbvbga");//Debugging line, remove later

                    //    DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
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
                    String command = tokens.nextToken();// purely to get "retr:" out of the way so we can grab the next token
                    String fileName = tokens.nextToken();// desired file's name

                    BufferedReader inData = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

                    outToServer.writeBytes(fileName);

                    String newFileName = fileName.replaceFirst("[.][^.]+$", "");

                    File f = new File(newFileName + 1 + ".txt");
                    pw = new PrintWriter(f);

                    statusCode = inFromServer.readLine();
                    System.out.println(statusCode);

                    if (statusCode.equals("200 OK")){
                        System.out.println("200 OK");
                        String fileLine = inData.readLine();

                        while(!fileLine.equals("eof")){
                            System.out.println("eof");
                            pw.println(fileLine);
                            fileLine = inData.readLine();
                        }
                        dataSocket.close();

                    } else if (statusCode.equals("550")) {
                        dataSocket.close();
                        System.out.println("\nNot a valid command\nWhat would you like to do next: \n list: || retr: file.txt || stor: file.txt || quit: ");
                    } else {
                        System.out.println("Something has gone wrong..." + statusCode);
                        dataSocket.close();
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
