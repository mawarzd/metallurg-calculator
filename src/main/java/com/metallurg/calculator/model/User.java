package com.metallurg.calculator.model;

/** Пользователь системы. */
public class User {

    private final int id;
    private final String username;
    private final String passwordHash;
    private final String salt;

    public User(int id, String username, String passwordHash, String salt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }
}
