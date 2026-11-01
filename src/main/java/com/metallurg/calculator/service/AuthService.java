package com.metallurg.calculator.service;

import com.metallurg.calculator.dao.UserDao;
import com.metallurg.calculator.model.User;
import com.metallurg.calculator.util.PasswordHasher;

import java.sql.SQLException;
import java.util.Optional;

/** Регистрация и вход пользователя. */
public class AuthService {

    private final UserDao userDao = new UserDao();
    private User currentUser;

    public User register(String username, String password) throws SQLException {
        validateCredentials(username, password);
        if (userDao.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hash(password, salt);
        currentUser = userDao.insert(username.trim(), hash, salt);
        return currentUser;
    }

    public User login(String username, String password) throws SQLException {
        validateCredentials(username, password);
        Optional<User> found = userDao.findByUsername(username.trim());
        if (found.isEmpty()) {
            throw new IllegalArgumentException("Неверный логин или пароль");
        }
        User user = found.get();
        if (!PasswordHasher.matches(password, user.getSalt(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Неверный логин или пароль");
        }
        currentUser = user;
        return user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }

    private void validateCredentials(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Введите логин");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Пароль должен быть не короче 4 символов");
        }
        if (!username.trim().matches("[A-Za-zА-Яа-я0-9_.-]{3,32}")) {
            throw new IllegalArgumentException("Логин: 3–32 символа (буквы, цифры, _ . -)");
        }
    }
}
