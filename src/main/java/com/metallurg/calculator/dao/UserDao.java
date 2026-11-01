package com.metallurg.calculator.dao;

import com.metallurg.calculator.db.Database;
import com.metallurg.calculator.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/** Доступ к таблице users. */
public class UserDao {

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, salt FROM users WHERE username = ?";
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public User insert(String username, String passwordHash, String salt) throws SQLException {
        String sql = "INSERT INTO users(username, password_hash, salt) VALUES (?, ?, ?)";
        try (PreparedStatement ps = Database.getInstance().getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, salt);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new User(keys.getInt(1), username, passwordHash, salt);
                }
            }
        }
        throw new SQLException("Не удалось создать пользователя");
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("salt")
        );
    }
}
