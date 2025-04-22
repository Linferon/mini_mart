package dao.impl;

import dao.Dao;
import dao.mapper.UserMapper;
import model.User;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class UserDao extends Dao<User> {
    @Override
    protected String getTableName() {
        return "USERS";
    }

    @Override
    protected Function<ResultSet, User> getMapper() {
        return UserMapper::mapRow;
    }

    public List<User> findByName(String name) {
        String sql = "SELECT * FROM USERS WHERE NAME like ?%";
        return queryList(sql, name);
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE EMAIL = ?";
        return querySingle(sql, email);
    }

    public List<User> findByRole(Long roleId) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE ROLE_ID = ?";
        return queryList(sql, roleId);
    }

    public Long save(User user) {
        if (user.getId() == null) {
            Timestamp now = new Timestamp(System.currentTimeMillis());

            String sql = "INSERT INTO " + getTableName() +
                    " (NAME, SURNAME, EMAIL, PASSWORD, ROLE_ID, CREATED_AT, UPDATED_AT) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            Long id = insert(sql,
                    user.getName(),
                    user.getSurname(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getRole().getId(),
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

        String sql = "UPDATE " + getTableName() +
                " SET NAME = ?, SURNAME = ?, EMAIL = ?, " +
                "PASSWORD = ?, ROLE_ID = ?, UPDATED_AT = ? " +
                "WHERE ID = ?";

        return update(sql,
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getPassword(),
                user.getRole().getId(),
                now,
                user.getId());
    }
}