package dao.mapper;

import exception.DatabaseMapException;
import model.Income;
import model.IncomeSource;
import model.User;
import util.LoggerUtil;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class IncomeMapper {
    private IncomeMapper() {
    }

    public static Income mapRow(ResultSet rs) {
        try {
            Long id = rs.getLong("ID");
            Long sourceId = rs.getLong("SOURCE_ID");
            BigDecimal totalAmount = rs.getBigDecimal("TOTAL_AMOUNT");
            Timestamp incomeDate = rs.getTimestamp("INCOME_DATE");
            Long accountantId = rs.getLong("ACCOUNTANT_ID");

            String sourceName = rs.getString("SOURCE_NAME");
            String accountantName = rs.getString("ACCOUNTANT_NAME");
            String accountantSurname = null;

            try {
                accountantSurname = rs.getString("ACCOUNTANT_SURNAME");
            } catch (SQLException ignored) {}

            IncomeSource source = new IncomeSource(sourceId, sourceName);

            User accountant = new User(
                    accountantId,
                    accountantName,
                    accountantSurname
            );

            return new Income(id, source, totalAmount, incomeDate, accountant);
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping income from ResultSet", e);
            throw new DatabaseMapException("Error mapping income");
        }
    }
}