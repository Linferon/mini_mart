package service.interfaces;

import model.MonthlyBudget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface MonthlyBudgetService {
    List<MonthlyBudget> getAllBudgets();

    MonthlyBudget getBudgetById(Long id);

    MonthlyBudget getBudgetByDate(LocalDate date);

    List<MonthlyBudget> getBudgetsByDateRange(LocalDate startDate, LocalDate endDate);

    List<MonthlyBudget> getBudgetsByDirector(Long directorId);

    Long createBudget(MonthlyBudget budget);

    Long createBudget(LocalDate budgetDate, BigDecimal plannedIncome, BigDecimal plannedExpenses);

    boolean updateBudget(MonthlyBudget budget);

    boolean updateActualValues(Long budgetId, BigDecimal actualIncome, BigDecimal actualExpenses);

    boolean deleteBudget(Long id);

    BigDecimal getTotalPlannedIncome(LocalDate startDate, LocalDate endDate);

    BigDecimal getTotalPlannedExpenses(LocalDate startDate, LocalDate endDate);

    BigDecimal getTotalActualIncome(LocalDate startDate, LocalDate endDate);

    BigDecimal getTotalActualExpenses(LocalDate startDate, LocalDate endDate);

    BigDecimal getTotalNetResult(LocalDate startDate, LocalDate endDate);

    double getAvgBudgetExecutionRate(LocalDate startDate, LocalDate endDate);
}
