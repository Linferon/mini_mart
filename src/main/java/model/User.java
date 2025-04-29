package model;

import util.TableFormatter;

import java.sql.Timestamp;

public class User implements FormattableEntity, TimestampedEntity {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private String password;
    private Boolean enabled;
    private Role role;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    private static final int ID_WIDTH = 5;
    private static final int NAME_WIDTH = 20;
    private static final int SURNAME_WIDTH = 20;
    private static final int EMAIL_WIDTH = 30;
    private static final int ROLE_WIDTH = 15;
    private static final int ENABLED_WIDTH = 10;

    public User(Long id, String name, String surname) {
        this.id = id;
        this.name = name;
        this.surname = surname;
    }

    public User(Long id, String name, String surname, String email, String password, Boolean enabled, Role role, Timestamp createdAt, Timestamp updatedAt) {
        this(id, name, surname);
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Сотрудник " +
                "\nid: " + id +
                "\nимя: " + name +
                "\nфамилия: " + surname +
                "\nemail: " + email +
                "\nроль: " + (role != null ? role.name() : "не указана");
    }

    @Override
    public String getTableHeader() {
        return TableFormatter.formatCell("ID", ID_WIDTH) +
                TableFormatter.formatCell("Имя", NAME_WIDTH) +
                TableFormatter.formatCell("Фамилия", SURNAME_WIDTH) +
                TableFormatter.formatCell("Email", EMAIL_WIDTH) +
                TableFormatter.formatCell("Роль", ROLE_WIDTH) +
                TableFormatter.formatCell("Активен", ENABLED_WIDTH);
    }

    @Override
    public String toTableRow() {
        return TableFormatter.formatCell(id, ID_WIDTH) +
                TableFormatter.formatCell(name, NAME_WIDTH) +
                TableFormatter.formatCell(surname, SURNAME_WIDTH) +
                TableFormatter.formatCell(email, EMAIL_WIDTH) +
                TableFormatter.formatCell(role != null ? role.name() : "-", ROLE_WIDTH) +
                TableFormatter.formatCell(Boolean.TRUE.equals(enabled) ? "Активен" : "Уволен", ENABLED_WIDTH);
    }

    @Override
    public String getTableDivider() {
        return TableFormatter.createDivider(ID_WIDTH, NAME_WIDTH, SURNAME_WIDTH, EMAIL_WIDTH, ROLE_WIDTH);
    }

    public String getFullName() {
        return name + " " + surname;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}