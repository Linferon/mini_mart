package controller;

import model.MonthlyBudget;
import model.Payroll;
import model.Role;
import model.User;
import service.*;
import util.ConsoleUtil;
import exception.handler.ExceptionHandler;
import util.InputHandler;
import util.LoggerUtil;
import util.TableFormatter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


public class DirectorController extends BaseController {
    private final UserService userService = UserService.getInstance();
    private final MonthlyBudgetService budgetService = MonthlyBudgetService.getInstance();
    private final PayrollService payrollService = PayrollService.getInstance();
    private static final List<Role> roles = RoleService.getInstance().getAllRoles();

    @Override
    public void showMenu() {
        createMenu("Меню Директора")
                .addMenuItem("Назначить бюджет на месяц", this::setBudget)
                .addMenuItem("Управление сотрудниками", this::manageEmployees)
                .addMenuItem("Просмотр статистики", this::showStatistics)
                .addExitItem("Выйти из системы")
                .show();
    }

    private void manageEmployees() {
        createMenu("Управление сотрудниками")
                .addMenuItem("Показать всех сотрудников", this::showAllEmployees)
                .addMenuItem("Зарегистрировать нового сотрудника", this::registerNewEmployee)
                .addMenuItem("Изменить данные сотрудника", this::updateEmployee)
                .addMenuItem("Уволить сотрудника или восстановить сотрудника", this::toggleEmployeeStatus)
                .addExitItem("Назад")
                .show();
    }

    private void toggleEmployeeStatus() {
        ExceptionHandler.execute(() -> {

            List<User> users = userService.getAllUsers();
            ConsoleUtil.printHeader("Все сотрудники");
            ConsoleUtil.println(TableFormatter.formatTable(users));

            Long userId = InputHandler.getLongInput("Введите ID сотрудника: ");
            User user = userService.getUserById(userId);

            if (userService.getCurrentUser().equals(user)) {
                LoggerUtil.error("Вы не можете уволить самого себя!");
            }

            user.setEnabled(!user.getEnabled());
            userService.updateUser(user);

            String action = Boolean.TRUE.equals(user.getEnabled()) ? "восстановлен" : "уволен";
            showSuccess("Сотрудник успешно " + action + ".");
        });
    }

    private void showAllEmployees() {
        ExceptionHandler.execute(() -> {
            List<User> users = userService.getActiveUsers();
            Map<String, Long> roleStats = userService.getRoleStats();

            ConsoleUtil.printHeader("Активные сотрудники");
            ConsoleUtil.println(TableFormatter.formatTable(users));

            ConsoleUtil.printHeader("Распределение сотрудников по ролям");
            roleStats.forEach((role, count) ->
                    ConsoleUtil.println(role + ": " + count + " чел.")
            );
        });
    }

    private void setBudget() {
        ExceptionHandler.execute(() -> {
            List<MonthlyBudget> currentBudgets = budgetService.getAllBudgets();

            ConsoleUtil.printHeader("Текущие бюджеты");
            ConsoleUtil.println(TableFormatter.formatTable(currentBudgets));

            BigDecimal plannedIncome = InputHandler.getBigDecimalInput("Введите сумму планируемого дохода: ");
            BigDecimal plannedExpenses = InputHandler.getBigDecimalInput("Введите сумму планируемых расходов: ");

            LocalDate budgetDate;
            while (true) {
                int year = InputHandler.getIntInput("Введите год (например, 2025): ");
                int month = InputHandler.getIntInput("Введите месяц (1-12): ");

                try {
                    budgetDate = LocalDate.of(year, month, 1);
                    LocalDate now = LocalDate.now().withDayOfMonth(1);

                    if (budgetDate.isBefore(now)) {
                        LoggerUtil.error("Нельзя установить бюджет на прошедший месяц.");
                    } else {
                        break;
                    }
                } catch (Exception e) {
                    LoggerUtil.error("Неверный ввод даты. Попробуйте снова.");
                }
            }

            budgetService.createBudget(budgetDate, plannedIncome, plannedExpenses);
            showSuccess("Бюджет успешно установлен.");

            BigDecimal netResult = plannedIncome.subtract(plannedExpenses);
            ConsoleUtil.println("Прогнозируемый чистый результат: " + netResult);

            if (netResult.compareTo(BigDecimal.ZERO) < 0) {
                ConsoleUtil.println("Внимание: прогнозируется убыток!");
            }
        });
    }


    private void registerNewEmployee() {
        ExceptionHandler.execute(() -> {
            String name = InputHandler.getStringInput("Введите имя сотрудника: ");
            String surname = InputHandler.getStringInput("Введите фамилию сотрудника: ");
            String email = InputHandler.getStringInput("Введите email: ");
            String password = InputHandler.getStringInput("Введите пароль: ");

            ConsoleUtil.printHeader("Доступные роли");

            ConsoleUtil.println(TableFormatter.formatTable(roles));

            int roleChoice = InputHandler.getIntInput("Введите номер роли: ");
            Role selectedRole = roles.get(roleChoice - 1);

            User newUser = new User(null, name, surname, email, password, true, selectedRole, null, null);
            userService.registerUser(newUser);
            showSuccess("Сотрудник успешно зарегистрирован.");
        });
    }

    private void updateEmployee() {
        ExceptionHandler.execute(() -> {
            showAllEmployees();
            Long userId = InputHandler.getLongInput("Введите ID сотрудника для обновления: ");
            User existingUser = userService.getUserById(userId);

            ConsoleUtil.printHeader("Текущие данные сотрудника");
            ConsoleUtil.println(existingUser.toString());
            ConsoleUtil.printDivider();

            InputHandler.inputIfPresent("Введите новое имя", existingUser::setName);
            InputHandler.inputIfPresent("Введите новую фамилию", existingUser::setSurname);
            InputHandler.inputIfPresent("Введите новый email", existingUser::setEmail);

            if (!userService.getCurrentUser().equals(existingUser)) {
                String changeRole = InputHandler.getStringInput("Хотите изменить роль? (да/любое слово): ");

                if (changeRole.equalsIgnoreCase("да")) {
                    ConsoleUtil.printHeader("Доступные роли");
                    ConsoleUtil.println(TableFormatter.formatTable(roles));

                    int roleChoice = InputHandler.getIntInput("Введите номер роли: ");
                    Role selectedRole = roles.get(roleChoice - 1);
                    existingUser.setRole(selectedRole);
                }
            }

            userService.updateUser(existingUser);
            showSuccess("Данные сотрудника обновлены.");
        });
    }

    private void showStatistics() {
        ExceptionHandler.execute(() -> showDateRangeMenu((startDate, endDate) -> {
            List<MonthlyBudget> budgets = budgetService.getBudgetsByDateRange(startDate, endDate);

            ConsoleUtil.printHeader("Статистика бюджета за период " + startDate + " - " + endDate);
            ConsoleUtil.println(TableFormatter.formatTable(budgets));

            ConsoleUtil.printHeader("Сводка");
            printBudgetSummary(startDate, endDate);
            printPayrollsSummary(startDate, endDate);
        }));
    }

    private void printPayrollsSummary(LocalDate start, LocalDate end){
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