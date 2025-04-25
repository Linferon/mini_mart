package dao.impl;

import dao.Dao;
import model.Role;
import dao.mapper.RoleMapper;

import java.sql.ResultSet;
import java.util.function.Function;

import static dao.DbConstants.ROLE_TABLE;
public class RoleDao extends Dao<Role> {

    @Override
    protected String getTableName() {
        return ROLE_TABLE;
    }

    @Override
    protected Function<ResultSet, Role> getMapper() {
        return RoleMapper::mapRow;
    }
}