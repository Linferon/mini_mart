package service.impl;

import dao.impl.UserDao;
import exception.AuthenticationException;
import exception.AuthorizationException;
import exception.nsee.UserNotFoundException;
import model.User;
import service.UserService;
import util.LoggerUtil;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class UserServiceImpl implements UserService {
    private final UserDao userDao = new UserDao();
    private User currentUser;

    public static final String ROLE_DIRECTOR = "Директор";
    public static final String ROLE_ACCOUNTANT = "Бухгалтер";
    public static final String ROLE_CASHIER = "Кассир";
    public static final String ROLE_STOCK_KEEPER = "Кладовщик";

    @Override
    public List<User> getAllUsers() {
        checkPermission(ROLE_DIRECTOR);
        return findAndValidate(userDao::findAll, "Сотрудники не были найдены!");
    }

    @Override
    public List<User> getUsersByName(String name) {
        checkPermission(ROLE_DIRECTOR, ROLE_ACCOUNTANT);
        return findAndValidate(() -> userDao.findByName(name), "Сотрудники с таким именем не были найдены!");
    }

    @Override
    public User getUserById(Long id) {
        if (currentUser != null && currentUser.getId().equals(id)) {
            return userDao.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("Сотрудник с таким id не был найден!"));
        }

        checkPermission(ROLE_DIRECTOR, ROLE_ACCOUNTANT);
        return userDao.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Сотрудник с таким id не был найден!"));
    }

    @Override
    public Long registerUser(User user) {
        checkPermission(ROLE_DIRECTOR);
        Long id = userDao.save(user);
        LoggerUtil.info("Был зарегистрирован новый сотрудник с id " + id);
        return id;
    }

    @Override
    public List<User> getUsersByRoleId(Long roleId) {
        checkPermission(ROLE_DIRECTOR);
        return findAndValidate(() -> userDao.findByRole(roleId),
                "Сотрудники с такой ролью не были найдены!");
    }

    @Override
    public User getUserByEmail(String email) {
        if (currentUser != null && currentUser.getEmail().equals(email)) {
            return userDao.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("Сотрудник с таким email не был найден!"));
        }

        checkPermission(ROLE_DIRECTOR, ROLE_ACCOUNTANT);
        return userDao.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Сотрудник с таким email не был найден!"));
    }

    @Override
    public Long updateUser(User user) {
        if (currentUser != null && currentUser.getId().equals(user.getId())) {
            if (!currentUser.getRole().getId().equals(user.getRole().getId())) {
                throw new AuthorizationException("Нельзя изменить свою роль");
            }

            Long id = userDao.save(user);
            LoggerUtil.info("Пользователь обновил свою информацию, id: " + id);
            return id;
        }

        checkPermission( ROLE_DIRECTOR);
        Long id = userDao.save(user);
        LoggerUtil.info("Был обновлен сотрудник с id " + id);
        return id;
    }

    @Override
    public void deleteUserById(Long id) {
        if (currentUser != null && currentUser.getId().equals(id)) {
            throw new AuthorizationException("Невозможно удалить собственную учетную запись");
        }

        checkPermission( ROLE_DIRECTOR);
        userDao.deleteById(id);
        LoggerUtil.info("Был удален сотрудник с id: " + id);
    }

    @Override
    public boolean authenticate(String email, String password) {
        try {
            Optional<User> userOpt = userDao.findByEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                if (user.getPassword().equals(password)) {
                    this.currentUser = user;
                    LoggerUtil.info("Пользователь авторизовался: " + user.getName() + " " + user.getSurname());
                    return true;
                }
            }

            LoggerUtil.warn("Неудачная попытка авторизации с email: " + email);
            return false;
        } catch (Exception e) {
            LoggerUtil.error("Ошибка при авторизации", e);
            return false;
        }
    }

    @Override
    public User getCurrentUser() {
        if (currentUser == null) {
            throw new AuthenticationException("Пользователь не авторизован");
        }
        return currentUser;
    }

    @Override
    public void logout() {
        LoggerUtil.info("Пользователь вышел из системы: " +
                (currentUser != null ? (currentUser.getName() + " " + currentUser.getSurname()) : ""));
        this.currentUser = null;
    }

    @Override
    public boolean isAuthenticated() {
        return currentUser != null;
    }

    @Override
    public boolean hasRole(String... roleNames) {
        if (currentUser == null || currentUser.getRole() == null) {
            return false;
        }

        String userRoleName = currentUser.getRole().getName();
        for (String roleName : roleNames) {
            if (roleName.equals(userRoleName)) {
                return true;
            }
        }

        return false;
    }

    private void checkPermission(String... allowedRoles) {
        if (currentUser == null) {
            throw new AuthenticationException("Пользователь не авторизован");
        }

        if (!hasRole(allowedRoles)) {
            throw new AuthorizationException("Недостаточно прав для выполнения операции");
        }
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
}