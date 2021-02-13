import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    // The fbla red is #be2c37
    public static void main(String[] args) { launch(args);}

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("login_layout.fxml"));
        Scene scene = new Scene(root, 1000, 600);

        primaryStage.setTitle("Quidology");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}