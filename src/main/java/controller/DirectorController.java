package controller;

import model.MonthlyBudget;
import model.Payroll;
import model.Role;
import model.User;
import service.*;
import util.*;
import exception.handler.ExceptionHandler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class DirectorController extends BaseController {
    private final UserService userService;
    private final MonthlyBudgetService budgetService;
    private final PayrollService payrollService;

    private final List<Role> roles;

    private final BudgetController budgetController;
    private final EmployeeController employeeController;
    private final StatisticController statisticController;

    public DirectorController() {
        userService = UserService.getInstance();
        budgetService = MonthlyBudgetService.getInstance();
        payrollService = PayrollService.getInstance();

        roles = RoleService.getInstance().getAllRoles();

        budgetController = new BudgetController();
        employeeController = new EmployeeController();
        statisticController = new StatisticController();
    }

    @Override
    public void showMenu() {
        createMenu("Меню Директора")
                .addMenuItem("Управление бюджетами", budgetController::manageBudgets)
                .addMenuItem("Управление сотрудниками", employeeController::manageEmployees)
                .addMenuItem("Просмотр статистики", statisticController::viewStatistics)
                .addExitItem("Выйти из системы")
                .show();
    }

    private class BudgetController{
        private void manageBudgets() {
            createMenu("Управление бюджетами")
                    .addMenuItem("Посмотреть все месячные бюджеты", this::viewAllBudgets)
                    .addMenuItem("Посмотреть бюджет по временному диапазону", this::viewBudgetsByDateRange)
                    .addMenuItem("Назначить бюджет на месяц", this::setBudget)
                    .addMenuItem("Изменить бюджет", this::editBudget)
                    .addExitItem("Назад")
                    .show();
        }

        private void viewAllBudgets() {
            ExceptionHandler.execute(() -> {
                List<MonthlyBudget> budgets = budgetService.getAllBudgets();
                ConsoleUtil.showEntitiesTable(budgets, "Список всех бюджетов на месяц: ");
            });
        }

        protected void viewBudgetsByDateRange(LocalDate startDate, LocalDate endDate) {
            ExceptionHandler.execute(() -> {
                List<MonthlyBudget> budgets = budgetService.getBudgetsByDateRange(startDate, endDate);
                ConsoleUtil.printHeader("Статистика бюджета за период " + startDate + " - " + endDate);
                ConsoleUtil.println(TableFormatter.formatTable(budgets));
            });
        }

        private void viewBudgetsByDateRange() {
            ExceptionHandler.execute(() -> showDateRangeMenu(this::viewBudgetsByDateRange));
        }

        private void setBudget() {
            ExceptionHandler.execute(() -> {
                BigDecimal plannedIncome = InputHandler.getBigDecimalInput("Введите сумму планируемого дохода: ");
                BigDecimal plannedExpenses = InputHandler.getBigDecimalInput("Введите сумму планируемых расходов: ");
                LocalDate budgetDate = InputHandler.getValidBudgetDate();

                budgetService.createBudget(budgetDate, plannedIncome, plannedExpenses);
                showSuccess("Бюджет успешно установлен.");
            });
        }

        private void editBudget() {
            viewBudgetsByDateRange();
            Long budgetId = InputHandler.getLongInput("Введите id бюджета для редактирования");
            MonthlyBudget budget = budgetService.getBudgetById(budgetId);
            ConsoleUtil.showEntityDetails(budget, "Редактирование бюджета");

            BigDecimal plannedIncome = InputHandler.getUpdatedBigDecimalValue("сумму планируемых доходов", budget.getPlannedIncome());
            BigDecimal plannedExpense = InputHandler.getUpdatedBigDecimalValue("сумму планируемых расходов", budget.getPlannedExpenses());
            LocalDate oldIncomeDate = budget.getBudgetDate();
            LocalDate budgetDate = InputHandler.getUpdatedDateValue("дату бюджета", oldIncomeDate);

            if (budgetService.updateBudget(budgetId, plannedIncome, plannedExpense, budgetDate)) {
                showSuccess("Доход успешно обновлен.");
            } else {
                showError("Не удалось обновить доход.");
            }
        }
    }

    private class EmployeeController{
        private void manageEmployees() {
            createMenu("Управление сотрудниками")
                    .addMenuItem("Показать всех сотрудников", this::viewAllEmployees)
                    .addMenuItem("Зарегистрировать нового сотрудника", this::registerNewEmployee)
                    .addMenuItem("Изменить данные сотрудника", this::updateEmployee)
                    .addMenuItem("Уволить сотрудника или восстановить сотрудника", this::toggleEmployeeStatus)
                    .addExitItem("Назад")
                    .show();
        }

        private void viewAllEmployees() {
            ExceptionHandler.execute(() -> {
                List<User> users = userService.getActiveUsers();
                Map<String, Long> roleStats = userService.getRoleStats();

                ConsoleUtil.showEntitiesTable(users, "Активные сотрудники");

                ConsoleUtil.printHeader("Распределение сотрудников по ролям");
                roleStats.forEach((role, count) ->
                        ConsoleUtil.println(role + ": " + count + " чел.")
                );
            });
        }

        private void registerNewEmployee() {
            ExceptionHandler.execute(() -> {
                String name = InputHandler.getStringInput("Введите имя сотрудника: ");
                String surname = InputHandler.getStringInput("Введите фамилию сотрудника: ");
                String email = InputHandler.getStringInput("Введите email: ");
                String password = InputHandler.getStringInput("Введите пароль: ");
                Role selectedRole = selectRole();

                User newUser = new User(null, name, surname, email, password, true, selectedRole, null, null);
                userService.registerUser(newUser);
                showSuccess("Сотрудник успешно зарегистрирован.");
            });
        }

        private void updateEmployee() {
            ExceptionHandler.execute(() -> {
                viewAllEmployees();
                Long userId = InputHandler.getLongInput("Введите ID сотрудника для обновления: ");
                User existingUser = userService.getUserById(userId);

                ConsoleUtil.showEntityDetails(existingUser, "Текущие данные сотрудника");

                InputHandler.inputIfPresent("Введите новое имя", existingUser::setName);
                InputHandler.inputIfPresent("Введите новую фамилию", existingUser::setSurname);
                InputHandler.inputIfPresent("Введите новый email", existingUser::setEmail);

                if (!userService.getCurrentUser().equals(existingUser)) {
                    showConfirmationMenu("Хотите изменить роль? ", () -> {
                        Role selectedRole = selectRole();
                        existingUser.setRole(selectedRole);
                    });
                }

                userService.updateUser(existingUser);
                showSuccess("Данные сотрудника обновлены.");
            });
        }

        private void toggleEmployeeStatus() {
            ExceptionHandler.execute(() -> {
                viewAllEmployees();
                Long userId = InputHandler.getLongInput("Введите ID сотрудника: ");
                User user = userService.getUserById(userId);

                validateSelfDismiss(user);
                user.setEnabled(!user.getEnabled());
                userService.updateUser(user);

                String action = Boolean.TRUE.equals(user.getEnabled()) ? "восстановлен" : "уволен";
                showSuccess("Сотрудник успешно " + action + ".");
            });
        }

        private Role selectRole() {
            ConsoleUtil.showEntitiesTable(roles, "Доступные роли");
            int roleChoice = InputHandler.getIntInput("Введите номер роли: ");
            return roles.get(roleChoice - 1);
        }

        private void validateSelfDismiss(User user) {
            if (userService.getCurrentUser().equals(user)) {
                LoggerUtil.error("Вы не можете уволить самого себя!");
            }
        }
    }

    private class StatisticController{
        private void viewStatistics() {
            ExceptionHandler.execute(() -> showDateRangeMenu((startDate, endDate) -> {
                budgetController.viewBudgetsByDateRange(startDate, endDate);
                ConsoleUtil.printHeader("Сводка");
                printBudgetSummary(startDate, endDate);
                printPayrollsSummary(startDate, endDate);
            }));
        }

        private void printPayrollsSummary(LocalDate start, LocalDate end) {
            List<Payroll> payrolls = payrollService.getPayrollsByPeriod(start, end);

            long totalPayrolls = payrolls.size();
            long paidPayrolls = payrolls.stream().filter(Payroll::isPaid).count();
            long unpaidPayrolls = totalPayrolls - paidPayrolls;

            ConsoleUtil.printHeader("Статистика зарплат за период " + start + " - " + end);
            ConsoleUtil.println("Всего начислений: " + totalPayrolls);
            ConsoleUtil.println("Выплачено: " + paidPayrolls + " (" + (paidPayrolls * 100 / totalPayrolls) + "%)");
            ConsoleUtil.println("Не выплачено: " + unpaidPayrolls + " (" + (unpaidPayrolls * 100 / totalPayrolls) + "%)");
        }


        private void printBudgetSummary(LocalDate start, LocalDate end) {
            BigDecimal plannedIncome = budgetService.getTotalPlannedIncome(start, end);
            BigDecimal actualIncome = budgetService.getTotalActualIncome(start, end);
            BigDecimal plannedExpenses = budgetService.getTotalPlannedExpenses(start, end);
            BigDecimal actualExpenses = budgetService.getTotalActualExpenses(start, end);

            BigDecimal plannedProfit = plannedIncome.subtract(plannedExpenses);
            BigDecimal actualProfit = actualIncome.subtract(actualExpenses);

            ConsoleUtil.println("Плановый доход: " + plannedIncome);
            ConsoleUtil.println("Плановые расходы: " + plannedExpenses);
            ConsoleUtil.println("Фактический доход: " + actualIncome);
            ConsoleUtil.println("Фактические расходы: " + actualExpenses);
            ConsoleUtil.println("Плановая прибыль: " + plannedProfit);
            ConsoleUtil.println("Фактическая прибыль: " + actualProfit);
        }
    }
}