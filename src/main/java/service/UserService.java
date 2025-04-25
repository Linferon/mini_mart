package service;

import dao.impl.UserDao;
import exception.AuthenticationException;
import exception.nsee.UserNotFoundException;
import model.User;
import util.LoggerUtil;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class UserService {
    private static UserService instance;

    private final UserDao userDao = new UserDao();
    private User currentUser;

    private UserService() {
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public List<User> getActiveUsers() {
        return findAndValidate(userDao::findActiveEmployees, "У вас нет сотрудников в компании!");
    }

    public List<User> getAllUsers() {
        return findAndValidate(userDao::findAll, "Сотрудники не были найдены!");
    }

    public User getUserById(Long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Сотрудник с таким id не был найден!"));
    }

    public Long registerUser(User user) {
        Long id = userDao.save(user);
        LoggerUtil.info("Был зарегистрирован новый сотрудник с id " + id);
        return id;
    }

    public Map<String, Long> getRoleStats() {
        return getAllUsers().stream()
                .collect(Collectors.groupingBy(
                        user -> user.getRole().getName(),
                        Collectors.counting()
                ));
    }

    public void updateUser(User user) {
        Long id = userDao.save(user);
        LoggerUtil.info("Был обновлен сотрудник с id " + id);
    }

    public boolean authenticate(String email, String password) {
        try {
            User user = findUserByEmail(email);

            if (isValidCredentials(user, password)) {
                this.currentUser = user;
                LoggerUtil.info("Пользователь авторизовался: " + user.getName() + " " + user.getSurname());
                return true;
            }

            LoggerUtil.warn("Неудачная попытка авторизации с email: " + email);
            return false;
        } catch (Exception e) {
            LoggerUtil.error("Ошибка при авторизации", e);
            return false;
        }
    }

    public User getCurrentUser() {
        if (currentUser == null) {
            throw new AuthenticationException("Пользователь не авторизован");
        }
        return currentUser;
    }

    public void logout() {
        if (currentUser != null) {
            LoggerUtil.info("Пользователь вышел из системы: " + currentUser.getName() + " " + currentUser.getSurname());
        } else {
            LoggerUtil.info("Пользователь вышел из системы: ");
        }
        this.currentUser = null;
    }

    private List<User> findAndValidate(Supplier<List<User>> supplier, String errorMessage) {
        List<User> users = supplier.get();

        if (users.isEmpty()) {
            LoggerUtil.warn(errorMessage);
            throw new UserNotFoundException(errorMessage);
        }

        LoggerUtil.info("Получено сотрудников: " + users.size());
        return users;
    }

    private User findUserByEmail(String email) {
        return userDao.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Не был найден сотрудник с таким email!"));
    }

    private boolean isValidCredentials(User user, String password) {
        return user.getPassword().equals(password) && Boolean.TRUE.equals(user.getEnabled());
    }
}