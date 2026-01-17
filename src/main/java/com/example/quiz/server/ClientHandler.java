package com.example.quiz.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String nick;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String message;
            while ((message = in.readLine()) != null) {
                processCommand(message);
                System.out.println("Otrzymano od " + (nick != null ? nick : "Nieznany") + ": " + message);
            }
        } catch (IOException e) {
            System.out.println("Gracz " + (nick != null ? nick : "nieznany") + " zerwał połączenie.");
        } finally {
            cleanUp();
        }
    }

    private void processCommand(String message) {
        if (message == null) return;
        String cleaned = message.trim();
        String[] parts = cleaned.split(":", 2);
        String command = parts[0];
        String data = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "JOIN":
                this.nick = data.trim();
                QuizServer.gameManager.addPlayer(this.nick, this);
                break;
            case "START":
                QuizServer.gameManager.startGame();
                break;
            case "ANSWER":
                if (this.nick != null) {
                    try {
                        int ansIdx = Integer.parseInt(data.trim());
                        QuizServer.gameManager.handleAnswer(this.nick, ansIdx);
                    } catch (NumberFormatException e) {
                        System.out.println("Błąd formatu odpowiedzi od " + nick);
                    }
                }
                break;
        }
    }

    public void sendMessage(String msg) {
        if (out != null) out.println(msg);
    }

    private void cleanUp() {
        try {
            if (nick != null) {
                QuizServer.gameManager.removePlayer(nick);
            }
            QuizServer.clients.remove(this);

            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}