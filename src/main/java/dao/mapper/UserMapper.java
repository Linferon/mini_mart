package dao.mapper;

import model.User;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper {
    private UserMapper(){}

    public static User mapRow(ResultSet rs) throws SQLException {
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
    }
}