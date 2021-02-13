package Login;

import BCrypt.BCrypt;
import Dashboard.DashboardController;
import Database.Database;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Dashboard.DashboardController.dashWindow;

public class AuthController {

    private static final Pattern userNamePattern = Pattern.compile("^[a-z0-9_-]{6,14}$");
    private static final Pattern passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
    //password patter: start of string, a digit must occur at least once,
    // a lower case letter must occur at least once
    //an upper case letter must occur at least once
    //a special character must occur at least once
    //no whitespace allowed in the entire string
    //anything, at least eight places though
    //Username is case-insensitive
    //Must be at least 8 characters in length. Make sure to include at least one lowercase, one uppercase, one special character and no whitespaces
    String sql;
    int id;
    Connection conn;

    public void initialize() {
        nameInput.textProperty().addListener((ov, oldValue, newValue) -> {
            int maxLength = 30;
            if (nameInput.getText().length() > maxLength) {
                String s = nameInput.getText().substring(0, maxLength);
                nameInput.setText(s);
            }
        });
        passInput.setId("password-field");
        passInput.textProperty().addListener((ov, oldValue, newValue) -> {
            int maxLength = 128;
            if (passInput.getText().length() > maxLength) {
                String s = passInput.getText().substring(0, maxLength);
                passInput.setText(s);
            }
        });

        HBox.setHgrow(hBox, Priority.ALWAYS);

        helpButton.setId("help-button");
        i.setId("i-text");
        infoCircle.setId("info-circle");

        Tooltip tooltip = new Tooltip("Must be at least 8 \n" +
                "characters in length. \n" +
                "Make sure to include at \n" +
                "least one lowercase, one \n" +
                "uppercase, one special \n" +
                "character and no whitespaces\n");

        tooltip.setShowDelay(Duration.seconds(0.2));
        Tooltip.install(infoStack,tooltip);
        viewAccounts.setId("view-accounts");

        loginButton.setOnAction(e -> {
            try {
                conn = Database.connect("jdbc:sqlite:quidology_users.db");
                sql = "SELECT id, password FROM users WHERE name = '"+nameInput.getText().toLowerCase()+"'";
                Statement stmt;
                Database.createNewTable(conn);
                stmt = conn.createStatement();
                stmt.setQueryTimeout(30);  // set timeout to 30 sec.
                ResultSet rs = stmt.executeQuery(sql);
                boolean flag = false;
                while (rs.next()) {
                    if (checkPass(passInput.getText(), rs.getString("password"))) {
                        id = rs.getInt("id");
                        flag = true;
                        System.out.println("Account exists");

                        dashWindow.setMinWidth(250);
                        dashWindow = new Stage();
                        dashWindow.initModality(Modality.NONE);
                        dashWindow.setTitle("Dashboard");

                        Parent root = FXMLLoader.load(Objects.requireNonNull(DashboardController.class.getClassLoader().getResource("dashboard_layout.fxml")));
                        Scene scene = new Scene(root, 1000, 600);
                        dashWindow.setScene(scene);
                        dashWindow.show();
                        ((Node)(e.getSource())).getScene().getWindow().hide();
                        conn.close();
                        break;
                    }
                }
                if (!flag) {
                    System.out.println("Username not registered"); //TODO Add pop-up error message
                }
                conn.close();
            } catch (SQLException | IOException throwables) {
                throwables.printStackTrace();
            }


        });
        signUpButton.setOnAction(e -> {
            try {
                conn = Database.connect("jdbc:sqlite:quidology_users.db");
                if (validateUserName(nameInput.getText()) && validatePassword(passInput.getText())) {
                    sql = "SELECT COUNT(id) FROM users WHERE name = '" + nameInput.getText().toLowerCase() + "'";
                    Statement stmt;
                    Database.createNewTable(conn);
                    stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    if (rs.getInt(1) == 0) {
                        Database.insertUser(nameInput.getText().toLowerCase(), savePass(passInput.getText()), conn);
                        System.out.println("Account created");
                        conn.close();
                    } else {
                        System.out.println("Account with that username already exists");
                    }
                }
                else if (!validateUserName(nameInput.getText()) && validatePassword(passInput.getText())){
                    System.out.println("Username invalid");
                }
                else if (validateUserName(nameInput.getText()) && !validatePassword(passInput.getText())){
                    System.out.println("Password invalid");
                }
                else{
                    System.out.println("Both Username and Password are invalid");
                }
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });

        helpButton.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(new URL("http://quidology.github.io").toURI());
            } catch (IOException | URISyntaxException ioException) {
                ioException.printStackTrace();
            }
        });

    }

    static boolean validateUserName(String userName) {
        Matcher mtch = userNamePattern.matcher(userName);
        return mtch.matches();
    }

    static boolean validatePassword (String password) {
        Matcher mtch = passwordPattern.matcher(password);
        return mtch.matches();
    }

    static String savePass(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    static boolean checkPass(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    // Add FXML
    @FXML
    private Button loginButton;

    @FXML
    private Button signUpButton;

    @FXML
    private Button viewAccounts;

    @FXML
    private Text i;

    @FXML
    private Circle infoCircle;

    @FXML
    private StackPane infoStack;

    @FXML
    private Button helpButton;

    @FXML
    private HBox hBox;

    @FXML
    private TextField passInput;

    @FXML
    private TextField nameInput;
}