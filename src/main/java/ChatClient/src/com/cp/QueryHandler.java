package ChatClient.src.com.cp;
import java.sql.*;

public class QueryHandler {
    private final String dbUrl;
    private final String username;
    private final String password;

    public QueryHandler() {
        this.dbUrl = "jdbc:mysql://localhost:3306/sys";
        this.username = "root";
        this.password = "root";
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, username, password);
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
