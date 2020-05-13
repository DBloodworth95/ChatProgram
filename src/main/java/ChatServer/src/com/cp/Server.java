package ChatServer.src.com.cp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    private final int serverPort;
    private ArrayList<ServerHelper> helperList = new ArrayList<>();

    public Server(int serverPort) {
        this.serverPort = serverPort;
    }
    //Server stores a list of helpers so we can control them.
    public List<ServerHelper> getHelperList() {
        return helperList;
    }

    @Override
    public void run() {
        try {
            //Server socket
            ServerSocket serverSocket = new ServerSocket(serverPort);
            //Continuously accept connections from client.
            while (true) {
                //Returns a socket
                System.out.println("Accepting client connections..");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepting connection from " + clientSocket);
                //Create a thread everytime a connection is accepted.
                //Our ServerHelper deals with the communication to our ServerSocket
                //Pass our ServerHelper the server so that it can communicate to it.
                ServerHelper helper = new ServerHelper(this, clientSocket);
                //Add out helper to the list of helpers when they connect.
                //Start the user/helpers stream.
                helperList.add(helper);
                helper.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Removes a user from our list of users when they logout.
    public void removeHelper(ServerHelper serverHelper) {
        helperList.remove(serverHelper);
    }
}
