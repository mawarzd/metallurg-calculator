package com.metallurg.calculator;

import com.metallurg.calculator.db.Database;
import com.metallurg.calculator.db.DatabaseSeeder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Главное JavaFX-приложение «Калькулятор металлурга».
 */
public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void init() throws Exception {
        Database.getInstance().initialize();
        DatabaseSeeder.seedIfNeeded();
    }

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        showLogin();
    }

    public static void showLogin() throws IOException {
        Parent root = FXMLLoader.load(MainApp.class.getResource("/fxml/login.fxml"));
        Scene scene = new Scene(root, 520, 480);
        scene.getStylesheets().add(MainApp.class.getResource("/css/app.css").toExternalForm());
        primaryStage.setTitle("Калькулятор металлурга — вход");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void showMain(String username) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/main.fxml"));
        Parent root = loader.load();
        com.metallurg.calculator.ui.MainController controller = loader.getController();
        controller.setCurrentUser(username);
        Scene scene = new Scene(root, 1180, 760);
        scene.getStylesheets().add(MainApp.class.getResource("/css/app.css").toExternalForm());
        primaryStage.setTitle("Калькулятор металлурга — " + username);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(980);
        primaryStage.setMinHeight(640);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
