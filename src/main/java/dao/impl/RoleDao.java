package dao.impl;

import dao.Dao;
import model.Role;
import dao.mapper.RoleMapper;

import java.sql.ResultSet;
import java.util.Optional;
import java.util.function.Function;

public class RoleDao extends Dao<Role> {

    @Override
    protected String getTableName() {
        return "ROLES";
    }

    @Override
    protected Function<ResultSet, Role> getMapper() {
        return RoleMapper::mapRow;
    }

    public Optional<Role> findByName(String name) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE NAME = ?";
        return querySingle(sql, name);
    }

    public Long save(Role role) {
        if (role.getId() == null) {
            String sql = "INSERT INTO " + getTableName() + " (NAME) VALUES (?)";
            Long id = insert(sql, role.getName());
            if (id != null) {
                role.setId(id);
            }
            return id;
        } else {
            boolean updated = update(role);
            return updated ? role.getId() : null;
        }
    }

    public boolean update(Role role) {
        String sql = "UPDATE " + getTableName() + " SET NAME = ? WHERE ID = ?";
        return update(sql, role.getName(), role.getId());
    }
}