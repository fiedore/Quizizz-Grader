package model;

import lombok.Data;

@Data
public class Question implements Comparable<Question>{

    private int id;
    private String text;

    public Question(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public Question(){
        this(-1, "");
    }


    @Override
    public int compareTo(Question o) {
        return this.id - o.id;
    }
}
