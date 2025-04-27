package controller;

import model.Role;
import model.User;
import service.UserService;
import migration.LiquibaseMigrator;
import util.DatabaseConnection;
import util.LoggerUtil;
import util.ConsoleUtil;
import util.InputHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ApplicationController {
    private final UserService userService;
    private final AuthController authUI;
    private final DateTimeFormatter dateTimeFormatter;

    public ApplicationController() {
        this.userService = UserService.getInstance();
        this.authUI = new AuthController();
        this.dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    }

    public void run() {
        try {
            initializeApplication();
            mainApplicationLoop();
        } catch (Exception e) {
            handleCriticalError(e);
        } finally {
            cleanupResources();
        }
    }

    private void initializeApplication() {
        LiquibaseMigrator.migrate();
    }

    private void mainApplicationLoop() {
        while (true) {
            if (!performAuthentication()) {
                continue;
            }

            User currentUser = retrieveCurrentUser();

            if (currentUser == null) {
                continue;
            }

            displayWelcomeMessage(currentUser);
            processUserInterface(currentUser);
            authUI.logout();

            if (shouldExitApplication()) {
                displayExitMessage();
                break;
            }
        }
    }

    private boolean performAuthentication() {
        if (!authUI.authenticate()) {
            LoggerUtil.error("Ошибка авторизации. Попробуйте снова.");
            return false;
        }
        return true;
    }

    private User retrieveCurrentUser() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            LoggerUtil.error("Не удалось получить данные текущего пользователя");
        }
        return currentUser;
    }

    private void displayWelcomeMessage(User user) {
        ConsoleUtil.printHeader("Добро пожаловать, " +
                user.getName() + " " + user.getSurname() +
                " (" + user.getRole().getName() + ")");
    }

    private void processUserInterface(User user) {
        BaseController ui = getUIForRole(user.getRole());
        if (ui != null) {
            ui.showMenu();
        }
    }

    private boolean shouldExitApplication() {
        String continueChoice;
        do {
            continueChoice = InputHandler.getStringInput("\nЖелаете продолжить работу? (да/нет): ");
        } while (!continueChoice.equalsIgnoreCase("да") && !continueChoice.equalsIgnoreCase("нет"));

        return continueChoice.equalsIgnoreCase("нет");
    }

    private void displayExitMessage() {
        ConsoleUtil.printHeader("Завершение работы системы");
        ConsoleUtil.println("Спасибо за использование системы управления предприятием!");
        ConsoleUtil.println("Время завершения: " + LocalDateTime.now().format(dateTimeFormatter));
    }

    private void handleCriticalError(Exception e) {
        LoggerUtil.error("Критическая ошибка приложения: " + e.getMessage());
        e.printStackTrace();
    }

    private void cleanupResources() {
        DatabaseConnection.closeConnection();
        InputHandler.closeScanner();
        LoggerUtil.close();
    }

    private BaseController getUIForRole(Role role) {
        String roleName = role.getName();

        return switch (roleName) {
            case "Директор" -> new DirectorController();
            case "Бухгалтер" -> new AccountantController();
            case "Кладовщик" -> new StockKeeperController();
            case "Кассир" -> new CashierController();
            default -> {
                handleUnknownRole(roleName);
                yield null;
            }
        };
    }

    private void handleUnknownRole(String roleName) {
        ConsoleUtil.println("Неизвестная роль: " + roleName + ". Доступ запрещен.");
        LoggerUtil.warn("Попытка доступа с неизвестной ролью: " + roleName);
    }
}