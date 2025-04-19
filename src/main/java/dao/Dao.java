package dao;

import java.util.List;
import java.util.Optional;

public interface Dao<T> {
    Optional<T> findById(Long id);
    List<T> findAll();
    Long save(T entity);
    void update(T entity);
    void delete(Long id);
}