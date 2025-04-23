package service.interfaces;

import model.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();

    List<User> getUsersByName(String name);

    User getUserById(Long id);

    Long registerUser(User user);

    List<User> getUsersByRoleId(Long roleId);

    User getUserByEmail(String email);

    Long updateUser(User user);

    void deleteUserById(Long id);

    boolean authenticate(String email, String password);

    User getCurrentUser();

    void logout();

    boolean isAuthenticated();

    boolean hasRole(String... roleNames);
}
