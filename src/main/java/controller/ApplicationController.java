package controller;

import model.Role;
import model.User;
import service.UserService;
import migration.LiquibaseMigrator;
import util.DatabaseConnection;

import static util.ConsoleUtil.printHeader;
import static util.ConsoleUtil.println;
import static util.InputHandler.closeScanner;
import static util.InputHandler.getStringInput;
import static util.LoggerUtil.*;

public class ApplicationController {
    private final UserService userService;
    private final AuthController authUI;

    public ApplicationController() {
        this.userService = UserService.getInstance();
        this.authUI = new AuthController();
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

            User currentUser = userService.getCurrentUser();
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
            error("Ошибка авторизации. Попробуйте снова.");
            return false;
        }
        return true;
    }

    private void displayWelcomeMessage(User user) {
        printHeader("Добро пожаловать, " +
                user.getName() + " " + user.getSurname() +
                " (" + user.getRole().name() + ")");
    }

    private void processUserInterface(User user) {
        BaseController ui = getUIForRole(user.getRole());

        assert ui != null;
        ui.showMenu();
    }

    private BaseController getUIForRole(Role role) {
        String roleName = role.name();

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

    private boolean shouldExitApplication() {
        while (true) {
            String input = getStringInput("\nЖелаете продолжить работу? (да/нет): ").trim().toLowerCase();
            if (input.equals("да")) return false;
            if (input.equals("нет")) return true;
        }
    }

    private void displayExitMessage() {
        printHeader("Завершение работы системы");
        println("Спасибо за использование системы управления предприятием!");
    }

    private void handleCriticalError(Exception e) {
        error("Критическая ошибка приложения: " + e.getMessage());
        e.printStackTrace();
    }

    private void cleanupResources() {
        DatabaseConnection.closeConnection();
        closeScanner();
        close();
    }

    private void handleUnknownRole(String roleName) {
        println("Неизвестная роль: " + roleName + ". Доступ запрещен.");
        warn("Попытка доступа с неизвестной ролью: " + roleName);
    }
}