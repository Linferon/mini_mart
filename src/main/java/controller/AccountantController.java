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

    private final IncomeController incomeController;
    private final ExpenseController expenseController;
    private final PayrollController payrollController;

    public AccountantController() {
        this.incomeService = IncomeService.getInstance();
        this.expenseService = ExpenseService.getInstance();
        this.payrollService = PayrollService.getInstance();
        this.incomeSourceService = IncomeSourceService.getInstance();
        this.expenseCategoryService = ExpenseCategoryService.getInstance();
        userService = UserService.getInstance();

        this.incomeController = new IncomeController();
        this.expenseController = new ExpenseController();
        this.payrollController = new PayrollController();
    }

    @Override
    public void showMenu() {
        createMenu("Меню Бухгалтера")
                .addMenuItem("Управление доходами", incomeController::manageIncomes)
                .addMenuItem("Управление расходами", expenseController::manageExpenses)
                .addMenuItem("Начисление зарплат", payrollController::managePayroll)
                .addExitItem("Выйти из системы")
                .show();
    }

    private class IncomeController {
        public void manageIncomes() {
            createMenu("Управление доходами")
                    .addMenuItem("Просмотреть доходы", this::viewIncomes)
                    .addMenuItem("Добавить доход", this::addIncome)
                    .addMenuItem("Редактировать доход", this::editIncome)
                    .addMenuItem("Удалить доход", this::deleteIncome)
                    .addExitItem("Назад")
                    .show();
        }

        private void viewIncomes() {
            createMenu("Просмотреть доходы")
                    .addMenuItem("Все", this::viewAllIncomes)
                    .addMenuItem("По категориям", this::viewIncomesByCategory)
                    .addMenuItem("По временному диапазону", this::viewIncomesByDateRange)
                    .addExitItem("Назад")
                    .show();
        }

        private void viewAllIncomes() {
            ExceptionHandler.execute(() -> {
                List<Income> incomes = incomeService.getAllIncomes();
                ConsoleUtil.showEntitiesTable(incomes, "Список доходов:  ");

                BigDecimal totalIncome = incomeService.getTotalIncome(incomes);
                ConsoleUtil.println("\nОбщая сумма доходов за период: " + totalIncome);
            });
        }

        private void viewIncomesByCategory() {
            ExceptionHandler.execute(() -> {
                ConsoleUtil.showEntitiesTable(incomeSourceService.getAllIncomeSources(), "Доступные источники дохода");
                Long sourceId = InputHandler.getLongInput("ID источника дохода");

                List<Income> incomes = incomeService.getIncomesBySource(sourceId);
                ConsoleUtil.showEntitiesTable(incomes, "Список доходов по источнику дохода:  ");

                BigDecimal totalIncome = incomeService.getTotalIncome(incomes);
                ConsoleUtil.println("\nОбщая сумма доходов за период: " + totalIncome);
            });
        }

        private void viewIncomesByDateRange() {
            ExceptionHandler.execute(() ->
                    showDateRangeMenu((startDate, endDate) -> {
                        List<Income> incomes = incomeService.getIncomesByDateRange(startDate, endDate);
                        ConsoleUtil.showEntitiesTable(incomes, "Список доходов за период " + startDate + " - " + endDate);

                        BigDecimal totalIncome = incomeService.getTotalIncome(incomes);
                        ConsoleUtil.println("\nОбщая сумма доходов за период: " + totalIncome);
                    }));
        }

        private void addIncome() {
            ExceptionHandler.execute(() -> {
                ConsoleUtil.showEntitiesTable(incomeSourceService.getAllIncomeSources(), "Источники дохода");

                Long sourceId = InputHandler.getLongInput("Введите ID источника дохода: ");
                BigDecimal amount = InputHandler.getBigDecimalInput("Введите сумму дохода: ");
                LocalDate incomeDate = InputHandler.getDateInput("Введите дату дохода (ГГГГ-ММ-ДД): ");

                incomeService.addIncome(sourceId, amount, incomeDate);

                showSuccess("Доход успешно добавлен.");
            });
        }

        private void editIncome() {
            ExceptionHandler.execute(() -> {
                ConsoleUtil.selectEntitiesForDateRange(
                        incomeService::getIncomesByDateRange,
                        "Выберите доход для редактирования"
                );

                Long incomeId = InputHandler.getLongInput("Введите ID дохода для редактирования: ");
                Income income = incomeService.getIncomeById(incomeId);
                ConsoleUtil.showEntityDetails(income, "Редактирование дохода");

                ConsoleUtil.showEntitiesTable(incomeSourceService.getAllIncomeSources(), "Доступные источники дохода");
                Long sourceId = InputHandler.getUpdatedLongValue("ID источника дохода", income.getSource().id());

                BigDecimal amount = InputHandler.getUpdatedBigDecimalValue("сумму дохода", income.getTotalAmount());

                LocalDate oldIncomeDate = DateTimeUtils.getLocalDateFromTimestamp(income.getIncomeDate());
                LocalDate incomeDate = InputHandler.getUpdatedDateValue("дату дохода", oldIncomeDate);

                if (incomeService.updateIncome(incomeId, sourceId, amount, incomeDate)) {
                    showSuccess("Доход успешно обновлен.");
                } else {
                    showError("Не удалось обновить доход.");
                }
            });
        }

        private void deleteIncome() {
            ExceptionHandler.execute(() -> {
                ConsoleUtil.selectEntitiesForDateRange(
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
    }

    private class ExpenseController {
        public void manageExpenses() {
            createMenu("Управление расходами")
                    .addMenuItem("Просмотреть расходы", this::viewExpenses)
                    .addMenuItem("Добавить расход", this::addExpense)
                    .addMenuItem("Редактировать расход", this::editExpense)
                    .addMenuItem("Удалить расход", this::deleteExpense)
                    .addExitItem("Назад")
                    .show();
        }

        private void viewExpenses() {
            createMenu("Просмотреть расходы")
                    .addMenuItem("Все", this::viewAllExpenses)
                    .addMenuItem("По категориям", this::viewExpensesByCategory)
                    .addMenuItem("По временному диапазону", this::viewExpensesByDateRange)
                    .addExitItem("Назад")
                    .show();
        }

        private void viewAllExpenses() {
            ExceptionHandler.execute(() -> {
                List<Expense> expenses = expenseService.getAllExpenses();

                ConsoleUtil.showEntitiesTable(expenses, "Список всех расходов: ");

                BigDecimal totalExpense = expenseService.getTotalExpenses(expenses);
                ConsoleUtil.println("\nОбщая сумма расходов: " + totalExpense);
            });
        }

        private void viewExpensesByCategory() {
            ExceptionHandler.execute(() -> {
                ConsoleUtil.showEntitiesTable(expenseCategoryService.getAllExpenseCategories(), "Доступные категории расходов");
                Long categoryId = InputHandler.getLongInput("Введите id категории: ");
                List<Expense> expenses = expenseService.getExpensesByCategory(categoryId);

                ConsoleUtil.showEntitiesTable(expenses, "Список расходов по категории: ");

                BigDecimal totalExpense = expenseService.getTotalExpenses(expenses);
                ConsoleUtil.println("\nОбщая сумма расходов по категории: " + totalExpense);
            });
        }

        private void viewExpensesByDateRange() {
            ExceptionHandler.execute(() ->
                    showDateRangeMenu((startDate, endDate) -> {
                        List<Expense> expenses = expenseService.getExpensesByDateRange(
                                DateTimeUtils.fromLocalDate(startDate),
                                DateTimeUtils.endOfDay(endDate)
                        );

                        ConsoleUtil.showEntitiesTable(expenses, "Список расходов за период " + startDate + " - " + endDate);

                        BigDecimal totalExpense = expenseService.getTotalExpenses(expenses);
                        ConsoleUtil.println("\nОбщая сумма расходов за период: " + totalExpense);
                    }));
        }

        private void addExpense() {
            ExceptionHandler.execute(() -> {
                ConsoleUtil.showEntitiesTable(expenseCategoryService.getAllExpenseCategories(), "Категории расходов");

                Long categoryId = InputHandler.getLongInput("Введите ID категории расхода: ");
                BigDecimal amount = InputHandler.getBigDecimalInput("Введите сумму расхода: ");
                LocalDate expenseDate = InputHandler.getDateInput("Введите дату расхода (ГГГГ-ММ-ДД): ");

                Timestamp expenseTimestamp = Timestamp.valueOf(expenseDate.atStartOfDay());

                expenseService.addExpense(categoryId, amount, expenseTimestamp);

                showSuccess("Расход успешно добавлен.");
            });
        }

        private void editExpense() {
            ExceptionHandler.execute(() -> {
                selectExpensesForDateRange("Выберите расход для редактирования");

                Long expenseId = InputHandler.getLongInput("Введите ID расхода для редактирования: ");
                Expense expense = expenseService.getExpenseById(expenseId);
                ConsoleUtil.showEntityDetails(expense, "Редактирование расхода с ID " + expenseId);

                ConsoleUtil.showEntitiesTable(expenseCategoryService.getAllExpenseCategories(), "Доступные категории расходов");
                Long categoryId = InputHandler.getUpdatedLongValue("ID категории расхода", expense.getCategory().id());
                BigDecimal amount = InputHandler.getUpdatedBigDecimalValue("сумму расхода", expense.getTotalAmount());

                LocalDate oldExpenseDate = DateTimeUtils.getLocalDateFromTimestamp(expense.getExpenseDate());
                LocalDate expenseDate = InputHandler.getUpdatedDateValue("дату расхода", oldExpenseDate);

                if (expenseService.updateExpense(expenseId, categoryId, amount, expenseDate)) {
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

        private void selectExpensesForDateRange(String headerMessage) {
            LocalDate startDate = InputHandler.getDateInput("Введите начальную дату для поиска расхода (ГГГГ-ММ-ДД): ");
            LocalDate endDate = InputHandler.getDateInput("Введите конечную дату для поиска расхода (ГГГГ-ММ-ДД): ");

            List<Expense> expenses = expenseService.getExpensesByDateRange(
                    DateTimeUtils.fromLocalDate(startDate),
                    DateTimeUtils.endOfDay(endDate)
            );

            ConsoleUtil.showEntitiesTable(expenses, headerMessage);
        }
    }

    private class PayrollController {
        public void managePayroll() {
            createMenu("Начисление зарплат")
                    .addMenuItem("Просмотреть зарплаты", this::viewPayrolls)
                    .addMenuItem("Начислить зарплату сотруднику", this::addPayroll)
                    .addMenuItem("Редактировать зарплату", this::editPayroll)
                    .addMenuItem("Отметить зарплату как выплаченную", this::markPayrollAsPaid)
                    .addMenuItem("Удалить зарплату", this::deletePayroll)
                    .addExitItem("Назад")
                    .show();
        }

        private void viewPayrolls() {
            createMenu("Просмотреть зарплаты")
                    .addMenuItem("Все", this::viewAllPayrolls)
                    .addMenuItem("Невыплаченные", this::viewUnpaidPayrolls)
                    .addMenuItem("По временному диапазону", this::viewPayrollsByDateRange)
                    .addExitItem("Назад")
                    .show();
        }

        private void viewPayrollsByDateRange() {
            ExceptionHandler.execute(() ->
                    showDateRangeMenu((startDate, endDate) -> {
                        List<Payroll> payrolls = payrollService.getPayrollsByPeriod(startDate, endDate);

                        ConsoleUtil.showEntitiesTable(payrolls, "Список зарплат за период " + startDate + " - " + endDate);

                        long paidCount = payrolls.stream().filter(Payroll::isPaid).count();
                        long unpaidCount = payrolls.size() - paidCount;

                        ConsoleUtil.println("Выплачено: " + paidCount + " | Не выплачено: " + unpaidCount);
                    }));
        }

        private void viewUnpaidPayrolls() {
            ExceptionHandler.execute(() -> {
                List<Payroll> unpaidPayrolls = payrollService.getUnpaidPayrolls();
                ConsoleUtil.showEntitiesTable(unpaidPayrolls, "Невыплаченные зарплаты");
            });
        }

        private void viewAllPayrolls() {
            ExceptionHandler.execute(() -> {
                List<Payroll> payrolls = payrollService.getAllPayrolls();
                ConsoleUtil.showEntitiesTable(payrolls, "Список всех зарплат: ");

                long paidCount = payrolls.stream().filter(Payroll::isPaid).count();
                long unpaidCount = payrolls.size() - paidCount;

                ConsoleUtil.println("Выплачено: " + paidCount + " | Не выплачено: " + unpaidCount);
            });
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

        private void editPayroll() {
            ExceptionHandler.execute(() -> {
                selectPayrollsForDateRange("Выберите зарплату для редактирования");

                Long payrollId = InputHandler.getLongInput("Введите ID зарплаты для редактирования: ");
                Payroll payroll = payrollService.getPayrollById(payrollId);
                ConsoleUtil.showEntityDetails(payroll, "Редактирование зарплаты с ID " + payrollId);

                if (Boolean.TRUE.equals(payroll.isPaid())) {
                    showConfirmationMenu("Внимание! Зарплата уже выплачена. Продолжить редактирование? ", () -> {
                        showEditMenu(payroll);
                        showSuccess("Зарплата успешно обновлена.");
                    });
                } else {
                    showEditMenu(payroll);
                }
            });
        }

        private void showEditMenu(Payroll payroll) {
            List<User> users = userService.getActiveUsers();
            ConsoleUtil.printHeader("Сотрудники");
            ConsoleUtil.println(TableFormatter.formatTable(users));

            Long employeeId = InputHandler.getUpdatedLongValue("ID сотрудника", payroll.getEmployee().getId());
            float hoursWorked = InputHandler.getUpdatedFloatValue("количество отработанных часов", payroll.getHoursWorked());
            BigDecimal hourlyRate = InputHandler.getUpdatedBigDecimalValue("почасовую ставку", payroll.getHourlyRate());

            LocalDate periodStart = InputHandler.getUpdatedDateValue("дату начала периода", payroll.getPeriodStart());
            LocalDate periodEnd = InputHandler.getUpdatedDateValue("дату окончания периода", payroll.getPeriodEnd());

            boolean updated = payrollService.updatePayroll(payroll.getId(), employeeId, hoursWorked, hourlyRate, periodStart, periodEnd);

            if (updated) {
                showSuccess("Зарплата успешно обновлена.");
            } else {
                showError("Не удалось обновить зарплату.");
            }
        }

        private void markPayrollAsPaid() {
            ExceptionHandler.execute(() -> {
                viewUnpaidPayrolls();

                Long payrollId = InputHandler.getLongInput("Введите ID зарплаты для отметки о выплате: ");
                String dateInput = InputHandler.getStringInput(
                        "Введите дату выплаты (ГГГГ-ММ-ДД) или нажмите Enter для текущей даты: "
                );
                LocalDate paymentDate = dateInput.isEmpty() ? LocalDate.now() : LocalDate.parse(dateInput);

                payrollService.markAsPaid(payrollId, paymentDate);
                showSuccess("Зарплата отмечена как выплаченная.");
            });
        }

        private void deletePayroll() {
            ExceptionHandler.execute(() -> {
                selectPayrollsForDateRange("Выберите зарплату для удаления");

                Long payrollId = InputHandler.getLongInput("Введите ID зарплаты для удаления: ");
                Payroll payroll = payrollService.getPayrollById(payrollId);

                if (Boolean.TRUE.equals(payroll.isPaid())) {
                    showConfirmationMenu("Внимание! Зарплата уже выплачена. Продолжить редактирование? ", () -> {
                        payrollService.deletePayroll(payrollId);
                        showSuccess("Зарплата была удалена.");
                    });
                } else {
                    payrollService.deletePayroll(payrollId);
                    showSuccess("Зарплата успешно удалена.");
                }
            });
        }

        private void selectPayrollsForDateRange(String headerMessage) {
            LocalDate startDate = InputHandler.getDateInput("Введите начальную дату для поиска зарплаты (ГГГГ-ММ-ДД): ");
            LocalDate endDate = InputHandler.getDateInput("Введите конечную дату для поиска зарплаты (ГГГГ-ММ-ДД): ");

            List<Payroll> payrolls = payrollService.getPayrollsByPeriod(startDate, endDate);
            ConsoleUtil.showEntitiesTable(payrolls, headerMessage);
        }
    }
}