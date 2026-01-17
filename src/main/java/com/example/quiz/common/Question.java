package com.example.quiz.common;

public class Question {
    public String content;
    public String[] options;
    public int correctAnswerIndex;

    public Question(String content, String[] options, int correctAnswerIndex) {
        this.content = content;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public String toNetworkString() {
        return "QUESTION:" + content + ";" + options[0] + ";" + options[1] + ";" + options[2] + ";" + options[3];
    }
}