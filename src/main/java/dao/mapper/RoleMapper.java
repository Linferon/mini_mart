package dao.mapper;

import exception.DatabaseMapException;
import model.Role;

import java.sql.ResultSet;
import java.sql.SQLException;

import static util.LoggerUtil.error;

public class RoleMapper{
    private RoleMapper(){}

    public static Role mapRow(ResultSet rs) {
        try {
            return new Role(
                    rs.getLong("ID"),
                    rs.getString("NAME")
            );
        } catch (SQLException e) {
            error("Error mapping role from ResultSet", e);
            throw new DatabaseMapException("Error mapping role");
        }
    }
}