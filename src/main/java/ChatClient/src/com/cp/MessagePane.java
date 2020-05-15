package ChatClient.src.com.cp;

import com.vdurmont.emoji.EmojiParser;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

@SuppressWarnings("SqlNoDataSourceInspection")
public class MessagePane extends JPanel {
    private final ChatClient client;
    private final String login;
    private final HashMap<String, String> emojiParse;
    private final DefaultListModel<String> listModel;
    private final JTextField inputField = new JTextField();

    public static class Builder {
        private ChatClient client;
        private String login;
        private HashMap<String, String> emojiParse;
        private DefaultListModel<String> listModel;
        private DefaultListModel<String> chatHistoryModel;
        private JList<String> jChatHistory;

        public Builder() {

        }
        public Builder client(ChatClient val) {
            client = val;
            return this;
        }
        public Builder recipient(String val) {
            login = val;
            return this;
        }
        public Builder emojiParser(HashMap<String, String> val) {
            emojiParse = val;
            return this;
        }
        public Builder domainListModel(DefaultListModel<String> val) {
            listModel = val;
            return this;
        }
        public Builder chatHistoryModel(DefaultListModel<String> val) {
            chatHistoryModel = val;
            return this;
        }
        public Builder jListChatHistory(JList<String> val) {
            jChatHistory = val;
            return this;
        }
        public MessagePane build() {
            return new MessagePane(this.jChatHistory, this.chatHistoryModel,this);
        }
    }

    public MessagePane(JList<String>jChatHistory, DefaultListModel<String> chatHistoryModel, Builder builder) {
        client = builder.client;
        login = builder.login;
        emojiParse = builder.emojiParse;
        listModel = builder.listModel;
        setLayout(new BorderLayout());
        add(new JScrollPane(jChatHistory), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        inputField.addActionListener(actionEvent -> {
            try {
                String msg = inputField.getText();
                client.msg(login, msg);
                //Loop through the HashMap which stores key sequences corresponding to a emoji alias
                //Replaces the key sequence found in a message with the emoji alias.
                //Parse the message to unicode to display emojis to UI.
                for(HashMap.Entry<String, String> entry : emojiParse.entrySet()) {
                    if(msg.contains(entry.getKey()))
                        msg = msg.replace(entry.getKey(), entry.getValue());
                }
                String parseMsg = EmojiParser.parseToUnicode(msg);
                chatHistoryModel.addElement("You: " + parseMsg);
                listModel.addElement(client.getUsername() + ":" + " " + msg);
                inputField.setText("");

                //Database update.
                String query = "INSERT INTO messages(author, sendto, message) VALUES (?,?,?)";
                QueryHandler queryHandler = new QueryHandler();
                Connection myConnection = DriverManager.getConnection(queryHandler.getDbUrl(), queryHandler.getUsername(), queryHandler.getPassword());
                PreparedStatement preparedStatement = myConnection.prepareStatement(query);
                preparedStatement.setString(1, client.getUsername());
                preparedStatement.setString(2, login);
                preparedStatement.setString(3, msg);
                preparedStatement.execute();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
