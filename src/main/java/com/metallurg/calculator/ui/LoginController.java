package com.metallurg.calculator.ui;

import com.metallurg.calculator.MainApp;
import com.metallurg.calculator.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/** Контроллер окна входа и регистрации. */
public class LoginController {

    private final AuthService authService = new AuthService();

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    private void onLogin() {
        try {
            authService.login(usernameField.getText(), passwordField.getText());
            MainApp.showMain(authService.getCurrentUser().getUsername());
        } catch (Exception e) {
            messageLabel.setText(rootMessage(e));
            e.printStackTrace();
        }
    }

    @FXML
    private void onRegister() {
        try {
            authService.register(usernameField.getText(), passwordField.getText());
            MainApp.showMain(authService.getCurrentUser().getUsername());
        } catch (Exception e) {
            messageLabel.setText(rootMessage(e));
            e.printStackTrace();
        }
    }

    private static String rootMessage(Throwable e) {
        Throwable cur = e;
        while (cur.getCause() != null) {
            cur = cur.getCause();
        }
        String msg = cur.getMessage();
        return (msg == null || msg.isBlank()) ? e.toString() : msg;
    }
}
