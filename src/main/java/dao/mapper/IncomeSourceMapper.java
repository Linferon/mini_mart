package dao.mapper;

import exception.DatabaseMapException;
import model.IncomeSource;

import java.sql.ResultSet;
import java.sql.SQLException;

import static util.LoggerUtil.error;

public class IncomeSourceMapper {
    private IncomeSourceMapper() {}

    public static IncomeSource mapRow(ResultSet rs){
        try {
            return new IncomeSource(
                    rs.getLong("ID"),
                    rs.getString("NAME")
            );
        } catch (SQLException e) {
            error("Error mapping role from ResultSet", e);
            throw new DatabaseMapException("Error mapping role");
        }
    }
}