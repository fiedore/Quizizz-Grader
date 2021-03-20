package controller;

import app.DataAdapter;
import app.SheetReader;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Question;
import model.Student;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GUIController {

    public Button fileSelectButton;
    public TextField filepath;
    public ChoiceBox<String> studentSelector;
    public ListView<Node> questionTableView;
    public ListView<Node> answerTableView;
    public Button setRightAnswersButton;

    private Stage stage;

    private TestController testController;
    private final List<Node> rightAnswersChoiceBoxes = new ArrayList<>();
    private String[] rightAnswers;

    public void selectFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik excela z ocenami");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Arkusz Excel", "*.xlsx"));
        File file = fileChooser.showOpenDialog(stage);
        System.out.println(file);
        if (file != null) {
            try {
                processFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processFile(File file) throws IOException {
        filepath.setText(file.getAbsolutePath());
        DataAdapter dataAdapter = new DataAdapter(SheetReader.getData(file.getPath()));
        List<Student> students = dataAdapter.extractStudents();
        List<Question> questions = dataAdapter.extractQuestions();

        testController = new TestController(questions, students);
        testController.mapQuestionsToAnswers(dataAdapter.extractAnswers());

        rightAnswersChoiceBoxes.clear();
        rightAnswers = new String[questions.size()];
        fillSelector(students);
        fillQuestionTableView(questions);
        fillAnswerTableView(students.get(0));

    }

    private void fillAnswerTableView(Student student) {
        answerTableView.getItems().clear();
        testController.getQuestionToAnswers()
                .forEach((key, value) -> answerTableView.getItems().add(new Text(value.get(student.getId()))));
    }

    private void fillQuestionTableView(List<Question> questions) {
        questionTableView.getItems().clear();
        questionTableView.setStyle("-fx-fit-to-width: true;");
        for (Question question : questions) {
            Text questionText = new Text((question.getId() + 1) + ". " + question.getText());
            questionTableView.getItems().add(questionText);
        }
    }

    private void fillSelector(List<Student> students) {
        studentSelector.getItems().clear();
        for (Student student : students) {
            Text studentText = new Text((student.getId() + 1) + ". " + student.getName());
            studentSelector.getItems().add(studentText.getText());
        }
        studentSelector.setValue(studentSelector.getItems().get(0));
    }


    public void setMainStage(Stage stage) {
        this.stage = stage;
    }

    public void selectStudent() {
        String selection = studentSelector.getSelectionModel().getSelectedItem();
        if (selection == null) {
            return;
        }
        String studentName = selection.split("\\. ")[1];
        testController.getStudentList().stream()
                .filter(student -> student.getName().equals(studentName))
                .findFirst().ifPresent(this::fillAnswerTableView);
    }

    public void setRightAnswersMode(ActionEvent actionEvent) {
        if (setRightAnswersButton.getText().equals("Ustaw poprawne odp")) {
            setRightAnswersButton.setText("Wróć");
            studentSelector.setDisable(true);
            answerTableView.getItems().clear();

            if (rightAnswersChoiceBoxes.isEmpty()) {
                for (int i = 0; i < testController.getQuestionList().size(); i++) {
                    ChoiceBox<String> answersChoiceBox = fillChoiceBoxWithPossibleAnswers(i);
                    answersChoiceBox.getProperties().put("i", i);
                    answersChoiceBox.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(0.1))));
                    answersChoiceBox.setOnAction(this::setRightAnswer);
                    rightAnswersChoiceBoxes.add(answersChoiceBox);
                }

            }

            for (int i = 0; i < testController.getQuestionList().size(); i++) {
                answerTableView.getItems().add(rightAnswersChoiceBoxes.get(i));
            }

        } else {
            setRightAnswersButton.setText("Ustaw poprawne odp");
            studentSelector.setDisable(false);
            selectStudent();
            System.out.println("rightAnswers=" + Arrays.toString(rightAnswers));
        }
    }


    private ChoiceBox<String> fillChoiceBoxWithPossibleAnswers(int i) {
        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        SortedSet<String> possibleAnswers = new TreeSet<>(testController.getAnswersToQuestion(i));
        choiceBox.getItems().addAll(possibleAnswers);
        return choiceBox;
    }

    @SuppressWarnings("unchecked")
    private void setRightAnswer(ActionEvent actionEvent) {
        ChoiceBox<String> choiceBox = (ChoiceBox<String>) actionEvent.getSource();
        int i = (int) choiceBox.getProperties().get("i");
        rightAnswers[i] = choiceBox.getValue();
    }

}
