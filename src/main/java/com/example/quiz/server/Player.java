package com.example.quiz.server;

import com.example.quiz.server.ClientHandler;

public class Player {
    private String nick;
    private int score = 0;
    private ClientHandler handler;

    public Player(String nick, ClientHandler handler) {
        this.nick = nick;
        this.handler = handler;
    }

    public String getNick() { return nick; }
    public int getScore() { return score; }
    public void addPoint() { this.score++; }
    public ClientHandler getHandler() { return handler; }
}