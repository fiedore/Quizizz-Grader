package controller;

import lombok.Data;
import model.Question;
import model.Student;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Data
public class TestController {
    private List<Question> questionList;
    private List<Student> studentList;
    private TreeMap<Question, List<String>> questionToAnswers;

    public TestController(List<Question> questionList, List<Student> studentList) {
        this.questionList = questionList;
        this.studentList = studentList;
        this.questionToAnswers = new TreeMap<>();
    }

    public void mapQuestionsToAnswers(Map<Integer, List<String>> idToAnswers) {

        for (Integer i : idToAnswers.keySet()) {
            Question question = questionList.stream().filter(ques -> ques.getId() == i).findFirst().orElse(new Question());
            questionToAnswers.put(question, idToAnswers.get(i));
        }

    }

    public List<String> getAnswersToQuestion(int questionID) {
        Question questionObject = questionList.stream().filter(question -> question.getId() == questionID).findFirst().orElse(new Question());
        return questionToAnswers.get(questionObject);
    }

    public Map<Question, String> getAnswersOfStudent(int studentID) {
        Map<Question, String> questionToAnswer = new TreeMap<>();
        for (Question question : questionList) {
            questionToAnswer.put(question, questionToAnswers.get(question).get(studentID));
        }

        return questionToAnswer;
    }
}
