package com.example.quiz.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class QuizClientApp extends Application {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private Stage primaryStage;
    private Label questionLabel = new Label("CISZA PRZED SAMYM POŁUDNIEM...");
    private Button[] answerButtons = new Button[4];
    private ListView<String> rankingList = new ListView<>();
    private Button startButton = new Button("DAWAĆ PYTANIA");

    private static final String BG_COLOR = "#FDF5E6";
    private static final String ACCENT_DARK = "#3E2723";
    private static final String ACCENT_LIGHT = "#D7CCC8";
    private static final String BUTTON_COLOR = "#5D4037";
    private static final String TEXT_ON_DARK = "#EFEBE9";
    private static final String GOLD_HIGHLIGHT = "#FFB300";

    private static final String BTN_STYLE =
            "-fx-background-color: " + BUTTON_COLOR + ";" +
                    "-fx-text-fill: " + TEXT_ON_DARK + ";" +
                    "-fx-font-family: 'Courier New';" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 15px;" +
                    "-fx-background-radius: 2;" +
                    "-fx-border-color: #8D6E63;" +
                    "-fx-border-width: 2px;" +
                    "-fx-border-style: segments(5, 5) phase 0;" +
                    "-fx-cursor: hand;";

    private static final String BTN_HOVER_STYLE =
            "-fx-background-color: #4E342E;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-family: 'Courier New';" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 15px;" +
                    "-fx-background-radius: 2;" +
                    "-fx-border-color: #A1887F;" +
                    "-fx-border-width: 2px;" +
                    "-fx-border-style: segments(5, 5) phase 0;" +
                    "-fx-cursor: hand;";

    private static final String BTN_SELECTED_STYLE =
            "-fx-background-color: #3E2723;" +
                    "-fx-text-fill: " + GOLD_HIGHLIGHT + ";" +
                    "-fx-border-color: " + GOLD_HIGHLIGHT + ";" +
                    "-fx-font-family: 'Courier New';" +
                    "-fx-font-size: 15px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-border-width: 2px;";

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showLoginScreen();
    }

    private void showLoginScreen() {
        VBox layout = new VBox(25);
        layout.setPadding(new Insets(50));
        layout.setAlignment(Pos.CENTER);

        layout.setStyle(
                "-fx-background-color: " + BG_COLOR + ";" +
                        "-fx-border-color: " + ACCENT_DARK + ";" +
                        "-fx-border-width: 5px;" +
                        "-fx-border-style: solid inside;" +
                        "-fx-border-insets: 10;"

        );

        Label wantedLabel = new Label("WANTED");
        wantedLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 48));
        wantedLabel.setStyle("-fx-text-fill: " + ACCENT_DARK + "; -fx-letter-spacing: 5px;");

        Label subLabel = new Label("POSZUKIWANY GRACZ");
        subLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 18));
        subLabel.setStyle("-fx-text-fill: " + ACCENT_DARK + ";");

        Separator sep = new Separator();
        sep.setMaxWidth(200);
        sep.setStyle("-fx-background-color: " + ACCENT_DARK + ";");

        TextField nickField = new TextField();
        nickField.setPromptText("WPISZ SWÓJ ALIAS...");
        nickField.setMaxWidth(300);
        nickField.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent transparent " + ACCENT_DARK + " transparent;" +
                        "-fx-border-width: 2px;" +
                        "-fx-text-fill: " + ACCENT_DARK + ";" +
                        "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 16px;" +
                        "-fx-alignment: CENTER;"
        );

        Button joinButton = new Button("DOSIĄDŹ KONIA");
        joinButton.setPrefWidth(200);
        joinButton.setPrefHeight(40);
        joinButton.setStyle(BTN_STYLE);
        applyHoverEffect(joinButton);

        joinButton.setOnAction(e -> {
            String nick = nickField.getText().trim();
            if (!nick.isEmpty()) {
                joinButton.setDisable(true);
                connectToServer(nick);
                showGameScreen();
            }
        });

        layout.getChildren().addAll(wantedLabel, subLabel, sep, nickField, joinButton);
        primaryStage.setScene(new Scene(layout, 500, 450));
        primaryStage.setTitle("Quiz");
        primaryStage.show();
    }

    private void showGameScreen() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: " + BG_COLOR + ";");

        VBox topBox = new VBox(10);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(20));
        topBox.setStyle("-fx-border-color: " + ACCENT_DARK + " transparent " + ACCENT_DARK + " transparent; -fx-border-width: 2px; -fx-border-style: solid;");

        Label telegramHeader = new Label("--- WIADOMOŚĆ Z BIURA SZERYFA ---");
        telegramHeader.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        telegramHeader.setStyle("-fx-text-fill: #8D6E63;");

        questionLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 20));
        questionLabel.setStyle("-fx-text-fill: " + ACCENT_DARK + ";");
        questionLabel.setWrapText(true);
        questionLabel.setAlignment(Pos.CENTER);

        topBox.getChildren().addAll(telegramHeader, questionLabel);
        mainLayout.setTop(topBox);

        VBox answerBox = new VBox(15);
        answerBox.setAlignment(Pos.CENTER);
        answerBox.setPadding(new Insets(30, 50, 30, 50));

        for (int i = 0; i < 4; i++) {
            answerButtons[i] = new Button("...");
            answerButtons[i].setPrefWidth(600);
            answerButtons[i].setPrefHeight(50);
            answerButtons[i].setStyle(BTN_STYLE);
            answerButtons[i].setWrapText(true);

            applyHoverEffect(answerButtons[i]);

            int finalI = i;
            answerButtons[i].setOnAction(e -> sendAnswer(finalI));
            answerBox.getChildren().add(answerButtons[i]);
        }
        mainLayout.setCenter(answerBox);

        VBox rightBox = new VBox(10);
        rightBox.setPadding(new Insets(15));
        rightBox.setPrefWidth(250);
        rightBox.setStyle("-fx-background-color: #FFFDF0; -fx-border-color: #D7CCC8; -fx-border-width: 1px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 2, 2);");

        Label rankingHeader = new Label("NAJBARDZIEJ POSZUKIWANI");
        rankingHeader.setFont(Font.font("Courier New", FontWeight.BOLD, 15));
        rankingHeader.setStyle("-fx-text-fill: " + ACCENT_DARK + "; -fx-underline: true;");
        rankingHeader.setAlignment(Pos.CENTER);
        rankingHeader.setMaxWidth(Double.MAX_VALUE);

        rankingList.setStyle("-fx-background-color: transparent; -fx-control-inner-background: #FFFDF0;");
        rankingList.setPrefHeight(300);

        startButton.setText("START");
        startButton.setPrefHeight(50);
        startButton.setMaxWidth(Double.MAX_VALUE);
        startButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #2E7D32;" +
                        "-fx-border-width: 3px;" +
                        "-fx-text-fill: #2E7D32;" +
                        "-fx-font-family: 'Courier New';" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 18px;" +
                        "-fx-cursor: hand;"
        );

        startButton.setOnMouseEntered(e -> startButton.setStyle("-fx-background-color: #E8F5E9; -fx-border-color: #2E7D32; -fx-border-width: 3px; -fx-text-fill: #2E7D32; -fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-font-size: 18px;"));
        startButton.setOnMouseExited(e -> startButton.setStyle("-fx-background-color: transparent; -fx-border-color: #2E7D32; -fx-border-width: 3px; -fx-text-fill: #2E7D32; -fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-font-size: 18px;"));

        startButton.setOnAction(e -> {
            if (out != null) out.println("START");
        });

        rightBox.getChildren().addAll(rankingHeader, rankingList, startButton);
        mainLayout.setRight(rightBox);

        BorderPane.setMargin(rightBox, new Insets(0, 0, 0, 20));

        primaryStage.setScene(new Scene(mainLayout, 950, 600));
    }

    private void applyHoverEffect(Button b) {
        b.setOnMouseEntered(e -> {
            if (!b.isDisabled() && !b.getStyle().contains(GOLD_HIGHLIGHT)) {
                b.setStyle(BTN_HOVER_STYLE);
            }
        });
        b.setOnMouseExited(e -> {
            if (!b.isDisabled() && !b.getStyle().contains(GOLD_HIGHLIGHT)) {
                b.setStyle(BTN_STYLE);
            }
        });
    }

    private void connectToServer(String nick) {
        try {
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Platform.runLater(() -> primaryStage.setTitle("Alias: " + nick));

            out.println("JOIN:" + nick);
            new Thread(this::listenToServer).start();
        } catch (IOException e) {
            showError("Błąd połączenia z biurem szeryfa!");
        }
    }

    private void listenToServer() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                String finalMsg = message;
                Platform.runLater(() -> handleServerMessage(finalMsg));
            }
        } catch (IOException e) {
            Platform.runLater(() -> questionLabel.setText("Połączenie zerwane."));
        }
    }

    private void handleServerMessage(String msg) {
        String[] parts = msg.split(":", 2);
        String command = parts[0];
        String data = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "QUESTION":
                if (answerButtons[0] == null) return;
                System.out.println("[KLIENT] Pytanie: " + data);

                String[] q = data.split(";");
                questionLabel.setText(q[0]);

                for (int i = 0; i < 4; i++) {
                    answerButtons[i].setText(q[i+1]);
                    answerButtons[i].setDisable(false);
                    answerButtons[i].setOpacity(1.0);
                    answerButtons[i].setStyle(BTN_STYLE);
                }
                break;

            case "SCORE":
                rankingList.getItems().clear();
                for (String p : data.split(";")) {
                    if (!p.isEmpty()) {
                        String[] scoreParts = p.split("=");
                        if (scoreParts.length == 2) {
                            rankingList.getItems().add(String.format("%-10s %s pkt", scoreParts[0], scoreParts[1]));
                        }
                    }
                }
                break;

            case "FINISH":
                System.out.println("[KLIENT] Koniec.");
                questionLabel.setText("KONIEC GRY");
                for (Button b : answerButtons) {
                    b.setDisable(true);
                    b.setStyle("-fx-background-color: transparent; -fx-text-fill: gray; -fx-border-color: gray;");
                }
                break;
        }
    }

    private void sendAnswer(int idx) {
        for (Button b : answerButtons) {
            b.setDisable(true);
            if (b != answerButtons[idx]) {
                b.setStyle("-fx-background-color: transparent; -fx-text-fill: #A1887F; -fx-border-color: #D7CCC8; -fx-border-style: dashed;");
            }
        }
        answerButtons[idx].setStyle(BTN_SELECTED_STYLE);
        answerButtons[idx].setOpacity(1.0);

        if (out != null) out.println("ANSWER:" + idx);
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.setHeaderText("Błąd");
        alert.showAndWait();
    }

    @Override
    public void stop() throws Exception {
        if (socket != null && !socket.isClosed()) socket.close();
        super.stop();
    }

    public static void main(String[] args) { launch(args); }
}