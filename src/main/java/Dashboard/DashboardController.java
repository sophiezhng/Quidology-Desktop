package Dashboard;

import TakeQuiz.QuizController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

import static TakeQuiz.QuizController.quizWindow;

public class DashboardController {
    public static Stage dashWindow = new Stage();

    public void initialize() {
        startQuiz.setOnAction(e -> {
            quizWindow.setMinWidth(250);
            quizWindow = new Stage();
            quizWindow.initModality(Modality.NONE);
            quizWindow.setTitle("FBLA Quiz");
            Parent parent = null;
            try {
                parent = FXMLLoader.load(Objects.requireNonNull(QuizController.class.getClassLoader().getResource("quiz_layout.fxml")));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            Scene scene = new Scene(parent, 700, 500);
            quizWindow.setX(300);
            quizWindow.setY(100);
            quizWindow.setScene(scene);

            dashWindow.hide();
        });
    }

    @FXML
    private Button startQuiz;
}
