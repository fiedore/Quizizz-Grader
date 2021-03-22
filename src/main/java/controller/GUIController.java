package controller;

import app.DataAdapter;
import app.SheetReader;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Question;
import model.Student;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GUIController {

    public Button fileSelectButton;
    public TextField filepath;
    public ChoiceBox<String> studentSelector;
    public ListView<Node> questionListView;
    public ListView<Node> answerListView;
    public Button setRightAnswersButton;

    private Stage stage;

    private TestController testController;
    private final List<Node> rightAnswersChoiceBoxes = new ArrayList<>();
    private String[] rightAnswers;

    public void selectFile() {
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
        bindScrollbars(questionListView, answerListView);
        bindSelections(questionListView, answerListView);

    }

    private void bindSelections(ListView<Node> listView1, ListView<Node> listView2) {
        listView1.selectionModelProperty().bindBidirectional(listView2.selectionModelProperty());
    }

    private void bindScrollbars(ListView<Node> listView1, ListView<Node> listView2) {
        Node n1 = listView1.lookup(".scroll-bar");
        if (n1 instanceof ScrollBar) {
            final ScrollBar bar1 = (ScrollBar) n1;
            Node n2 = listView2.lookup(".scroll-bar");
            if (n2 instanceof ScrollBar) {
                final ScrollBar bar2 = (ScrollBar) n2;
                bar1.valueProperty().bindBidirectional(bar2.valueProperty());
            }
        }
    }

    private void fillAnswerTableView(Student student) {
        ObservableList<Node> items = answerListView.getItems();
        items.clear();
        testController.getQuestionToAnswers()
                .forEach((key, value) -> {
                    String answerString = value.get(student.getId());
                    Label answer = new Label(answerString);
                    Tooltip tooltip = new Tooltip(answerString);
                    tooltip.setShowDelay(Duration.millis(100));
                    answer.setTooltip(tooltip);
                    items.add(answer);
                });
    }

    private void fillQuestionTableView(List<Question> questions) {
        questionListView.getItems().clear();

        questionListView.setStyle("-fx-fit-to-width: true;");
        for (Question question : questions) {
            String questionString = (question.getId() + 1) + ". " + question.getText();
            Label questionText = new Label(questionString);
            Tooltip tooltip = new Tooltip(questionString);
            tooltip.setShowDelay(Duration.millis(100));
            questionText.setTooltip(tooltip);
            questionListView.getItems().add(questionText);
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

    public void setRightAnswersMode() {
        if (setRightAnswersButton.getText().equals("Ustaw poprawne odp")) {
            setRightAnswersButton.setText("Wróć");
            studentSelector.setDisable(true);
            answerListView.getItems().clear();

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
                answerListView.getItems().add(rightAnswersChoiceBoxes.get(i));
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
