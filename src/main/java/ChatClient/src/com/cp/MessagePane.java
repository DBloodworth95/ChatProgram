package ChatClient.src.com.cp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MessagePane extends JPanel {
    private final ChatClient client;
    private final String login;
    private JTextField inputField = new JTextField();

    public MessagePane(ChatClient client, String login, JList<String>messageList, DefaultListModel<String> listModel, DefaultListModel<String> otherListModel) {
        this.client = client;
        this.login = login;
        setLayout(new BorderLayout());
        add(new JScrollPane(messageList), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    String msg = inputField.getText();
                    client.msg(login, msg);
                    listModel.addElement("You: " + msg);
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
}
