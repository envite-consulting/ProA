package de.envite.proa.repository;

import de.envite.proa.entities.Role;
import de.envite.proa.entities.User;
import de.envite.proa.repository.tables.UserTable;

public class UserMapper {
    public static UserTable map(User user) {
        UserTable table = new UserTable();
        table.setEmail(user.getEmail());
        table.setFirstName(user.getFirstName());
        table.setLastName(user.getLastName());
        table.setPassword(user.getPassword());
        switch (user.getRole()) {
            case "Admin":
                table.setRole(Role.Admin);
                break;
            case "User":
                table.setRole(Role.User);
                break;
            default:
                throw new IllegalArgumentException("Unknown role: " + user.getRole());
        }
        return table;
    }

    public static User map(UserTable table) {
        User user = new User();
        user.setId(table.getId());
        user.setEmail(table.getEmail());
        user.setFirstName(table.getFirstName());
        user.setLastName(table.getLastName());
        user.setPassword(table.getPassword());
        user.setCreatedAt(table.getCreatedAt());
        user.setModifiedAt(table.getModifiedAt());
        switch (table.getRole()) {
            case Admin:
                user.setRole("Admin");
                break;
            case User:
                user.setRole("User");
                break;
        }
        return user;
    }
}
