package ChatClient.src.com.cp;

import com.vdurmont.emoji.EmojiParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

public class MessagePane extends JPanel {
    private final ChatClient client;
    private final String login;
    private final HashMap<String, String> emojiParse;
    private final DefaultListModel<String> otherListModel;
    private JTextField inputField = new JTextField();

    public MessagePane(ChatClient client, String login, JList<String>messageList, DefaultListModel<String> listModel, DefaultListModel<String> otherListModel, HashMap<String, String> emojiParse) {
        this.client = client;
        this.login = login;
        this.emojiParse = emojiParse;
        this.otherListModel = otherListModel;
        setLayout(new BorderLayout());
        add(new JScrollPane(messageList), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
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
                    listModel.addElement("You: " + parseMsg);
                    otherListModel.addElement(client.getUsername() + ":" + " " + msg);
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
            }
        });
    }
    public JTextField getInputField() {
        return inputField;
    }

    public void setInputField(JTextField inputField) {
        this.inputField = inputField;
    }

    public DefaultListModel<String> getModel() {
        return otherListModel;
    }
}
