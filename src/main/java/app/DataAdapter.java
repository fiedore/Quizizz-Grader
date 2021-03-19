package app;

import model.Question;
import model.Student;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DataAdapter {

    public static final int NAMES_ROW_INDEX = 3;
    public static final int NAMES_AND_ANSWERS_COLUMN_INDEX = 4;
    public static final int QUESTIONS_COLUMN_INDEX = 0;
    public static final int QUESTIONS_ROW_INDEX = 4;

    public Map<Integer, List<String>> data;

    public DataAdapter(Map<Integer, List<String>> data) {
        this.data = data;
    }

    public List<Student> extractStudents() {
        List<Student> studentList = new ArrayList<>();
        List<String> namesRow = data.get(NAMES_ROW_INDEX);

        for (int i = NAMES_AND_ANSWERS_COLUMN_INDEX; i < namesRow.size(); i++) {
            String cellValue = namesRow.get(i);
            String name = cellValue.split("\\(")[0].trim();
            studentList.add(new Student(studentList.size(), name));
        }

        return studentList;
    }

    public List<Question> extractQuestions() {
        List<Question> questionList = new ArrayList<>();
        for (int i = QUESTIONS_ROW_INDEX; i < data.keySet().size(); i++) {
            List<String> row = data.get(i);

            if (row.get(0).equals("Total")){
                break;
            }

            questionList.add(new Question(questionList.size(), row.get(0)));
        }
        return questionList;
    }

    public Map<Integer, List<String>> extractAnswers(){
        Map<Integer, List<String>> answersMap = new TreeMap<>();
        for (int i = QUESTIONS_ROW_INDEX, count=0; i < data.keySet().size(); i++, count++){
            List<String> row = data.get(i);

            if (row.get(0).equals("Total")){
                break;
            }

            List<String> answers = IntStream
                    .range(NAMES_AND_ANSWERS_COLUMN_INDEX, row.size())
                    .mapToObj(row::get)
                    .collect(Collectors.toList());

            answersMap.put(count, answers);
        }

        return answersMap;
    }
}
