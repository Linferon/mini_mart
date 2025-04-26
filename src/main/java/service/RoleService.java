package service;

import dao.impl.RoleDao;
import exception.nsee.RoleNotFoundException;
import model.Role;
import util.LoggerUtil;

import java.util.List;
import java.util.function.Supplier;

public class RoleService {
    private static RoleService instance;
    private final RoleDao roleDao = new RoleDao();

    private RoleService() {}

    public static synchronized RoleService getInstance() {
        if (instance == null) {
            instance = new RoleService();
        }
        return instance;
    }
    
    public List<Role> getAllRoles() {
        return findAndValidate(roleDao::findAll);
    }

    private List<Role> findAndValidate(Supplier<List<Role>> supplier) {
        List<Role> roles = supplier.get();
        
        if (roles.isEmpty()) {
            LoggerUtil.warn("Роли не найдены");
            throw new RoleNotFoundException("Роли не найдены");
        }
        
        LoggerUtil.info("Получено ролей: " + roles.size());
        return roles;
    }
}