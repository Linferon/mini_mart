package dao.mapper;

import model.IncomeSource;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IncomeSourceMapper {
    private IncomeSourceMapper(){}
    
    public static IncomeSource mapRow(ResultSet rs) throws SQLException {
        return new IncomeSource(
            rs.getLong("ID"),
            rs.getString("NAME")
        );
    }
}