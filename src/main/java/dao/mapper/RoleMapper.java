package dao.mapper;

import model.Role;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RoleMapper{
    private RoleMapper(){}

    public static Role mapRow(ResultSet rs) throws SQLException {
        return new Role(
            rs.getLong("ID"),
            rs.getString("NAME")
        );
    }
}