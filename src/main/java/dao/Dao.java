package dao;

import util.DatabaseConnection;
import util.LoggerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class Dao<T> {

    protected abstract String getTableName();

    protected abstract Function<ResultSet, T> getMapper();

    protected Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    protected Optional<T> querySingle(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = prepareStatement(conn, sql, params)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(getMapper().apply(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Ошибка при выполнении запроса: " + sql, e);
        }

        return Optional.empty();
    }

    protected List<T> queryList(String sql, Object... params) {
        List<T> result = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = prepareStatement(conn, sql, params)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(getMapper().apply(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Ошибка при выполнении запроса: " + sql, e);
        }

        return result;
    }

    protected Long insert(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = prepareStatement(conn, sql, params, Statement.RETURN_GENERATED_KEYS)) {

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Создание записи не удалось, строки не затронуты.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Создание записи не удалось, ID не получен.");
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Ошибка при выполнении вставки: " + sql, e);
        }

        return null;
    }

    protected boolean update(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = prepareStatement(conn, sql, params)) {

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Ошибка при обновлении: " + sql, e);
            return false;
        }
    }

    protected boolean delete(String sql, Object... params) {
        return update(sql, params);
    }

    private PreparedStatement prepareStatement(Connection conn, String sql, Object[] params) throws SQLException {
        return prepareStatement(conn, sql, params, 0);
    }

    private PreparedStatement prepareStatement(Connection conn, String sql, Object[] params, int returnKeys) throws SQLException {
        PreparedStatement pstmt;

        if (returnKeys == 0) {
            pstmt = conn.prepareStatement(sql);
        } else {
            pstmt = conn.prepareStatement(sql, returnKeys);
        }

        setParameters(pstmt, params);
        return pstmt;
    }

    private void setParameters(PreparedStatement pstmt, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            switch (params[i]) {
                case null -> pstmt.setNull(i + 1, Types.NULL);
                case String string -> pstmt.setString(i + 1, string);
                case Integer integer -> pstmt.setInt(i + 1, integer);
                case Long longL -> pstmt.setLong(i + 1, longL);
                case Double doubleD -> pstmt.setDouble(i + 1, doubleD);
                case Boolean bool -> pstmt.setBoolean(i + 1, bool);
                case Timestamp timestamp -> pstmt.setTimestamp(i + 1, timestamp);
                case Date date -> pstmt.setDate(i + 1, date);
                case java.math.BigDecimal bigDecimal -> pstmt.setBigDecimal(i + 1, bigDecimal);
                default -> pstmt.setObject(i + 1, params[i]);
            }
        }
    }


    public Optional<T> findById(Long id) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE ID = ?";
        return querySingle(sql, id);
    }

    public List<T> findAll() {
        String sql = "SELECT * FROM " + getTableName();
        return queryList(sql);
    }

    public boolean deleteById(Long id) {
        String sql = "DELETE FROM " + getTableName() + " WHERE ID = ?";
        return delete(sql, id);
    }
}