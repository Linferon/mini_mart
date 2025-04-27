package controller;

import service.UserService;
import util.ConsoleUtil;
import util.InputHandler;

public class AuthController {
    private final UserService userService;

    public AuthController() {
        this.userService = UserService.getInstance();
    }

    public boolean authenticate() {
        ConsoleUtil.printHeader("Система Авторизации");

        String email = InputHandler.getStringInput("Введите email: ");
        String password = InputHandler.getStringInput("Введите пароль: ");

        return userService.authenticate(email, password);
    }

    public void logout() {
        userService.logout();
        ConsoleUtil.println("Вы успешно вышли из системы.");
    }
}