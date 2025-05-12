package dao.impl;

import dao.Dao;
import dao.mapper.UserMapper;
import model.User;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static dao.DbConstants.*;

public class UserDao extends Dao<User> {
    @Override
    protected String getTableName() {
        return USER_TABLE;
    }

    @Override
    protected Function<ResultSet, User> getMapper() {
        return UserMapper::mapRow;
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT u.*, r.NAME as ROLE_NAME " +
                "FROM " + USER_TABLE + " u " +
                "LEFT JOIN " + ROLE_TABLE + " r ON u.ROLE_ID = r.ID " +
                "WHERE u.ID = ?";
        return querySingle(sql, id);
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT u.*, r.NAME as ROLE_NAME " +
                "FROM " + USER_TABLE + " u " +
                "LEFT JOIN " + ROLE_TABLE + " r ON u.ROLE_ID = r.ID";
        return queryList(sql);
    }

    public List<User> findActiveEmployees(){
        String sql = "SELECT u.*, r.NAME as ROLE_NAME " +
                "FROM " + USER_TABLE + " u " +
                "LEFT JOIN " + ROLE_TABLE + " r ON u.ROLE_ID = r.ID" +
                " WHERE u.ENABLED = TRUE";
        return queryList(sql);
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT u.*, r.NAME as ROLE_NAME " +
                "FROM " + USER_TABLE + " u " +
                "LEFT JOIN " + ROLE_TABLE + " r ON u.ROLE_ID = r.ID " +
                "WHERE u.EMAIL = ?";
        return querySingle(sql, email);
    }

    public Long save(User user) {
        if (user.getId() == null) {
            Timestamp now = new Timestamp(System.currentTimeMillis());

            String sql = "INSERT INTO " + USER_TABLE +
                    " (NAME, SURNAME, EMAIL, PASSWORD, ROLE_ID, CREATED_AT, UPDATED_AT) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            Long id = insert(sql,
                    user.getName(),
                    user.getSurname(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getRole().id(),
                    user.getCreatedAt() != null ? user.getCreatedAt() : now,
                    user.getUpdatedAt() != null ? user.getUpdatedAt() : now);

            if (id != null) {
                user.setId(id);
            }
            return id;
        } else {
            boolean updated = update(user);
            return updated ? user.getId() : null;
        }
    }

    public boolean update(User user) {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String sql = "UPDATE " + USER_TABLE +
                " SET NAME = ?, SURNAME = ?, EMAIL = ?, " +
                "PASSWORD = ?, ENABLED = ?,  ROLE_ID = ?, UPDATED_AT = ? " +
                "WHERE ID = ?";

        return update(sql,
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getPassword(),
                user.getEnabled(),
                user.getRole().id(),
                now,
                user.getId());
    }
}