package service;

import dao.impl.RoleDao;
import model.Role;

import java.util.List;

import static util.EntityUtil.findAndValidate;
public class RoleService {
    private static RoleService instance;
    private final RoleDao roleDao;

    private RoleService() {
        roleDao = new RoleDao();
    }

    public static synchronized RoleService getInstance() {
        if (instance == null) {
            instance = new RoleService();
        }
        return instance;
    }
    
    public List<Role> getAllRoles() {
        return findAndValidate(roleDao::findAll);
    }
}