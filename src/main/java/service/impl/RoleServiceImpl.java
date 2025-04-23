package service.impl;

import dao.impl.RoleDao;
import exception.AuthenticationException;
import exception.AuthorizationException;
import exception.nsee.RoleNotFoundException;
import model.Role;
import service.interfaces.RoleService;
import service.interfaces.UserService;
import util.LoggerUtil;

import java.util.List;
import java.util.function.Supplier;

public class RoleServiceImpl implements RoleService {
    private final RoleDao roleDao = new RoleDao();
    private final UserService userService = new UserServiceImpl();

    private static final String ROLE_DIRECTOR = "Директор";

    @Override
    public List<Role> getAllRoles() {
        checkAdminPermission();
        return findAndValidate(roleDao::findAll, "Роли не найдены");
    }

    @Override
    public Role getRoleById(Long id) {
        checkAuthentication();
        return roleDao.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Роль с ID " + id + " не найдена"));
    }

    @Override
    public Role getRoleByName(String name) {
        checkAuthentication();
        return roleDao.findByName(name)
                .orElseThrow(() -> new RoleNotFoundException("Роль с названием '" + name + "' не найдена"));
    }

    private List<Role> findAndValidate(Supplier<List<Role>> supplier, String errorMessage) {
        List<Role> roles = supplier.get();
        
        if (roles.isEmpty()) {
            LoggerUtil.warn(errorMessage);
            throw new RoleNotFoundException(errorMessage);
        }
        
        LoggerUtil.info("Получено ролей: " + roles.size());
        return roles;
    }

    private void checkAuthentication() {
        if (!userService.isAuthenticated()) {
            throw new AuthenticationException("Пользователь не авторизован");
        }
    }

    private void checkAdminPermission() {
        checkAuthentication();
        if (!userService.hasRole(ROLE_DIRECTOR)) {
            throw new AuthorizationException("Только администратор может управлять ролями");
        }
    }
}