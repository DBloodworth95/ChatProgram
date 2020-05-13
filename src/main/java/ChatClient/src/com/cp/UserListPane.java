package ChatClient.src.com.cp;

import javax.management.Query;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;

public class UserListPane extends JPanel implements UserStatusListener, MessageListener {
    private final ChatClient client;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    //Message pane content
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    DefaultListModel<String> chatHistory = new DefaultListModel<>();
    JList<String> jChatHistory = new JList<>(chatHistory);


    public UserListPane(ChatClient client) {
        this.client = client;
        this.client.addUserStatusListener(this);
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        setLayout(new BorderLayout());
        add(new JScrollPane(userList), BorderLayout.CENTER);
        client.addMessageListener(this);
        QueryHandler queryHandler = new QueryHandler();

        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() > 1) {
                    ArrayList<Message> msgHistory = new ArrayList<>();
                    String login = userList.getSelectedValue();
                    String receivedQuery = "SELECT * FROM messages WHERE author ='" + login + "' AND sendto='" + client.getUsername() + "'";
                    String sentQuery = "SELECT * FROM messages WHERE author ='" + client.getUsername() + "' AND sendto='" + login + "'";
                    try {
                        PreparedStatement receivedStatement = queryHandler.getConnection().prepareStatement(receivedQuery);
                        ResultSet resultSetReceived = receivedStatement.executeQuery();
                        PreparedStatement sentStatement = queryHandler.getConnection().prepareStatement(sentQuery);
                        ResultSet resultSet = sentStatement.executeQuery();
                        while(resultSetReceived.next()) {
                            Message message = new Message(resultSetReceived.getString("author"), resultSetReceived.getString("sendto"), resultSetReceived.getString("message"), resultSetReceived.getTimestamp("time"));
                            msgHistory.add(message);
                        }
                        while(resultSet.next()) {
                            Message message = new Message(resultSet.getString("author"), resultSet.getString("sendto"),resultSet.getString("message"), resultSet.getTimestamp("time"));
                            msgHistory.add(message);
                        }
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    msgHistory.sort(new Comparator<Message>() {
                        @Override
                        public int compare(Message message, Message t1) {
                            if(message.getTime() == null || t1.getTime() == null)
                                return 0;
                            return message.getTime().compareTo(t1.getTime());
                        }
                    });
                    for (Message message : msgHistory) {
                        if (message.getAuthor().equalsIgnoreCase(client.getUsername()))
                            chatHistory.addElement("You: " + message.getMsg());
                        else
                            chatHistory.addElement(message.getAuthor() + ": " + message.getMsg());
                    }

                    JFrame f = new JFrame("Message " + login);
                    MessagePane messagePane = new MessagePane(client, login, jChatHistory, chatHistory, listModel);
                    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    f.setSize(500,500);
                    f.getContentPane().add(messagePane, BorderLayout.CENTER);
                    f.setVisible(true);
                    f.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            chatHistory.removeAllElements();
                        }
                    });
                }
            }
        });
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient("localhost", 8818, "guest");
        UserListPane userListPane = new UserListPane(client);
        JFrame frame = new JFrame("User List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 600);
        frame.getContentPane().add(userListPane, BorderLayout.CENTER);
        frame.setVisible(true);
        if(client.connect()) {
            try {
                client.login("guest");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void online(String login) {
        userListModel.addElement(login);
    }

    @Override
    public void offline(String login) {
        userListModel.removeElement(login);
    }

    @Override
    public void onMessage(String fromLogin, String msg) throws SQLException {
        String query = "INSERT INTO messages(author, sendto, message) VALUES (?,?,?)";
        QueryHandler queryHandler = new QueryHandler();
        Connection myConnection = DriverManager.getConnection(queryHandler.getDbUrl(), queryHandler.getUsername(), queryHandler.getPassword());
        PreparedStatement preparedStatement = myConnection.prepareStatement(query);
        preparedStatement.setString(1, fromLogin);
        preparedStatement.setString(2, client.getUsername());
        preparedStatement.setString(3, msg);
        preparedStatement.execute();
        String login = userList.getSelectedValue();
        String line = fromLogin + ": " + msg;
        listModel.addElement(line);
        chatHistory.addElement(line);
        System.out.println(line);
    }
}
