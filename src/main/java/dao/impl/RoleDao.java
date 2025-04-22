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
}