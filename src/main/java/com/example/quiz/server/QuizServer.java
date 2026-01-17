package com.example.quiz.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class QuizServer {
    private static final int PORT = 12345;
    protected static List<ClientHandler> clients = new ArrayList<>();
    public static GameManager gameManager = new GameManager();

    public static void main(String[] args) {
        System.out.println("Serwer QUIZ O KONIACH I KOWBOJACH uruchomiony...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nowy gracz połączył się: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Błąd serwera: " + e.getMessage());
        }
    }

    public static void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}
