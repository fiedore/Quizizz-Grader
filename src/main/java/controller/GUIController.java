package controller;

import app.DataAdapter;
import app.SheetReader;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Question;
import model.Student;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GUIController {

    public Button fileSelect;
    public TextField filepath;
    public ChoiceBox<String> studentSelector;
    public ListView<Node> questionTableView;
    public ListView<String> answerTableView;

    private Stage stage;

    private TestController testController;

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
        DataAdapter dataAdapter = new DataAdapter(SheetReader.getData("siur.xlsx"));
        List<Student> students = dataAdapter.extractStudents();
        List<Question> questions = dataAdapter.extractQuestions();

        testController = new TestController(questions, students);
        testController.mapQuestionsToAnswers(dataAdapter.extractAnswers());

        fillSelector(students);
        fillQuestionTableView(questions);
        fillAnswerTableView(testController.getQuestionToAnswers(), students.get(0));

    }

    private void fillAnswerTableView(TreeMap<Question, List<String>> questionToAnswers, Student student) {
        answerTableView.getItems().clear();

    }

    private void fillQuestionTableView(List<Question> questions) {
        questionTableView.getItems().clear();
        questionTableView.setStyle("-fx-fit-to-width: true;");
        for (Question question : questions) {
            Text questionText = new Text(question.getId() + ". " + question.getText());
            questionText.wrappingWidthProperty().bind(questionTableView.widthProperty().subtract(5));
            questionTableView.getItems().add(questionText);
        }
    }

    private void fillSelector(List<Student> students) {
        studentSelector.getItems().clear();
        for (Student student : students) {
            Text studentText = new Text(student.getId() + ". " + student.getName());
            studentSelector.getItems().add(studentText.getText());
        }
        studentSelector.setValue(studentSelector.getItems().get(0));
    }




    public void setMainStage(Stage stage) {
        this.stage = stage;
    }
}
