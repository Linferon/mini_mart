package service;

import model.Income;
import model.IncomeSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IncomeService {
    List<Income> getAllIncomes();

    Income getIncomeById(Long id);

    List<Income> getIncomesBySource(Long sourceId);

    List<Income> getIncomesByAccountant(Long accountantId);

    List<Income> getIncomesByDateRange(LocalDate startDate, LocalDate endDate);

    Long addIncome(Income income);

    Long addIncome(Long sourceId, BigDecimal amount, LocalDate incomeDate);

    boolean updateIncome(Income income);

    boolean deleteIncome(Long id);

    BigDecimal getTotalIncomeAmount(LocalDate startDate, LocalDate endDate);

    Map<IncomeSource, BigDecimal> getIncomesBySource(LocalDate startDate, LocalDate endDate);

    BigDecimal getMonthlyAverage(int numberOfMonths);
}
