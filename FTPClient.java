import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import javax.swing.*;

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
        int count = 2;
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
            String serverName = tokens.nextToken(); // pass the connect command
            serverName = tokens.nextToken();
            port1 = Integer.parseInt(tokens.nextToken());
            serverName = "127.0.0.1";
            System.out.println("You are connected to " + serverName);

            Socket ControlSocket = new Socket("127.0.0.1", 12000);//was serverName, port1

            while (isOpen && clientgo) {
                System.out.println("loop top");
                DataOutputStream outToServer = new DataOutputStream(ControlSocket.getOutputStream());

                DataInputStream inFromServer = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));
                //	BufferedReader inFromServer = new BufferedReader(new InputStreamReader(ControlSocket.getInputStream()));
                sentence = inFromUser.readLine();

                if (sentence.equals("list:")) {

                    port = port + count;
                    outToServer.writeBytes(port + " " + sentence + " " + '\n');
                    System.out.println(sentence+ "hello world");
                    ServerSocket welcomeData = new ServerSocket(port);
                    Socket dataSocket = welcomeData.accept();
                    System.out.println("fkdjbvbga");

                    //    DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
                    BufferedReader inData = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
                    while (notEnd) {//notEnd
                        //prints out the list of files available from the server.
                        modifiedSentence = inData.readLine();//readUTF
                        System.out.println(modifiedSentence);

                        String[] list = modifiedSentence.split(" ");

                        for (String s: list
                        ) {
                            System.out.println(s);
                        }
                        break;

                    }

                    count++;
                    welcomeData.close();
                    dataSocket.close();
                    System.out.println("\nWhat would you like to do next: \n retr: file.txt ||stor: file.txt  || quit: ");

                } else if (sentence.startsWith("retr: ")) {
                    //If the user wants to retrieve a file from the server.
                    port = port + count;
                    outToServer.writeBytes(port + " " + sentence + " " +'\n');
                    ServerSocket welcomeData = new ServerSocket(port);
                    Socket dataSocket = welcomeData.accept();

                    DataInputStream inData = new DataInputStream(dataSocket.getInputStream());
                    byte[] b = new byte[1024];

                    System.out.println("What file would you like?");
                    String fileName = sc.next();
                    File f = new File(fileName);

                    inData.read(b);

                    FileOutputStream fout = new FileOutputStream(f);
                    fout.write(b);

                    System.out.println(fileName + " has been written.");

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
