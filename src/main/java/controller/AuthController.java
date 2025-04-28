package controller;

import service.UserService;

import static util.ConsoleUtil.printHeader;
import static util.ConsoleUtil.println;
import static util.InputHandler.getStringInput;

public class AuthController {
    private final UserService userService;

    public AuthController() {
        this.userService = UserService.getInstance();
    }

    public boolean authenticate() {
        printHeader("Система Авторизации");

        String email = getStringInput("Введите email: ");
        String password = getStringInput("Введите пароль: ");

        return userService.authenticate(email, password);
    }

    public void logout() {
        userService.logout();
        println("Вы успешно вышли из системы.");
    }
}