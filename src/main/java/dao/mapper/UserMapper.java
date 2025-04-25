package dao.mapper;

import exception.DatabaseMapException;
import model.Role;
import model.User;
import util.LoggerUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UserMapper {
    private UserMapper() {
    }

    public static User mapRow(ResultSet rs) {
        try {
            Long id = rs.getLong("ID");
            String name = rs.getString("NAME");
            String surname = rs.getString("SURNAME");
            String email = rs.getString("EMAIL");
            String password = rs.getString("PASSWORD");
            Boolean enabled = rs.getBoolean("ENABLED");
            Long roleId = rs.getLong("ROLE_ID");
            Timestamp createdAt = rs.getTimestamp("CREATED_AT");
            Timestamp updatedAt = rs.getTimestamp("UPDATED_AT");

            String roleName = rs.getString("ROLE_NAME");

            Role role = new Role(roleId, roleName);

            return new User(id, name, surname, email, password, enabled, role, createdAt, updatedAt);
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping user from ResultSet", e);
            throw new DatabaseMapException("Error mapping user");
        }
    }
}