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
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class GUIController {

    public static final String POINTS_SUFFIX = "_points";
    public Button fileSelectButton;
    public TextField filepath;
    public ChoiceBox<String> studentSelector;
    public ListView<Node> questionListView;
    public ListView<Node> answerListView;
    public Button setRightAnswersButton;
    public ListView<Node> pointsListView;

    private Stage stage;

    private TestController testController;
    private final List<Node> rightAnswersChoiceBoxes = new ArrayList<>();
    private String[] rightAnswers;
    private String[] points;
    private String filename;
    private Preferences preferences;

    public void selectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik excela z ocenami");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arkusz Excel - wyniki z Quizizz", "*.xlsx"));
        preferences = Preferences.userRoot().node(this.getClass().getName());
        fileChooser.setInitialDirectory(new File(preferences.get("last directory", System.getProperty("user.dir"))));
        File file = fileChooser.showOpenDialog(stage);
        System.out.println(file);
        if (file != null) {
            try {
                processFile(file);
            } catch (IOException | BackingStoreException e) {
                e.printStackTrace();
            }
        }
    }

    private void processFile(File file) throws IOException, BackingStoreException {
        filepath.setText(file.getAbsolutePath());
        preferences.put("last directory", String.valueOf(file.getParentFile()));
        filename = file.getName();
        DataAdapter dataAdapter = new DataAdapter(SheetReader.getData(file.getPath()));
        List<Student> students = dataAdapter.extractStudents();
        List<Question> questions = dataAdapter.extractQuestions();

        testController = new TestController(questions, students);
        testController.mapQuestionsToAnswers(dataAdapter.extractAnswers());

        rightAnswersChoiceBoxes.clear();
        rightAnswers = new String[questions.size()];

        if (Arrays.asList(preferences.keys()).contains(filename)) {
            String rightAnswersArrayString = preferences.get(filename, null);
            if (rightAnswersArrayString != null) {
                rightAnswers = rightAnswersArrayString.split("\\|");
            }
        }

        fillSelector(students);
        fillQuestionListView(questions);
        fillPointsListView(questions.size());
        fillAnswerListView(students.get(0));

        bindScrollbars(questionListView, answerListView);
        bindScrollbars(questionListView, pointsListView);

        bindSelections(questionListView, answerListView);
        bindSelections(questionListView, pointsListView);

    }

    private void fillPointsListView(int size) throws BackingStoreException {
        ObservableList<Node> items = pointsListView.getItems();
        boolean hasPreference;

        hasPreference = Arrays.stream(preferences.keys()).anyMatch(s -> s.equals(filename + POINTS_SUFFIX));

        if (hasPreference) {
            String pointsArrayString = preferences.get(filename + POINTS_SUFFIX, "");
            points = pointsArrayString.substring(1, pointsArrayString.length() - 1).split(", ");
        } else {
            points = new String[size];
        }

        for (int i = 0; i < size; i++) {
            ChoiceBox<String> choiceBox = new ChoiceBox<>();

            for (int j = 1; j <= 5; j++) {
                choiceBox.getItems().add(j + "");
            }

            if (hasPreference) {
                choiceBox.setValue(points[i]);
            } else {
                choiceBox.setValue("1");
                points[i] = choiceBox.getValue();
            }

            choiceBox.getProperties().put("i", i);
            choiceBox.setOnAction(this::setPoints);
            choiceBox.setOnShown( event -> {
                @SuppressWarnings("unchecked")
                ChoiceBox<String> box = (ChoiceBox<String>) event.getSource();
                int boxIndex = (Integer) box.getProperties().get("i");
                questionListView.getSelectionModel().select(boxIndex);
                answerListView.getSelectionModel().select(boxIndex);
            });
            items.add(choiceBox);
        }

        if (!hasPreference) {
            preferences.put(filename + POINTS_SUFFIX, Arrays.toString(points));
        }
    }

    @SuppressWarnings("unchecked")
    private void setPoints(ActionEvent actionEvent) {
        ChoiceBox<String> choiceBox = (ChoiceBox<String>) actionEvent.getSource();
        int i = (int) choiceBox.getProperties().get("i");
        points[i] = choiceBox.getValue();
        System.out.printf("Putting this into preferences: %s - %s%n", filename + POINTS_SUFFIX, Arrays.toString(points));
        preferences.put(filename + POINTS_SUFFIX, Arrays.toString(points));
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

    private void fillAnswerListView(Student student) {
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

    private void fillQuestionListView(List<Question> questions) {
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
                .findFirst().ifPresent(this::fillAnswerListView);
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
                    rightAnswersChoiceBoxes.add(answersChoiceBox);

                    if (rightAnswers[i] != null && !rightAnswers[i].equals("null")) {
                        answersChoiceBox.setValue(rightAnswers[i]);
                    }

                    answersChoiceBox.setOnAction(this::setRightAnswer);
                }

            }

            for (int i = 0; i < testController.getQuestionList().size(); i++) {
                answerListView.getItems().add(rightAnswersChoiceBoxes.get(i));
            }

        } else {
            setRightAnswersButton.setText("Ustaw poprawne odp");
            studentSelector.setDisable(false);
            selectStudent();
        }
    }


    private ChoiceBox<String> fillChoiceBoxWithPossibleAnswers(int i) {
        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        SortedSet<String> possibleAnswers = new TreeSet<>(testController.getAnswersToQuestion(i));
        choiceBox.getItems().addAll(possibleAnswers);
        choiceBox.getItems().add("BRAK");
        return choiceBox;
    }

    @SuppressWarnings("unchecked")
    private void setRightAnswer(ActionEvent actionEvent) {
        ChoiceBox<String> choiceBox = (ChoiceBox<String>) actionEvent.getSource();
        int i = (int) choiceBox.getProperties().get("i");
        rightAnswers[i] = choiceBox.getValue();
        String jointAnswers = String.join("|", rightAnswers);
        System.out.printf("Putting this into preferences: %s - %s%n", filename, jointAnswers);
        preferences.put(filename, jointAnswers);
    }

}
