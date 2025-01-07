package de.envite.proa.usecases.user;

import de.envite.proa.entities.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserUsecaseTest {

    private static User user;
    private static final long ID = 1L;
    private static final String EMAIL = "test@test.de";

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserUsecase usecase;

    @BeforeAll
    static void beforeAll() {
        user = new User();
        user.setId(ID);
        user.setEmail(EMAIL);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByEmail() {
        when(repository.findByEmail(EMAIL)).thenReturn(user);

        User result = usecase.findByEmail(EMAIL);

        assertNotNull(result, "findByEmail should return a User object.");
        assertEquals(user, result, "The returned user should match the test user.");
        verify(repository, times(1)).findByEmail(EMAIL);
    }

    @Test
    void testPatchUser() {
        when(repository.patchUser(ID, user)).thenReturn(user);

        User result = usecase.patchUser(ID, user);

        assertNotNull(result, "patchUser should return an updated User object.");
        assertEquals(user, result, "The returned user should match the updated user.");
        verify(repository, times(1)).patchUser(ID, user);
    }

    @Test
    void testFindById() {
        when(repository.findById(ID)).thenReturn(user);

        User result = usecase.findById(ID);

        assertNotNull(result, "findById should return a User object.");
        assertEquals(user, result, "The returned user should match the test user.");
        verify(repository, times(1)).findById(ID);
    }
}
