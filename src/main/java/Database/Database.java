package Database;

import java.sql.*;

public class Database {
    // questions path: "jdbc:sqlite::resource:https://quidology.github.io/resources/questions.db";

    public static Connection connect(String url)throws SQLException {
        try {
            return DriverManager.getConnection(url);
        }
        catch(SQLException e){
            System.err.println("ERROR: " + e);
        }
        return null;
    }

    public static void createNewTable(Connection conn) {

        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS users (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	name text NOT NULL,\n"
                + "	password text NOT NULL\n"
                + ");";

        try (
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void insertUser(String name, String password, Connection conn) {
        String sql = "INSERT INTO users(name,password) VALUES(?,?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
