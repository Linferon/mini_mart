package controller;

import model.*;
import service.*;
import util.*;
import exception.handler.ExceptionHandler;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

public class AccountantController extends BaseController {
    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final PayrollService payrollService;
    private final IncomeSourceService incomeSourceService;
    private final ExpenseCategoryService expenseCategoryService;
    private final MonthlyBudgetService monthlyBudgetService;

    public AccountantController() {
        this.incomeService = IncomeService.getInstance();
        this.expenseService = ExpenseService.getInstance();
        this.payrollService = PayrollService.getInstance();
        this.incomeSourceService = IncomeSourceService.getInstance();
        this.expenseCategoryService = ExpenseCategoryService.getInstance();
        this.monthlyBudgetService = MonthlyBudgetService.getInstance();
    }

    @Override
    public void showMenu() {
        createMenu("Меню Бухгалтера")
                .addMenuItem("Управление доходами", this::manageIncomes)
                .addMenuItem("Управление расходами", this::manageExpenses)
                .addMenuItem("Начисление зарплат", this::managePayroll)
                .addExitItem("Выйти из системы")
                .show();
    }

    private void manageIncomes() {
        createMenu("Управление доходами")
                .addMenuItem("Добавить доход", this::addIncome)
                .addMenuItem("Просмотреть доходы", this::viewIncomes)
                .addMenuItem("Редактировать доход", this::editIncome)
                .addMenuItem("Удалить доход", this::deleteIncome)
                .addExitItem("Назад")
                .show();
    }

    private void addIncome() {
        ExceptionHandler.execute(() -> {
            showEntitiesTable(incomeSourceService.getAllIncomeSources(), "Источники дохода");

            Long sourceId = InputHandler.getLongInput("Введите ID источника дохода: ");
            BigDecimal amount = InputHandler.getBigDecimalInput("Введите сумму дохода: ");
            LocalDate incomeDate = InputHandler.getDateInput("Введите дату дохода (ГГГГ-ММ-ДД): ");

            incomeService.addIncome(sourceId, amount, incomeDate);
            updateMonthlyBudget(amount, incomeDate, true);

            showSuccess("Доход успешно добавлен.");
        }, "Ошибка при добавлении дохода");
    }

    private void viewIncomes() {
        ExceptionHandler.execute(() ->
                showDateRangeMenu((startDate, endDate) -> {
                    List<Income> incomes = incomeService.getIncomesByDateRange(startDate, endDate);
                    showEntitiesTable(incomes, "Список доходов за период " + startDate + " - " + endDate);

                    BigDecimal totalIncome = incomeService.getTotalIncome(incomes);
                    ConsoleUtil.println("\nОбщая сумма доходов за период: " + totalIncome);
                }), "Ошибка при просмотре доходов");
    }

    private void editIncome() {
        ExceptionHandler.execute(() -> {
            selectEntitiesForDateRange(
                    incomeService::getIncomesByDateRange,
                    "Выберите доход для редактирования"
            );

            Long incomeId = InputHandler.getLongInput("Введите ID дохода для редактирования: ");
            Income income = incomeService.getIncomeById(incomeId);

            showEntityDetails(income, "Редактирование дохода");

            LocalDate oldIncomeDate = getLocalDateFromTimestamp(income.getIncomeDate());
            BigDecimal oldAmount = income.getTotalAmount();

            showEntitiesTable(incomeSourceService.getAllIncomeSources(), "Доступные источники дохода");

            Long sourceId = getUpdatedLongValue("ID источника дохода", income.getSource().id());
            BigDecimal amount = getUpdatedBigDecimalValue("сумму дохода", income.getTotalAmount());
            LocalDate incomeDate = getUpdatedDateValue("дату дохода", oldIncomeDate);

            income.setSource(incomeSourceService.getIncomeSourceById(sourceId));
            income.setTotalAmount(amount);
            income.setIncomeDate(Timestamp.valueOf(incomeDate.atStartOfDay()));

            if (incomeService.updateIncome(income)) {
                updateBudgetOnEdit(oldAmount, amount, oldIncomeDate, incomeDate, true);
                showSuccess("Доход успешно обновлен.");
            } else {
                showError("Не удалось обновить доход.");
            }
        }, "Ошибка при редактировании дохода");
    }

    private void deleteIncome() {
        ExceptionHandler.execute(() -> {
            selectEntitiesForDateRange(
                    incomeService::getIncomesByDateRange,
                    "Выберите доход для удаления"
            );

            Long incomeId = InputHandler.getLongInput("Введите ID дохода для удаления: ");
            Income income = incomeService.getIncomeById(incomeId);

            LocalDate incomeDate = getLocalDateFromTimestamp(income.getIncomeDate());
            BigDecimal amount = income.getTotalAmount();

            if (incomeService.deleteIncome(incomeId)) {
                updateBudgetOnDelete(amount, incomeDate, true);
                showSuccess("Доход успешно удален.");
            } else {
                showError("Не удалось удалить доход.");
            }
        }, "Ошибка при удалении дохода");
    }

    private void manageExpenses() {
        createMenu("Управление расходами")
                .addMenuItem("Добавить расход", this::addExpense)
                .addMenuItem("Просмотреть расходы", this::viewExpenses)
                .addMenuItem("Редактировать расход", this::editExpense)
                .addMenuItem("Удалить расход", this::deleteExpense)
                .addExitItem("Назад")
                .show();
    }

    private void addExpense() {
        ExceptionHandler.execute(() -> {
            showEntitiesTable(expenseCategoryService.getAllExpenseCategories(), "Категории расходов");

            Long categoryId = InputHandler.getLongInput("Введите ID категории расхода: ");
            BigDecimal amount = InputHandler.getBigDecimalInput("Введите сумму расхода: ");
            LocalDate expenseDate = InputHandler.getDateInput("Введите дату расхода (ГГГГ-ММ-ДД): ");

            Timestamp expenseTimestamp = Timestamp.valueOf(expenseDate.atStartOfDay());

            ExpenseCategory category = expenseCategoryService.getExpenseCategoryById(categoryId);
            Expense expense = new Expense(category, amount);
            expense.setExpenseDate(expenseTimestamp);

            expenseService.addExpense(expense);
            updateMonthlyBudget(amount, expenseDate, false);

            showSuccess("Расход успешно добавлен.");
        }, "Ошибка при добавлении расхода");
    }

    private void viewExpenses() {
        ExceptionHandler.execute(() ->
                showDateRangeMenu((startDate, endDate) -> {
                    List<Expense> expenses = expenseService.getExpensesByDateRange(
                            DateTimeUtils.fromLocalDate(startDate),
                            DateTimeUtils.endOfDay(endDate)
                    );

                    showEntitiesTable(expenses, "Список расходов за период " + startDate + " - " + endDate);

                    BigDecimal totalExpense = expenseService.getTotalExpenses(expenses);
                    ConsoleUtil.println("\nОбщая сумма расходов за период: " + totalExpense);
                }), "Ошибка при просмотре расходов");
    }

    private void editExpense() {
        ExceptionHandler.execute(() -> {
            selectExpensesForDateRange("Выберите расход для редактирования");

            Long expenseId = InputHandler.getLongInput("Введите ID расхода для редактирования: ");
            Expense expense = expenseService.getExpenseById(expenseId);

            showEntityDetails(expense, "Редактирование расхода с ID " + expenseId);

            LocalDate oldExpenseDate = getLocalDateFromTimestamp(expense.getExpenseDate());
            BigDecimal oldAmount = expense.getTotalAmount();

            showEntitiesTable(expenseCategoryService.getAllExpenseCategories(), "Доступные категории расходов");

            Long categoryId = getUpdatedLongValue("ID категории расхода", expense.getCategory().id());
            BigDecimal amount = getUpdatedBigDecimalValue("сумму расхода", expense.getTotalAmount());
            LocalDate expenseDate = getUpdatedDateValue("дату расхода", oldExpenseDate);

            expense.setCategory(expenseCategoryService.getExpenseCategoryById(categoryId));
            expense.setTotalAmount(amount);
            expense.setExpenseDate(Timestamp.valueOf(expenseDate.atStartOfDay()));

            if (expenseService.updateExpense(expense)) {
                updateBudgetOnEdit(oldAmount, amount, oldExpenseDate, expenseDate, false);
                showSuccess("Расход успешно обновлен.");
            } else {
                showError("Не удалось обновить расход.");
            }
        }, "Ошибка при редактировании расхода");
    }

    private void deleteExpense() {
        ExceptionHandler.execute(() -> {
            selectExpensesForDateRange("Выберите расход для удаления");

            Long expenseId = InputHandler.getLongInput("Введите ID расхода для удаления: ");
            Expense expense = expenseService.getExpenseById(expenseId);

            LocalDate expenseDate = getLocalDateFromTimestamp(expense.getExpenseDate());
            BigDecimal amount = expense.getTotalAmount();

            expenseService.deleteExpense(expenseId);
            updateBudgetOnDelete(amount, expenseDate, false);

            showSuccess("Расход успешно удален.");
        }, "Ошибка при удалении расхода");
    }

    private void managePayroll() {
        createMenu("Начисление зарплат")
                .addMenuItem("Начислить зарплату сотруднику", this::addPayroll)
                .addMenuItem("Просмотреть зарплаты", this::viewPayrolls)
                .addMenuItem("Отметить зарплату как выплаченную", this::markPayrollAsPaid)
                .addExitItem("Назад")
                .show();
    }

    private void addPayroll() {
        ExceptionHandler.execute(() -> {
            Long employeeId = InputHandler.getLongInput("Введите ID сотрудника: ");
            float hoursWorked = InputHandler.getFloatInput("Введите количество отработанных часов: ");
            BigDecimal hourlyRate = InputHandler.getBigDecimalInput("Введите почасовую ставку: ");
            LocalDate periodStart = InputHandler.getDateInput("Введите дату начала периода (ГГГГ-ММ-ДД): ");
            LocalDate periodEnd = InputHandler.getDateInput("Введите дату окончания периода (ГГГГ-ММ-ДД): ");

            payrollService.createPayroll(employeeId, hoursWorked, hourlyRate, periodStart, periodEnd);
            showSuccess("Зарплата успешно начислена.");
        }, "Ошибка при начислении зарплаты");
    }

    private void viewPayrolls() {
        ExceptionHandler.execute(() ->
                showDateRangeMenu((startDate, endDate) -> {
                    List<Payroll> payrolls = payrollService.getPayrollsByPeriod(startDate, endDate);

                    showEntitiesTable(payrolls, "Список зарплат за период " + startDate + " - " + endDate);

                    long paidCount = payrolls.stream().filter(Payroll::isPaid).count();
                    long unpaidCount = payrolls.size() - paidCount;

                    ConsoleUtil.println("Выплачено: " + paidCount + " | Не выплачено: " + unpaidCount);
                }), "Ошибка при просмотре зарплат");
    }

    private void markPayrollAsPaid() {
        ExceptionHandler.execute(() -> {
            List<Payroll> unpaidPayrolls = payrollService.getUnpaidPayrolls();
            showEntitiesTable(unpaidPayrolls, "Невыплаченные зарплаты");

            Long payrollId = InputHandler.getLongInput("Введите ID зарплаты для отметки о выплате: ");
            String dateInput = InputHandler.getStringInput(
                    "Введите дату выплаты (ГГГГ-ММ-ДД) или нажмите Enter для текущей даты: "
            );
            LocalDate paymentDate = dateInput.isEmpty() ? LocalDate.now() : LocalDate.parse(dateInput);

            payrollService.markAsPaid(payrollId, paymentDate);
            showSuccess("Зарплата отмечена как выплаченная.");
        }, "Ошибка при отметке зарплаты как выплаченной");
    }

    private void updateMonthlyBudget(BigDecimal amount, LocalDate date, boolean isIncome) {
        try {
            LocalDate firstDayOfMonth = date.withDayOfMonth(1);
            MonthlyBudget budget = monthlyBudgetService.getBudgetByDate(firstDayOfMonth);

            BigDecimal newActualIncome = budget.getActualIncome();
            BigDecimal newActualExpenses = budget.getActualExpenses();

            if (isIncome) {
                newActualIncome = newActualIncome.add(amount);
            } else {
                newActualExpenses = newActualExpenses.add(amount);
            }

            monthlyBudgetService.updateActualValues(budget.getId(), newActualIncome, newActualExpenses);

            String budgetType = isIncome ? "доход" : "расход";
            ConsoleUtil.println("Фактический " + budgetType + " в бюджете на " +
                    firstDayOfMonth.getMonth() + " " + firstDayOfMonth.getYear() + " обновлен.");
        } catch (Exception e) {
            LoggerUtil.warn("Не удалось обновить месячный бюджет: " + e.getMessage());
        }
    }

    private void updateBudgetOnEdit(BigDecimal oldAmount, BigDecimal newAmount,
                                    LocalDate oldDate, LocalDate newDate, boolean isIncome) {
        try {
            if (!oldDate.withDayOfMonth(1).equals(newDate.withDayOfMonth(1))) {
                LocalDate oldFirstDayOfMonth = oldDate.withDayOfMonth(1);
                MonthlyBudget oldBudget = monthlyBudgetService.getBudgetByDate(oldFirstDayOfMonth);

                if (isIncome) {
                    BigDecimal newActualIncome = oldBudget.getActualIncome().subtract(oldAmount);
                    if (newActualIncome.compareTo(BigDecimal.ZERO) < 0) newActualIncome = BigDecimal.ZERO;
                    monthlyBudgetService.updateActualValues(oldBudget.getId(), newActualIncome, oldBudget.getActualExpenses());
                } else {
                    BigDecimal newActualExpenses = oldBudget.getActualExpenses().subtract(oldAmount);
                    if (newActualExpenses.compareTo(BigDecimal.ZERO) < 0) newActualExpenses = BigDecimal.ZERO;
                    monthlyBudgetService.updateActualValues(oldBudget.getId(), oldBudget.getActualIncome(), newActualExpenses);
                }

                updateMonthlyBudget(newAmount, newDate, isIncome);
            } else {
                BigDecimal diff = newAmount.subtract(oldAmount);
                if (diff.compareTo(BigDecimal.ZERO) != 0) {
                    LocalDate firstDayOfMonth = newDate.withDayOfMonth(1);
                    MonthlyBudget budget = monthlyBudgetService.getBudgetByDate(firstDayOfMonth);

                    if (isIncome) {
                        BigDecimal newActualIncome = budget.getActualIncome().add(diff);
                        if (newActualIncome.compareTo(BigDecimal.ZERO) < 0) newActualIncome = BigDecimal.ZERO;
                        monthlyBudgetService.updateActualValues(budget.getId(), newActualIncome, budget.getActualExpenses());
                    } else {
                        BigDecimal newActualExpenses = budget.getActualExpenses().add(diff);
                        if (newActualExpenses.compareTo(BigDecimal.ZERO) < 0) newActualExpenses = BigDecimal.ZERO;
                        monthlyBudgetService.updateActualValues(budget.getId(), budget.getActualIncome(), newActualExpenses);
                    }

                    String budgetType = isIncome ? "доход" : "расход";
                    ConsoleUtil.println("Фактический " + budgetType + " в бюджете на " +
                            firstDayOfMonth.getMonth() + " " + firstDayOfMonth.getYear() + " обновлен.");
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Не удалось корректно обновить месячные бюджеты: " + e.getMessage());
        }
    }

    private void updateBudgetOnDelete(BigDecimal amount, LocalDate date, boolean isIncome) {
        try {
            LocalDate firstDayOfMonth = date.withDayOfMonth(1);
            MonthlyBudget budget = monthlyBudgetService.getBudgetByDate(firstDayOfMonth);

            if (isIncome) {
                BigDecimal newActualIncome = budget.getActualIncome().subtract(amount);
                if (newActualIncome.compareTo(BigDecimal.ZERO) < 0) {
                    newActualIncome = BigDecimal.ZERO;
                }
                monthlyBudgetService.updateActualValues(budget.getId(), newActualIncome, budget.getActualExpenses());
                ConsoleUtil.println("Фактический доход в бюджете обновлен.");
            } else {
                BigDecimal newActualExpenses = budget.getActualExpenses().subtract(amount);
                if (newActualExpenses.compareTo(BigDecimal.ZERO) < 0) {
                    newActualExpenses = BigDecimal.ZERO;
                }
                monthlyBudgetService.updateActualValues(budget.getId(), budget.getActualIncome(), newActualExpenses);
                ConsoleUtil.println("Фактические расходы в бюджете обновлены.");
            }
        } catch (Exception e) {
            LoggerUtil.warn("Не удалось обновить месячный бюджет: " + e.getMessage());
        }
    }

    private <T extends FormattableEntity> void showEntitiesTable(List<T> entities, String title) {
        ConsoleUtil.printHeader(title);
        ConsoleUtil.println(TableFormatter.formatTable(entities));
    }

    private void showEntityDetails(Object entity, String title) {
        ConsoleUtil.printHeader(title);
        ConsoleUtil.println(entity.toString());
    }

    private Long getUpdatedLongValue(String fieldName, Long currentValue) {
        long value = InputHandler.getLongInput("Введите новый " + fieldName + " (или 0, чтобы оставить текущий): ");
        return value == 0 ? currentValue : value;
    }

    private BigDecimal getUpdatedBigDecimalValue(String fieldName, BigDecimal currentValue) {
        BigDecimal value = InputHandler.getBigDecimalInput("Введите новую " + fieldName + " (или 0, чтобы оставить текущую): ");
        return value.compareTo(BigDecimal.ZERO) == 0 ? currentValue : value;
    }

    private LocalDate getUpdatedDateValue(String fieldName, LocalDate currentValue) {
        String dateStr = InputHandler.getStringInput("Введите новую " + fieldName + " (ГГГГ-ММ-ДД, или оставьте пустым для текущей): ");
        return dateStr.isEmpty() ? currentValue : LocalDate.parse(dateStr);
    }

    private LocalDate getLocalDateFromTimestamp(Timestamp timestamp) {
        return LocalDate.ofInstant(timestamp.toInstant(), java.time.ZoneId.systemDefault());
    }

    private <T extends FormattableEntity> void selectEntitiesForDateRange(DateRangeSupplier<T> supplier, String headerMessage) {
        LocalDate startDate = InputHandler.getDateInput("Введите период для поиска дохода" + " (начальная дата, ГГГГ-ММ-ДД): ");
        LocalDate endDate = InputHandler.getDateInput("Введите период для поиска дохода" + " (конечная дата, ГГГГ-ММ-ДД): ");

        List<T> entities = supplier.getForDateRange(startDate, endDate);
        showEntitiesTable(entities, headerMessage);

    }

    private void selectExpensesForDateRange(String headerMessage) {
        LocalDate startDate = InputHandler.getDateInput("Введите начальную дату для поиска расхода (ГГГГ-ММ-ДД): ");
        LocalDate endDate = InputHandler.getDateInput("Введите конечную дату для поиска расхода (ГГГГ-ММ-ДД): ");

        List<Expense> expenses = expenseService.getExpensesByDateRange(
                DateTimeUtils.fromLocalDate(startDate),
                DateTimeUtils.endOfDay(endDate)
        );

        showEntitiesTable(expenses, headerMessage);
    }

    @FunctionalInterface
    private interface DateRangeSupplier<T> {
        List<T> getForDateRange(LocalDate startDate, LocalDate endDate);
    }
}