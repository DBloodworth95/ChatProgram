package ChatClient.src.com.cp;

import java.sql.SQLException;

public interface MessageListener {
    void onMessage(String login, String msg) throws SQLException;
}
