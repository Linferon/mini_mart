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
    private final UserService userService;

    public AccountantController() {
        this.incomeService = IncomeService.getInstance();
        this.expenseService = ExpenseService.getInstance();
        this.payrollService = PayrollService.getInstance();
        this.incomeSourceService = IncomeSourceService.getInstance();
        this.expenseCategoryService = ExpenseCategoryService.getInstance();
        userService = UserService.getInstance();
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

            showSuccess("Доход успешно добавлен.");
        });
    }

    private void viewIncomes() {
        ExceptionHandler.execute(() ->
                showDateRangeMenu((startDate, endDate) -> {
                    List<Income> incomes = incomeService.getIncomesByDateRange(startDate, endDate);
                    showEntitiesTable(incomes, "Список доходов за период " + startDate + " - " + endDate);

                    BigDecimal totalIncome = incomeService.getTotalIncome(incomes);
                    ConsoleUtil.println("\nОбщая сумма доходов за период: " + totalIncome);
                }));
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
            showEntitiesTable(incomeSourceService.getAllIncomeSources(), "Доступные источники дохода");

            Long sourceId = InputHandler.getUpdatedLongValue("ID источника дохода", income.getSource().id());
            IncomeSource incomeSource = incomeSourceService.getIncomeSourceById(sourceId);

            BigDecimal amount = InputHandler.getUpdatedBigDecimalValue("сумму дохода", income.getTotalAmount());

            LocalDate oldIncomeDate = DateTimeUtils.getLocalDateFromTimestamp(income.getIncomeDate());
            LocalDate incomeDate = InputHandler.getUpdatedDateValue("дату дохода", oldIncomeDate);

            income.setSource(incomeSource);
            income.setTotalAmount(amount);
            income.setIncomeDate(Timestamp.valueOf(incomeDate.atStartOfDay()));

            if (incomeService.updateIncome(income)) {
                showSuccess("Доход успешно обновлен.");
            } else {
                showError("Не удалось обновить доход.");
            }
        });
    }

    private void deleteIncome() {
        ExceptionHandler.execute(() -> {
            selectEntitiesForDateRange(
                    incomeService::getIncomesByDateRange,
                    "Выберите доход для удаления"
            );

            Long incomeId = InputHandler.getLongInput("Введите ID дохода для удаления: ");

            if (incomeService.deleteIncome(incomeId)) {
                showSuccess("Доход успешно удален.");
            } else {
                showError("Не удалось удалить доход.");
            }
        });
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

            Expense expense = new Expense(category, amount, expenseTimestamp);
            expenseService.addExpense(expense);

            showSuccess("Расход успешно добавлен.");
        });
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
                }));
    }

    private void editExpense() {
        ExceptionHandler.execute(() -> {
            selectExpensesForDateRange("Выберите расход для редактирования");

            Long expenseId = InputHandler.getLongInput("Введите ID расхода для редактирования: ");
            Expense expense = expenseService.getExpenseById(expenseId);
            LocalDate oldExpenseDate = DateTimeUtils.getLocalDateFromTimestamp(expense.getExpenseDate());

            showEntityDetails(expense, "Редактирование расхода с ID " + expenseId);
            showEntitiesTable(expenseCategoryService.getAllExpenseCategories(), "Доступные категории расходов");

            Long categoryId = InputHandler.getUpdatedLongValue("ID категории расхода", expense.getCategory().id());
            ExpenseCategory expenseCategory = expenseCategoryService.getExpenseCategoryById(categoryId);
            BigDecimal amount = InputHandler.getUpdatedBigDecimalValue("сумму расхода", expense.getTotalAmount());
            LocalDate expenseDate = InputHandler.getUpdatedDateValue("дату расхода", oldExpenseDate);

            expense.setCategory(expenseCategory);
            expense.setTotalAmount(amount);
            expense.setExpenseDate(Timestamp.valueOf(expenseDate.atStartOfDay()));

            if (expenseService.updateExpense(expense)) {
                showSuccess("Расход успешно обновлен.");
            } else {
                showError("Не удалось обновить расход.");
            }
        });
    }

    private void deleteExpense() {
        ExceptionHandler.execute(() -> {
            selectExpensesForDateRange("Выберите расход для удаления");

            Long expenseId = InputHandler.getLongInput("Введите ID расхода для удаления: ");

            expenseService.deleteExpense(expenseId);
            showSuccess("Расход успешно удален.");
        });
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
            List<User> users = userService.getActiveUsers();
            ConsoleUtil.printHeader("Сотрудники");
            ConsoleUtil.println(TableFormatter.formatTable(users));
            Long employeeId = InputHandler.getLongInput("Введите ID сотрудника: ");
            float hoursWorked = InputHandler.getFloatInput("Введите количество отработанных часов: ");
            BigDecimal hourlyRate = InputHandler.getBigDecimalInput("Введите почасовую ставку: ");
            LocalDate periodStart = InputHandler.getDateInput("Введите дату начала периода (ГГГГ-ММ-ДД): ");
            LocalDate periodEnd = InputHandler.getDateInput("Введите дату окончания периода (ГГГГ-ММ-ДД): ");

            payrollService.createPayroll(employeeId, hoursWorked, hourlyRate, periodStart, periodEnd);
            showSuccess("Зарплата успешно начислена.");
        });
    }

    private void viewPayrolls() {
        ExceptionHandler.execute(() ->
                showDateRangeMenu((startDate, endDate) -> {
                    List<Payroll> payrolls = payrollService.getPayrollsByPeriod(startDate, endDate);

                    showEntitiesTable(payrolls, "Список зарплат за период " + startDate + " - " + endDate);

                    long paidCount = payrolls.stream().filter(Payroll::isPaid).count();
                    long unpaidCount = payrolls.size() - paidCount;

                    ConsoleUtil.println("Выплачено: " + paidCount + " | Не выплачено: " + unpaidCount);
                }));
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
        });
    }

    private <T extends FormattableEntity> void showEntitiesTable(List<T> entities, String title) {
        ConsoleUtil.printHeader(title);
        ConsoleUtil.println(TableFormatter.formatTable(entities));
    }

    private void showEntityDetails(Object entity, String title) {
        ConsoleUtil.printHeader(title);
        ConsoleUtil.println(entity.toString());
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
