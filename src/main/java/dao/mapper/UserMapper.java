package dao.mapper;

import dao.impl.RoleDao;
import exception.DatabaseMapException;
import model.Role;
import model.User;
import util.LoggerUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper {
    private static final RoleDao roleDao = new RoleDao();

    private UserMapper() {
    }

    public static User mapRow(ResultSet rs) {
        try {
            Long roleId = rs.getLong("ROLE_ID");
            Role role = roleDao.findById(roleId).orElse(new Role(roleId, "Unknown Role"));

            return new User(
                    rs.getLong("ID"),
                    rs.getString("NAME"),
                    rs.getString("SURNAME"),
                    rs.getString("EMAIL"),
                    rs.getString("PASSWORD"),
                    role,
                    rs.getTimestamp("CREATED_AT"),
                    rs.getTimestamp("UPDATED_AT")
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping user from ResultSet", e);
            throw new DatabaseMapException("Error mapping user");
        }
    }
}