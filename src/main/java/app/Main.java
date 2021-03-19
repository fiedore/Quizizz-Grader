package app;

import gui.GUILauncher;
import model.Question;
import model.Student;
import controller.TestController;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
//        DataAdapter dataAdapter = new DataAdapter(SheetReader.getData("siur.xlsx"));
//        List<Student> students = dataAdapter.extractStudents();
//        List<Question> questions = dataAdapter.extractQuestions();
//
//        TestController testController = new TestController(questions, students);
//        testController.mapQuestionsToAnswers(dataAdapter.extractAnswers());
//
//        System.out.println(testController.getAnswersOfStudent(0));

        new GUILauncher().run(args);

    }
}
