package ChatServer.src.com.cp;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ServerHelper extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;

    //ServerHelper takes a Server and a Socket as a parameter.
    public ServerHelper(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    //Handles our thread when it's alive.
    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Communicates with the client.
    private void handleClientSocket() throws IOException {
        //Create an input/output stream for our client to send and receive communications
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        //Reads lines sent from the client.
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        //Reads the lines sent to the server and acts accordingly.
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(" ");
            if (tokens.length > 0) {
                String cmd = tokens[0];
                if("quit".equalsIgnoreCase(cmd)) {
                    handleLogout();
                    break;
                } else if("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                } else if("whisper".equalsIgnoreCase(cmd)) {
                    String[] tokensMsg = line.split(" ", 3);
                    handleMessage(tokensMsg);
                } else if("msg".equalsIgnoreCase(cmd)) {
                    String[] tokensMsg = line.split(" ", 2);
                    handleGlobalMessage(tokensMsg);
                } else {
                    String msg = "Invalid command! '" + cmd + "'" + "\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }
        clientSocket.close();
    }
    //Handles a message request sent from a user to another
    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String body = tokens[2];
        //Loop through our list of users and if the login matches that of the message command, send the message to the user
        List<ServerHelper> helperList = server.getHelperList();
        for(ServerHelper helper : helperList) {
            if(sendTo.equalsIgnoreCase(helper.getLogin())) {
                String outMsg = "Whisper" + " " + login + " " + body + "\n";
                helper.send(outMsg);
            }
        }
    }

    //Handles a global message request sent from a user to server
    private void handleGlobalMessage(String[] tokens) throws IOException {
        String body = tokens[1];
        //Loop through our list of users and if the login matches that of the message command, send the message to the user
        List<ServerHelper> helperList = server.getHelperList();
        for(ServerHelper helper : helperList) {
            String outMsg = login + ": " + body + "\n";
            helper.send(outMsg);
        }
    }

    private void handleLogout() throws IOException {
        server.removeHelper(this);
        List<ServerHelper> helperList = server.getHelperList();
        String offlineMsg = "Offline" + " " + login + " has logged out!" + "\n";
        //Loop through every user(helper) who's logged in and send the logout message.
        for(ServerHelper helper : helperList) {
            //Prevents the user who's logging in's username from showing up in the online list
            if(!login.equalsIgnoreCase(helper.getLogin())) {
                helper.send(offlineMsg);
            }
        }
        clientSocket.close();
    }

    public String getLogin() {
        return login;
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if(tokens.length == 2) {
            String login = tokens[1];


                String msg = "online\n";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("User " + login + " has logged in!");
                //Store a message that gets sent to every user when they login.
                //Gets the list of helpers so we can notify everyone that a new user has logged in.
                String onlineMsg = "Online" + " " + login + " has logged in!" + "\n";
                List<ServerHelper> helperList = server.getHelperList();

                //Loop through every user who's currently logged in, let the user who just logged in know who is currently online.
                for(ServerHelper helper : helperList) {
                    //Prevents nulls from showing up (nulls are people who are connected but not logged in)
                    if(helper.getLogin() != null) {
                        //Prevents the user who's logging in's username from showing up in the online list
                        if(!login.equalsIgnoreCase(helper.getLogin())) {
                            //Display the people who are currently online
                            String currentlyOnline = "Currently online: " + helper.getLogin() + "\n";
                            send(currentlyOnline);
                        }
                    }
                }
                //Loop through every user(helper) and send the login message.
                for(ServerHelper helper : helperList) {
                    //Prevents the user who's logging in's username from showing up in the online list
                    if(!login.equalsIgnoreCase(helper.getLogin())) {
                        helper.send(onlineMsg);
                    }
                }
        }
    }
    //Writes a message.
    private void send(String msg) throws IOException {
        //If the login isn't null (Basically anyone who's confirmed their credentials) then write the message.
        if(login != null)
        outputStream.write(msg.getBytes());
    }
}
