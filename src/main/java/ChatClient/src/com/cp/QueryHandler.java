package ChatClient.src.com.cp;

import javax.management.Query;
import java.sql.*;

public class QueryHandler {
    private String dbUrl;
    private String username;
    private String password;

    public QueryHandler() {
        this.dbUrl = "jdbc:mysql://localhost:3306/sys";
        this.username = "root";
        this.password = "root";
    }

    public Connection getConnection() throws SQLException {
        Connection myConnection = DriverManager.getConnection(dbUrl, username, password);
        return myConnection;
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
