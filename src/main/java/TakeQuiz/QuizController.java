package TakeQuiz;

import Database.Database;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static TakeQuiz.ReportController.reportWindow;

public class QuizController {

    public static Stage quizWindow = new Stage();

    Connection conn;
    String sql;

    int intQType;

    private static int[] questionTypes = new int[5];
    private static double[] marks = new double[5];
    private static String[] fiveAnswers = new String[5];

    private static VBox[] fiveQBox = new VBox[5];
    Button nextButton, backButton, finishButton;
    HBox buttons = new HBox();

    int finished = 0;

    int[][] buttonColor = new int[4][2];
    int[][] scrambId = new int[4][2];

    String rightAnswer = null;

    TextField textField;
    String shrtRightAnswer;

    public static double[] getMarks() {
        return marks;
    }

    public static String[] getFiveAnswers() {
        return fiveAnswers;
    }

    public static VBox[] getVBoxs() {
        return fiveQBox;
    }

    public void initialize() throws SQLException {

        fiveQBox = new VBox[5];
        marks = new double[5];

        buttonColor = new int[4][2];
        scrambId = new int[4][2];

        conn = Database.connect("jdbc:sqlite:questions.db");
        System.out.println("No error... yet");

        nextButton = new Button("Next");

        startNewQuestion();
        borderPane.setCenter(fiveQBox[finished]);
        quizWindow.show();

        nextButton.setOnAction(e -> {
            checkAnswer();
            finished++;
            startNewQuestion();
            borderPane.setCenter(fiveQBox[finished]);
            quizWindow.show();
        });
        backButton = new Button("Back");
        backButton.setOnAction(e -> {
            checkAnswer();
            finished--;
            startNewQuestion();
            borderPane.setCenter(fiveQBox[finished]);
            quizWindow.show();
        });
        finishButton = new Button("Finish Quiz");
        finishButton.setOnAction(e -> {
            // TODO: Generate report
            checkAnswer();
            double total = 0;
            for (double elem: marks) {
                total+=elem;
            }
            System.out.println(total);
            reportWindow.setMinWidth(250);
            reportWindow = new Stage();
            reportWindow.initModality(Modality.NONE);
            reportWindow.setTitle("FBLA Quiz");
            Parent parent = null;
            try {
                parent = FXMLLoader.load(Objects.requireNonNull(ReportController.class.getClassLoader().getResource("report_layout.fxml")));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            Scene scene = new Scene(parent, 900, 600);
            reportWindow.setX(300);
            reportWindow.setY(100);
            reportWindow.setScene(scene);

            quizWindow.hide();
        });
    }

    private void startNewQuestion() {
        if (finished < 5) {
            buttons = new HBox();
            buttons.setPrefWidth(200);
            nextButton.setText("Next");
            if (finished == 0) {
                buttons.getChildren().add(nextButton);
            }
            else if (finished == 4) {
                buttons.getChildren().addAll(backButton, finishButton);
            }
            else {
                buttons.getChildren().addAll(backButton, nextButton);
            }
            if (fiveQBox[finished] == null) {
                VBox quizQ = new VBox();
                quizQ.setAlignment(Pos.CENTER);

                intQType = (int) (Math.random() * 4);
                questionTypes[finished] = intQType;

                try {
                    switch (intQType) {
                        case 0:
                            quizQ.getChildren().add(addMatchingQ(conn, finished + 1));
                            break;
                        case 1:
                            quizQ.getChildren().add(addMCQ(conn, finished + 1));
                            break;
                        case 2:
                            quizQ.getChildren().add(addShortQ(conn, finished + 1));
                            break;
                        case 3:
                            quizQ.getChildren().add(addTorFQ(conn, finished + 1));
                            break;
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                buttons.setAlignment(Pos.CENTER);
                quizQ.getChildren().add(buttons);
                fiveQBox[finished] = quizQ;
                return;
            }
            buttons.setAlignment(Pos.CENTER);
            fiveQBox[finished].getChildren().add(buttons);
        }
    }

    private VBox addMatchingQ(Connection c, int qNum) throws SQLException {
        VBox theQ = new VBox();

        Label label = new Label(Integer.toString(qNum)+". ");
        label.wrapTextProperty().setValue(true);
        label.setMaxWidth(400);
        int choseQuestion;
        int answerInt;
        String question;
        String answer = null;
        List<String> questions = new ArrayList<>();
        List<String> answers = new ArrayList<>();
        String[] nQuestions = new String[4];
        String[] nAnswers = new String[4];
        List<Integer> indexArray = Arrays.asList(0, 1, 2, 3);

        sql = "SELECT id FROM accounting_i WHERE short_answer = 1";
        Statement stmt;
        stmt = c.createStatement();
        stmt.setQueryTimeout(30);  // set timeout to 30 sec.
        ResultSet rs = stmt.executeQuery(sql);
        List<Integer> ids = new ArrayList<>();

        while (rs.next()) {
            ids.add(rs.getInt("id"));
        }
        Random rand = new Random();
        for (int i = 0; i < 4; i++) {
            choseQuestion = ids.get(rand.nextInt(ids.size()));
            sql = "SELECT question, answer, option_a, option_b, option_c, option_d FROM accounting_i WHERE id = '"+choseQuestion+"'";
            rs = stmt.executeQuery(sql);
            question = rs.getString("question");
            answerInt = rs.getInt("answer");
            switch(answerInt) {
                case 1:
                    answer = rs.getString("option_a");
                    break;
                case 2:
                    answer = rs.getString("option_b");
                    break;
                case 3:
                    answer = rs.getString("option_c");
                    break;
                case 4:
                    answer = rs.getString("option_d");
                    break;
            }
            questions.add(question); answers.add(answer);
        }
        for (int i = 0; i < questions.size(); i++) {
            fiveAnswers[finished] += questions.get(i)+" A: "+answers.get(i)+"\n ";
        }

        Collections.shuffle(indexArray);

        for (int i = 0; i < indexArray.size(); i++) {
            scrambId[i][0] = ids.get(indexArray.get(i));
            nQuestions[i] = questions.get(indexArray.get(i));
        }

        Collections.shuffle(indexArray);

        for (int i = 0; i < indexArray.size(); i++) {
            scrambId[i][1] = ids.get(indexArray.get(i));
            nAnswers[i] = answers.get(indexArray.get(i));
        }

        buttonColor = new int[4][2];

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.getColumnConstraints().add(new ColumnConstraints(300));
        gridPane.getColumnConstraints().add(new ColumnConstraints(150));

        ToggleButton[] toggleQuestions = new ToggleButton[4];
        ToggleButton[] toggleAnswers = new ToggleButton[4];

        for (int i = 0; i < toggleQuestions.length; i++) {
            toggleQuestions[i] = new ToggleButton();
            toggleQuestions[i].wrapTextProperty().setValue(true);
            toggleQuestions[i].setText(nQuestions[i]);
            toggleQuestions[i].setUserData(nQuestions[i]);
            gridPane.add(toggleQuestions[i], 0, i, 1, 1);
            GridPane.setFillWidth(toggleQuestions[i], true);
            GridPane.setFillHeight(toggleQuestions[i], true);
            toggleQuestions[i].setMaxWidth(Double.MAX_VALUE);
            int finalI = i;
            toggleQuestions[i].setOnAction(event -> {
                switch(checkColor(buttonColor)) {
                    case 0:
                        toggleQuestions[finalI].setStyle("-fx-background-color: transparent");
                        break;
                    case 1:
                        toggleQuestions[finalI].setStyle("-fx-background-color: purple");
                        break;
                    case 2:
                        toggleQuestions[finalI].setStyle("-fx-background-color: blue");
                        break;
                    case 3:
                        toggleQuestions[finalI].setStyle("-fx-background-color: green");
                        break;
                    case 4:
                        toggleQuestions[finalI].setStyle("-fx-background-color: red");
                        break;
                }
                if (buttonColor[finalI][0] > 0) {
                    buttonColor[finalI][0] = 0;
                }
                buttonColor[finalI][0] = checkColor(buttonColor);
            });

        }
        for (int i = 0; i < toggleAnswers.length; i++) {
            toggleAnswers[i] = new ToggleButton();
            toggleAnswers[i].wrapTextProperty().setValue(true);
            toggleAnswers[i].setText(nAnswers[i]);
            toggleAnswers[i].setUserData(nAnswers[i]);
            gridPane.add(toggleAnswers[i], 1, i, 1, 1);
            GridPane.setFillWidth(toggleAnswers[i], true);
            GridPane.setFillHeight(toggleAnswers[i], true);
            toggleAnswers[i].setMaxWidth(Double.MAX_VALUE);
            toggleAnswers[i].setMaxHeight(Double.MAX_VALUE);
            int finalI = i;
            toggleAnswers[i].setOnAction(event -> {
                switch(checkColor(buttonColor)) {
                    case 0:
                        toggleAnswers[finalI].setStyle("-fx-background-color: transparent");
                        break;
                    case 1:
                        toggleAnswers[finalI].setStyle("-fx-background-color: purple");
                        break;
                    case 2:
                        toggleAnswers[finalI].setStyle("-fx-background-color: blue");
                        break;
                    case 3:
                        toggleAnswers[finalI].setStyle("-fx-background-color: green");
                        break;
                    case 4:
                        toggleAnswers[finalI].setStyle("-fx-background-color: red");
                        break;
                }
                if (buttonColor[finalI][1] > 0) {
                    buttonColor[finalI][1] = 0;
                }
                buttonColor[finalI][1] = checkColor(buttonColor);
            });
        }
        Button clearButton = new Button("Clear Matches");
        clearButton.setOnAction(e -> {
            for (int i = 0; i < toggleQuestions.length; i++) {
                toggleQuestions[i].setStyle("-fx-background-color: transparent");
                buttonColor[i][0] = 0;
            }
            for (int i = 0; i < toggleAnswers.length; i++) {
                toggleAnswers[i].setStyle("-fx-background-color: transparent");
                buttonColor[i][1] = 0;
            }
        });
        theQ.getChildren().addAll(label, clearButton, gridPane);

        theQ.setAlignment(Pos.CENTER);
        return theQ;
    }

    String userAnswer;
    private VBox addMCQ(Connection c, int qNum) throws SQLException{
        VBox theQ = new VBox();
        theQ.setAlignment(Pos.CENTER);

        int choseQuestion;
        int answerInt;
        String question;


        RadioButton[] radioButtons = new RadioButton[4];
        final ToggleGroup options = new ToggleGroup();

        sql = "SELECT id FROM accounting_i";
        Statement stmt;
        stmt = c.createStatement();
        stmt.setQueryTimeout(30);  // set timeout to 30 sec.
        ResultSet rs = stmt.executeQuery(sql);
        List<Integer> ids = new ArrayList<>();

        while (rs.next()) {
            ids.add(rs.getInt("id"));
        }
        Random rand = new Random();
        choseQuestion = ids.get(rand.nextInt(ids.size()));
        sql = "SELECT question, answer, option_a, option_b, option_c, option_d FROM accounting_i WHERE id = '"+choseQuestion+"'";
        rs = stmt.executeQuery(sql);
        question = rs.getString("question");
        answerInt = rs.getInt("answer");
        switch(answerInt) {
            case 1:
                rightAnswer = rs.getString("option_a");
                break;
            case 2:
                rightAnswer = rs.getString("option_b");
                break;
            case 3:
                rightAnswer = rs.getString("option_c");
                break;
            case 4:
                rightAnswer = rs.getString("option_d");
                break;
        }
        fiveAnswers[finished] = rightAnswer;
        int count = 0;
        for (String elem: new String[]{"option_a", "option_b", "option_c", "option_d"}) {
            radioButtons[count] = new RadioButton(rs.getString(elem));
            radioButtons[count].setUserData(rs.getString(elem));
            radioButtons[count].setToggleGroup(options);
            radioButtons[count].setMaxWidth(400);
            radioButtons[count].setWrapText(true);

            count++;
        }
        options.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            public void changed(ObservableValue<? extends Toggle> ov,
                                Toggle old_toggle, Toggle new_toggle) {
                if (options.getSelectedToggle() != null) {
                    userAnswer = options.getSelectedToggle().getUserData().toString();
                    if (userAnswer.equals(rightAnswer)) {
                        marks[finished] = 1;
                    }
                    else {
                        marks[finished] = 0;
                    }
                }
            }
        });

        Label label = new Label(Integer.toString(qNum)+". "+question);
        label.wrapTextProperty().setValue(true);
        label.setMaxWidth(400);

        theQ.getChildren().add(label);
        for (int i = 0; i < radioButtons.length; i++) {
            theQ.getChildren().add(radioButtons[i]);
        }

        theQ.setAlignment(Pos.CENTER);
        return theQ;
    }

    private VBox addShortQ(Connection c, int qNum) throws SQLException{
        VBox theQ = new VBox();
        theQ.setAlignment(Pos.CENTER);

        int choseQuestion;
        int answerInt;
        String question;
        String rightAnswer = null;

        textField = new TextField();

        sql = "SELECT id FROM accounting_i WHERE short_answer = 1";
        Statement stmt;
        stmt = c.createStatement();
        stmt.setQueryTimeout(30);  // set timeout to 30 sec.
        ResultSet rs = stmt.executeQuery(sql);
        List<Integer> ids = new ArrayList<>();

        while (rs.next()) {
            ids.add(rs.getInt("id"));
        }
        Random rand = new Random();
        choseQuestion = ids.get(rand.nextInt(ids.size()));
        sql = "SELECT question, answer, option_a, option_b, option_c, option_d FROM accounting_i WHERE id = '"+choseQuestion+"'";
        rs = stmt.executeQuery(sql);
        question = rs.getString("question");
        answerInt = rs.getInt("answer");
        switch(answerInt) {
            case 1:
                shrtRightAnswer = rs.getString("option_a");
                break;
            case 2:
                shrtRightAnswer = rs.getString("option_b");
                break;
            case 3:
                shrtRightAnswer = rs.getString("option_c");
                break;
            case 4:
                shrtRightAnswer = rs.getString("option_d");
                break;
        }
        fiveAnswers[finished] = shrtRightAnswer;

        int count = 0;

        Label label = new Label(Integer.toString(qNum)+". "+question);
        label.setWrapText(true);
        label.setMaxWidth(400);
        textField.setMaxWidth(400);
        theQ.getChildren().addAll(label, textField);

        theQ.setAlignment(Pos.CENTER);
        return theQ;

    }

    private VBox addTorFQ(Connection c, int qNum) throws SQLException{ // TODO: edit to make appropriate for true and false qs
        VBox theQ = new VBox();

        int choseQuestion;
        int answerInt;
        String question;
        String statement = null;
        boolean rightTorFAnswer;

        RadioButton[] radioButtons = new RadioButton[2];
        final ToggleGroup options = new ToggleGroup();

        sql = "SELECT id FROM accounting_i";
        Statement stmt;
        stmt = c.createStatement();
        stmt.setQueryTimeout(30);  // set timeout to 30 sec.
        ResultSet rs = stmt.executeQuery(sql);
        List<Integer> ids = new ArrayList<>();

        while (rs.next()) {
            ids.add(rs.getInt("id"));
        }
        Random rand = new Random();
        choseQuestion = ids.get(rand.nextInt(ids.size()));
        sql = "SELECT question, answer, option_a, option_b, option_c, option_d FROM accounting_i WHERE id = '"+choseQuestion+"'";
        rs = stmt.executeQuery(sql);

        question = rs.getString("question");
        int trueOrFalse = (int)(Math.random());
        int randomAnswer;
        if (trueOrFalse==0) {
            randomAnswer = (int)(Math.random()*4);
            switch(randomAnswer) {
                case 1:
                    statement = rs.getString("option_a");
                    break;
                case 2:
                    statement = rs.getString("option_b");
                    break;
                case 3:
                    statement = rs.getString("option_c");
                    break;
                case 4:
                    statement = rs.getString("option_d");
                    break;
            }
            rightTorFAnswer = false;
        }
        else {
            answerInt = rs.getInt("answer");
            switch(answerInt) {
                case 1:
                    statement = rs.getString("option_a");
                    break;
                case 2:
                    statement = rs.getString("option_b");
                    break;
                case 3:
                    statement = rs.getString("option_c");
                    break;
                case 4:
                    statement = rs.getString("option_d");
                    break;
            }
            rightTorFAnswer = true;
        }
        if (rightTorFAnswer) {
            fiveAnswers[finished] = "True";
        }
        else {
            fiveAnswers[finished] = "False";
        }
        radioButtons[0] = new RadioButton("True");
        radioButtons[0].setUserData("True");
        radioButtons[0].setToggleGroup(options);
        radioButtons[1] = new RadioButton("False");
        radioButtons[1].setUserData("False");
        radioButtons[1].setToggleGroup(options);

        options.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            public void changed(ObservableValue<? extends Toggle> ov,
                                Toggle old_toggle, Toggle new_toggle) {
                if (options.getSelectedToggle() != null) {
                    userAnswer = options.getSelectedToggle().getUserData().toString();
                    if ((userAnswer.equals("True") && rightTorFAnswer) || (userAnswer.equals("False") && !rightTorFAnswer)) {
                        marks[finished] = 1;
                    }
                    else {
                        marks[finished] = 0;
                    }
                }
            }
        });

        Label label = new Label(Integer.toString(qNum)+". "+question+" "+statement);
        label.wrapTextProperty().setValue(true);
        label.setMaxWidth(400);
        theQ.getChildren().add(label);
        for (int i = 0; i < radioButtons.length; i++) {
            theQ.getChildren().add(radioButtons[i]);
        }

        theQ.setAlignment(Pos.CENTER);
        return theQ;
    }

    @FXML
    private BorderPane borderPane = new BorderPane();

    private int checkColor(int buttonColor[][]) {
        int nextColor = 0;
        int left = 0;
        int right = 0;
        for (int i = 0; i < 4; i++) {
            if (buttonColor[i][0] > 0) {
                left++;
            }
            if (buttonColor[i][1] > 0) {
                right++;
            }
        }
        if (Math.abs(left - right) > 1) {
            return 0;
        }
        if (left == right){
            return left + 1;
        }
        if (left > right) {
            return left;
        }
        return right;
    }

    private void checkAnswer() {
        if (questionTypes[finished] == 0) {
            marks[finished] = 0;
            for (int i = 0; i < scrambId.length; i++) {
                for (int j = 0; j < scrambId.length; j++) {
                    if (scrambId[i][0] == scrambId[j][1] && buttonColor[i][0] == buttonColor[j][1] && buttonColor[i][0] != 0 && buttonColor[j][1] != 0) {
                        marks[finished] += 0.25;
                    }
                }
            }
        }
        else if (questionTypes[finished] == 2) {
            if (textField.getText().equalsIgnoreCase(shrtRightAnswer)) {
                marks[finished] = 1;
            }
        }
    }
}
