import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FTPMulti {

    private final static int PORT = 12000;
    private static ServerSocket welcome;

    public static void main(String[] args) {

        FTPServer s;

        //Set up the welcome port for clients to connect to.
        try {

            welcome = new ServerSocket(PORT);

        } catch (IOException e) {

            System.out.println("Unable to open welcome socket.");
            e.printStackTrace();
            System.exit(-1);
        }

        //Allow clients to connect and begin processing their requests
        while (true) {
            try {

                Socket client = welcome.accept();
                s = new FTPServer(client);
                Thread t = new Thread(s);
                t.start();

            } catch (IOException e) {
                System.out.println("Client accept failed to set up connection.");
                e.printStackTrace();
            }
        }
    }
}
