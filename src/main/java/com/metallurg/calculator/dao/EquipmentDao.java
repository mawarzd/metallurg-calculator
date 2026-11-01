package com.metallurg.calculator.dao;

import com.metallurg.calculator.db.Database;
import com.metallurg.calculator.model.Equipment;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Доступ к витрине оборудования. */
public class EquipmentDao {

    public List<Equipment> findAll() throws SQLException {
        return query("SELECT * FROM equipment ORDER BY type, name", null);
    }

    public List<Equipment> search(String text) throws SQLException {
        String like = "%" + text.trim() + "%";
        String sql = """
                SELECT * FROM equipment
                WHERE name LIKE ? OR type LIKE ? OR manufacturer LIKE ? OR description LIKE ?
                ORDER BY type, name
                """;
        List<Equipment> list = new ArrayList<>();
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public List<Equipment> filterByType(String type) throws SQLException {
        String sql = "SELECT * FROM equipment WHERE type = ? ORDER BY name";
        List<Equipment> list = new ArrayList<>();
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public void deleteById(int id) throws SQLException {
        try (PreparedStatement ps = Database.getInstance().getConnection()
                .prepareStatement("DELETE FROM equipment WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        try (var st = Database.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM equipment")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public void insert(String name, String type, Double capacityT, String description, String manufacturer)
            throws SQLException {
        String sql = """
                INSERT INTO equipment(name, type, capacity_t, description, manufacturer)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, type);
            if (capacityT == null) {
                ps.setNull(3, java.sql.Types.REAL);
            } else {
                ps.setDouble(3, capacityT);
            }
            ps.setString(4, description);
            ps.setString(5, manufacturer);
            ps.executeUpdate();
        }
    }

    private List<Equipment> query(String sql, String unused) throws SQLException {
        List<Equipment> list = new ArrayList<>();
        try (var st = Database.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    private Equipment map(ResultSet rs) throws SQLException {
        double cap = rs.getDouble("capacity_t");
        Double capacity = rs.wasNull() ? null : cap;
        return new Equipment(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("type"),
                capacity,
                rs.getString("description"),
                rs.getString("manufacturer")
        );
    }
}
