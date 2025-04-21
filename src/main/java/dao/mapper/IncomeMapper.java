package dao.mapper;

import dao.impl.IncomeSourceDao;
import dao.impl.UserDao;
import exception.DatabaseMapException;
import model.Income;
import model.IncomeSource;
import model.User;
import util.LoggerUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IncomeMapper {
    private static final IncomeSourceDao sourceDao = new IncomeSourceDao();
    private static final UserDao userDao = new UserDao();

    private IncomeMapper() {
    }

    public static Income mapRow(ResultSet rs) {
        try {
            Long sourceId = rs.getLong("SOURCE_ID");
            Long accountantId = rs.getLong("ACCOUNTANT_ID");

            IncomeSource source = sourceDao.findById(sourceId)
                    .orElse(new IncomeSource(sourceId, "Unknown Source"));

            User accountant = userDao.findById(accountantId)
                    .orElse(new User(accountantId, "Unknown", "User", "", "", null, null, null));

            return new Income(
                    rs.getLong("ID"),
                    source,
                    rs.getBigDecimal("TOTAL_AMOUNT"),
                    rs.getTimestamp("INCOME_DATE"),
                    accountant
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping income from ResultSet", e);
            throw new DatabaseMapException("Error mapping income");
        }
    }
}