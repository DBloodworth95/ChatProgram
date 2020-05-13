package ChatClient.src.com.cp;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

public class ChatClient {

    private final String serverName;
    private final int port;
    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferedIn;
    private final String username;

    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();

    //Chat client takes server name and port
    public ChatClient(String serverName, int port, String username) {
        this.serverName = serverName;
        this.port = port;
        this.username = username;
    }
    //When executed, we connect to the server using a instance of this class.
    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost", 8818, "guest");
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("ONLINE: " + login);
            }

            @Override
            public void offline(String login) {
                System.out.println("OFFLINE: " + login);
            }
        });
        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String login, String msg) {
                System.out.println("Got a message from " + login + ": " + msg);
            }
        });
        if(!client.connect()) {
            System.err.println("Connection failed!");
        } else {
            System.out.println("Connection Successful!");
            if(client.login("guest")) {
                System.out.println("Login successful!");

            } else {
                System.err.println("Login failed!");
            }
        }
    }

    public void msg(String sendTo, String msg) throws IOException {
        String cmd = "whisper " + sendTo + " " + msg + "\n";
        serverOut.write(cmd.getBytes());
    }

    public boolean login(String login) throws IOException {
        String cmd = "login " + login + " " + "\n";
        serverOut.write(cmd.getBytes());
        String response = bufferedIn.readLine();
        System.out.println("Response Line: " + response);
        if("online".equalsIgnoreCase(response)) {
            startMessageReader();
            return true;
        } else {
            return false;
        }
    }

    public void logoff() throws IOException {
        String cmd = "quit\n";
        serverOut.write(cmd.getBytes());
    }

    public void startMessageReader() {
        Thread t = new Thread() {
            @Override
            public void run() {
                readMessageLoop();
            }
        };
        t.start();
    }
    //Reads server messages
    public void readMessageLoop() {
        try {
            String line;
            while((line = bufferedIn.readLine()) != null) {
                String[] tokens = line.split(" ");
                if(tokens.length > 0) {
                    String cmd = tokens[0];
                    //Listens for online/offline presence of other clients.
                    if("online".equalsIgnoreCase(cmd)) {
                        handleOnline(tokens);
                    } else if("offline".equalsIgnoreCase(cmd)) {
                        handleOffline(tokens);
                    } else if("whisper".equalsIgnoreCase(cmd)) {
                        String[] tokensMsg = line.split(" ", 3);
                        handleMessage(tokensMsg);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void handleMessage(String[] tokensMsg) throws SQLException {
        String login = tokensMsg[1];
        String msg = tokensMsg[2];

        for(MessageListener listener : messageListeners) {
            listener.onMessage(login, msg);
        }
    }

    public void handleOffline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners) {
            listener.offline(login);
        }
    }

    public void handleOnline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners) {
            listener.online(login);
        }
    }

    //Manages the connection, creates a socket and generates IO streams to send/receive data.
    public boolean connect() {
        try {
            this.socket = new Socket(serverName, port);
            System.out.println("Client port is " + socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addUserStatusListener(UserStatusListener listener) {
        userStatusListeners.add(listener);
    }
    public void removeUserStatusListener(UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

    public String getUsername() {
        return username;
    }
}
