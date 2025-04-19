package dao.mapper;

import exception.DatabaseMapException;
import model.User;
import util.LoggerUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper {
    private UserMapper() {
    }

    public static User mapRow(ResultSet rs) {
        try {
            return new User(
                    rs.getLong("ID"),
                    rs.getString("NAME"),
                    rs.getString("SURNAME"),
                    rs.getString("EMAIL"),
                    rs.getString("PASSWORD"),
                    rs.getLong("ROLE_ID"),
                    rs.getTimestamp("CREATED_AT"),
                    rs.getTimestamp("UPDATED_AT")
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping role from ResultSet", e);
            throw new DatabaseMapException("Error mapping role");
        }
    }
}