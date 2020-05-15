package ChatClient.src.com.cp;

import com.vdurmont.emoji.EmojiParser;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

@SuppressWarnings("ALL")
public class UserListPane extends JPanel implements UserStatusListener, MessageListener {
    private final ChatClient client;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    //Message pane content
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    DefaultListModel<String> chatHistory;
    JList<String> jChatHistory;
    private ArrayList<MessagePane> activeMessagePanes = new ArrayList<>();
    HashMap<String, String> emojiParse = new HashMap<>() {{
       put(":)", ":smiley:");
       put(";)", ":wink:");
       put(":(", ":worried:");
    }};

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
                    chatHistory = new DefaultListModel<>();
                    jChatHistory = new JList<>(chatHistory);
                    ArrayList<Message> msgHistory = new ArrayList<>();
                    String login = userList.getSelectedValue();
                    //Fetches conversation history and adds to JList.
                    String receivedQuery = "SELECT * FROM messages WHERE author ='" + login + "' AND sendto='" + client.getUsername() + "'";
                    String sentQuery = "SELECT * FROM messages WHERE author ='" + client.getUsername() + "' AND sendto='" + login + "'";
                    try {
                        PreparedStatement receivedStatement = queryHandler.getConnection().prepareStatement(receivedQuery);
                        ResultSet resultSetReceived = receivedStatement.executeQuery();
                        PreparedStatement sentStatement = queryHandler.getConnection().prepareStatement(sentQuery);
                        ResultSet resultSet = sentStatement.executeQuery();
                        while(resultSetReceived.next()) {
                            Message message = new Message.Builder()
                                    .author(resultSetReceived.getString("author"))
                                    .sendTo(resultSetReceived.getString("sendto"))
                                    .msg(resultSetReceived.getString("message"))
                                    .time(resultSetReceived.getTimestamp("time"))
                                    .build();
                            msgHistory.add(message);
                        }
                        while(resultSet.next()) {
                            Message message = new Message.Builder()
                                    .author(resultSet.getString("author"))
                                    .sendTo(resultSet.getString("sendto"))
                                    .msg(resultSet.getString("message"))
                                    .time(resultSet.getTimestamp("time"))
                                    .build();
                            msgHistory.add(message);
                        }
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }

                    //Sort messages by time sent.
                    msgHistory.sort(new Comparator<Message>() {
                        @Override
                        public int compare(Message message, Message t1) {
                            if(message.getTime() == null || t1.getTime() == null)
                                return 0;
                            return message.getTime().compareTo(t1.getTime());
                        }
                    });
                    //Loop through the HashMap which stores key sequences corresponding to a emoji alias
                    //Replaces the key sequence found in a message with the emoji alias.
                    //Parse the message to unicode to display emojis to UI.
                    for (Message message : msgHistory) {
                        String msg = message.getMsg();
                        for(HashMap.Entry<String, String> entry : emojiParse.entrySet()) {
                            if(msg.contains(entry.getKey()))
                                msg = msg.replace(entry.getKey(), entry.getValue());
                        }
                        String parseMsg = EmojiParser.parseToUnicode(msg);
                        //If message was sent by user, add "you: + message"
                        //Else add author + message
                        if (message.getAuthor().equalsIgnoreCase(client.getUsername()))
                            chatHistory.addElement("You: " + parseMsg);
                        else
                            chatHistory.addElement(message.getAuthor() + ": " + parseMsg);
                    }
                    //Displays the UI.
                    JFrame f = new JFrame("Message " + login);
                    MessagePane messagePane = new MessagePane.Builder()
                            .client(client)
                            .recipient(login)
                            .jListChatHistory(jChatHistory)
                            .chatHistoryModel(chatHistory)
                            .domainListModel(listModel)
                            .emojiParser(emojiParse)
                            .build();
                    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    f.setSize(500,500);
                    f.getContentPane().add(messagePane, BorderLayout.CENTER);
                    f.setVisible(true);
                    activeMessagePanes.add(messagePane);
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
        String login = userList.getSelectedValue();
        String query = "INSERT INTO messages(author, sendto, message) VALUES (?,?,?)";
        QueryHandler queryHandler = new QueryHandler();
        Connection myConnection = DriverManager.getConnection(queryHandler.getDbUrl(), queryHandler.getUsername(), queryHandler.getPassword());
        PreparedStatement preparedStatement = myConnection.prepareStatement(query);
        preparedStatement.setString(1, fromLogin);
        preparedStatement.setString(2, client.getUsername());
        preparedStatement.setString(3, msg);
        preparedStatement.execute();
        for(HashMap.Entry<String, String> entry : emojiParse.entrySet()) {
            if(msg.contains(entry.getKey()))
                msg = msg.replace(entry.getKey(), entry.getValue());
        }
        String parseMsg = EmojiParser.parseToUnicode(msg);
        if(login.equalsIgnoreCase(fromLogin)) {
            String line = fromLogin + ": " + parseMsg;
            listModel.addElement(line);
            chatHistory.addElement(line);
        }
    }
}
