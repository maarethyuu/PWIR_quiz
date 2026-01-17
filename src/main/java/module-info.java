module com.example.quiz {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.quiz.client to javafx.graphics, javafx.fxml;

    exports com.example.quiz.client;
    exports com.example.quiz.server;
    exports com.example.quiz.common;
}