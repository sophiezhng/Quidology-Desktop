package TakeQuiz;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.print.PageRange;
import javafx.print.PrinterJob;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

import static TakeQuiz.QuizController.quizWindow;

public class ReportController {
    public static Stage reportWindow = new Stage();

    @FXML
    private PieChart pieChart;

    VBox bigVbox = new VBox(20);

    public void initialize() {

        double[] marks = QuizController.getMarks();
        double total = 0;
        for (double elem:marks) {
            total += elem;
        }
        ObservableList<PieChart.Data> pieChartData =
            FXCollections.observableArrayList(
                    new PieChart.Data("Right", total),
                    new PieChart.Data("Wrong", 5-total));

        pieChart.setTitle("Right-to-Wrong Ratio");
        pieChart.setData(pieChartData);

        markOutOfFive.setFont(new Font(16));
        markOutOfFive.setText("Final Mark: "+total+"/5");

        VBox[] qBoxes = QuizController.getVBoxs();

        for (int i = 0; i < qBoxes.length; i++) {
            Label label;
            if (marks[i] > 0) {
                label = new Label("✓");
            }
            else {
                label = new Label("✗\nCorrect Answer(s): "+QuizController.getFiveAnswers()[i]);
            }
            label.setWrapText(true);
            label.setMaxWidth(400);
            qBoxes[i].getChildren().add(label);
            bigVbox.getChildren().add(qBoxes[i]);
        }


        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(bigVbox);
        borderPane.setCenter(scrollPane);
        borderPane.setMargin(scrollPane, new Insets(12,12,12,12));

        Button newButton = new Button("New Quiz");
        newButton.setOnAction(e -> {
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

            reportWindow.hide();
        });
        Button printBtn = new Button("Print Report");
        printBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("To Printer!");
                PrinterJob job = PrinterJob.createPrinterJob();
                if(job != null){
                    job.showPrintDialog(reportWindow);
                    VBox printResults = new VBox();
                    printResults.getChildren().addAll(markOutOfFive, pieChart);
                    printResults.setStyle("-fx-background-color: #00529b");
                    bigVbox.setStyle("-fx-background-color: #00529b");
                    job.printPage(printResults);
                    job.getJobSettings().setPageRanges(new PageRange(2, 4));
                    job.printPage(bigVbox);
                    job.endJob();
                }
            }
        });

        borderPane.setRight(printBtn);

        borderPane.setBottom(newButton);
        borderPane.setMargin(newButton, new Insets(12,12,24,24));

        reportWindow.show();
    }
    @FXML
    private BorderPane borderPane = new BorderPane();

    @FXML
    private Label markOutOfFive;
}
