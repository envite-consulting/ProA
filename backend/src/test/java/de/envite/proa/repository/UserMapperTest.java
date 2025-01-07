package de.envite.proa.repository;

import de.envite.proa.entities.Role;
import de.envite.proa.entities.User;
import de.envite.proa.repository.tables.UserTable;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "test@example.com";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String PASSWORD = "password123";
    private static final String ROLE_ADMIN = "Admin";
    private static final String ROLE_USER = "User";
    private static final String UNKNOWN_ROLE = "UnknownRole";
    private static final Role ROLE_ADMIN_ENUM = Role.Admin;
    private static final Role ROLE_USER_ENUM = Role.User;
    private static final LocalDateTime CREATED_AT = LocalDateTime.now();
    private static final LocalDateTime MODIFIED_AT = LocalDateTime.now();

    @Test
    void testClassInitialization() {
        UserMapper mapper = new UserMapper();
        assertNotNull(mapper);
    }

    @Test
    void testMapUserToUserTable_AdminRole() {
        User user = new User();
        user.setEmail(EMAIL);
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setPassword(PASSWORD);
        user.setRole(ROLE_ADMIN);

        UserTable table = UserMapper.map(user);

        assertNotNull(table);
        assertEquals(EMAIL, table.getEmail());
        assertEquals(FIRST_NAME, table.getFirstName());
        assertEquals(LAST_NAME, table.getLastName());
        assertEquals(PASSWORD, table.getPassword());
        assertEquals(ROLE_ADMIN_ENUM, table.getRole());
    }

    @Test
    void testMapUserToUserTable_UserRole() {
        User user = new User();
        user.setEmail(EMAIL);
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setPassword(PASSWORD);
        user.setRole(ROLE_USER);

        UserTable table = UserMapper.map(user);

        assertNotNull(table);
        assertEquals(EMAIL, table.getEmail());
        assertEquals(FIRST_NAME, table.getFirstName());
        assertEquals(LAST_NAME, table.getLastName());
        assertEquals(PASSWORD, table.getPassword());
        assertEquals(ROLE_USER_ENUM, table.getRole());
    }

    @Test
    void testMapUserToUserTable_UnknownRole() {
        User user = new User();
        user.setRole(UNKNOWN_ROLE);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> UserMapper.map(user));
        assertEquals("Unknown role: " + UNKNOWN_ROLE, exception.getMessage());
    }

    @Test
    void testMapUserTableToUser_AdminRole() {
        UserTable table = new UserTable();
        table.setId(USER_ID);
        table.setEmail(EMAIL);
        table.setFirstName(FIRST_NAME);
        table.setLastName(LAST_NAME);
        table.setCreatedAt(CREATED_AT);
        table.setModifiedAt(MODIFIED_AT);
        table.setRole(ROLE_ADMIN_ENUM);

        User user = UserMapper.map(table);

        assertNotNull(user);
        assertEquals(USER_ID, user.getId());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(FIRST_NAME, user.getFirstName());
        assertEquals(LAST_NAME, user.getLastName());
        assertEquals(CREATED_AT, user.getCreatedAt());
        assertEquals(MODIFIED_AT, user.getModifiedAt());
        assertEquals(ROLE_ADMIN, user.getRole());
    }

    @Test
    void testMapUserTableToUser_UserRole() {
        UserTable table = new UserTable();
        table.setId(USER_ID);
        table.setEmail(EMAIL);
        table.setFirstName(FIRST_NAME);
        table.setLastName(LAST_NAME);
        table.setCreatedAt(CREATED_AT);
        table.setModifiedAt(MODIFIED_AT);
        table.setRole(ROLE_USER_ENUM);

        User user = UserMapper.map(table);

        assertNotNull(user);
        assertEquals(USER_ID, user.getId());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(FIRST_NAME, user.getFirstName());
        assertEquals(LAST_NAME, user.getLastName());
        assertEquals(CREATED_AT, user.getCreatedAt());
        assertEquals(MODIFIED_AT, user.getModifiedAt());
        assertEquals(ROLE_USER, user.getRole());
    }
}
