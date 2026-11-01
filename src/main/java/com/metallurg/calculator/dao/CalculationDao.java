package com.metallurg.calculator.dao;

import com.metallurg.calculator.db.Database;
import com.metallurg.calculator.model.AkosInput;
import com.metallurg.calculator.model.AkosResult;
import com.metallurg.calculator.model.MnlzInput;
import com.metallurg.calculator.model.MnlzResult;
import com.metallurg.calculator.util.SimpleJson;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Сохранение и выборка расчётов АКОС / МНЛЗ. */
public class CalculationDao {

    public void saveAkos(Integer userId, AkosInput input, AkosResult result) throws SQLException {
        String sql = """
                INSERT INTO akos_calculations(user_id, steel_grade, melt_mass_kg, source_mode, input_json, result_json)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            if (userId == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, userId);
            }
            ps.setString(2, input.getSteelGrade());
            ps.setDouble(3, input.getMeltMassKg());
            ps.setString(4, input.getSourceMode());
            ps.setString(5, SimpleJson.akosInput(input));
            ps.setString(6, SimpleJson.akosResult(result));
            ps.executeUpdate();
        }
    }

    public void saveMnlz(Integer userId, MnlzInput input, MnlzResult result) throws SQLException {
        String sql = """
                INSERT INTO mnlz_calculations(
                    user_id, steel_grade, converter_mass_t, width_m, thickness_m,
                    casting_speed_m_min, casting_time_min, strands, liquid_pool_m,
                    radius_m, height_m, machine_type, source_mode)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            if (userId == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, userId);
            }
            ps.setString(2, input.getSteelGrade());
            ps.setDouble(3, input.getConverterMassT());
            ps.setDouble(4, input.getWidthM());
            ps.setDouble(5, input.getThicknessM());
            ps.setDouble(6, input.getCastingSpeedMPerMin());
            ps.setDouble(7, input.getCastingTimeMin());
            ps.setInt(8, result.getStrands());
            ps.setDouble(9, result.getLiquidPoolDepthM());
            ps.setDouble(10, result.getRadiusM());
            ps.setDouble(11, result.getHeightM());
            ps.setString(12, result.getMachineType());
            ps.setString(13, input.getSourceMode());
            ps.executeUpdate();
        }
    }

    public List<MnlzRow> findMnlz(String filterGrade, String sortColumn, boolean asc) throws SQLException {
        String col = switch (sortColumn == null ? "id" : sortColumn) {
            case "steel_grade", "converter_mass_t", "strands", "liquid_pool_m", "radius_m", "created_at" -> sortColumn;
            default -> "id";
        };
        String dir = asc ? "ASC" : "DESC";
        StringBuilder sql = new StringBuilder(
                "SELECT id, steel_grade, converter_mass_t, width_m, thickness_m, casting_speed_m_min, "
                        + "casting_time_min, strands, liquid_pool_m, radius_m, height_m, machine_type, "
                        + "source_mode, created_at FROM mnlz_calculations");
        boolean hasFilter = filterGrade != null && !filterGrade.isBlank();
        if (hasFilter) {
            sql.append(" WHERE steel_grade LIKE ?");
        }
        sql.append(" ORDER BY ").append(col).append(' ').append(dir);

        List<MnlzRow> rows = new ArrayList<>();
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql.toString())) {
            if (hasFilter) {
                ps.setString(1, "%" + filterGrade.trim() + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new MnlzRow(
                            rs.getInt("id"),
                            rs.getString("steel_grade"),
                            rs.getDouble("converter_mass_t"),
                            rs.getDouble("width_m"),
                            rs.getDouble("thickness_m"),
                            rs.getDouble("casting_speed_m_min"),
                            rs.getDouble("casting_time_min"),
                            rs.getInt("strands"),
                            rs.getDouble("liquid_pool_m"),
                            rs.getDouble("radius_m"),
                            rs.getDouble("height_m"),
                            rs.getString("machine_type"),
                            rs.getString("source_mode"),
                            rs.getString("created_at")
                    ));
                }
            }
        }
        return rows;
    }

    public void deleteMnlz(int id) throws SQLException {
        try (PreparedStatement ps = Database.getInstance().getConnection()
                .prepareStatement("DELETE FROM mnlz_calculations WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<AkosRow> findAkos(String filterGrade) throws SQLException {
        String sql = """
                SELECT id, steel_grade, melt_mass_kg, source_mode, result_json, created_at
                FROM akos_calculations
                """;
        if (filterGrade != null && !filterGrade.isBlank()) {
            sql += " WHERE steel_grade LIKE ?";
        }
        sql += " ORDER BY id DESC";
        List<AkosRow> rows = new ArrayList<>();
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            if (filterGrade != null && !filterGrade.isBlank()) {
                ps.setString(1, "%" + filterGrade.trim() + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new AkosRow(
                            rs.getInt("id"),
                            rs.getString("steel_grade"),
                            rs.getDouble("melt_mass_kg"),
                            rs.getString("source_mode"),
                            rs.getString("result_json"),
                            rs.getString("created_at")
                    ));
                }
            }
        }
        return rows;
    }

    public void deleteAkos(int id) throws SQLException {
        try (PreparedStatement ps = Database.getInstance().getConnection()
                .prepareStatement("DELETE FROM akos_calculations WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int countAllCalculations() throws SQLException {
        try (var st = Database.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT (SELECT COUNT(*) FROM akos_calculations) + (SELECT COUNT(*) FROM mnlz_calculations)")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public record MnlzRow(int id, String steelGrade, double converterMassT, double widthM, double thicknessM,
                          double castingSpeed, double castingTime, int strands, double liquidPool,
                          double radius, double height, String machineType, String sourceMode, String createdAt) {
    }

    public record AkosRow(int id, String steelGrade, double meltMassKg, String sourceMode,
                          String resultJson, String createdAt) {
    }
}
