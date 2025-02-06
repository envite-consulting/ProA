package de.envite.proa.repository;

import de.envite.proa.entities.authentication.User;
import de.envite.proa.repository.tables.UserTable;
import de.envite.proa.repository.user.UserDao;
import de.envite.proa.repository.user.UserMapper;
import de.envite.proa.repository.user.UserRepositoryImpl;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRepositoryImplTest {

	private static final Long USER_ID = 1L;
	private static final String USER_EMAIL = "test@example.com";
	private static final String OLD_EMAIL = "old@example.com";
	private static final String OLD_FIRST_NAME = "OldFirstName";
	private static final String OLD_LAST_NAME = "OldLastName";
	private static final String OLD_PASSWORD = "oldpassword";
	private static final String UPDATED_EMAIL = "updated@example.com";
	private static final String UPDATED_FIRST_NAME = "UpdatedFirstName";
	private static final String UPDATED_LAST_NAME = "UpdatedLastName";
	private static final String UPDATED_PASSWORD = "newpassword";
	private static final String HASHED_PASSWORD = "hashedpassword";

	@InjectMocks
	private UserRepositoryImpl userRepository;

	@Mock
	private UserDao userDao;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testFindByEmail_UserExists() {
		UserTable userTable = new UserTable();
		userTable.setEmail(USER_EMAIL);
		when(userDao.findByEmail(USER_EMAIL)).thenReturn(userTable);

		User user = userRepository.findByEmail(USER_EMAIL);

		assertNotNull(user);
		assertEquals(USER_EMAIL, user.getEmail());
		verify(userDao).findByEmail(USER_EMAIL);
	}

	@Test
	void testFindByEmail_UserDoesNotExist() {
		when(userDao.findByEmail(USER_EMAIL)).thenReturn(null);

		User user = userRepository.findByEmail(USER_EMAIL);

		assertNull(user);
		verify(userDao).findByEmail(USER_EMAIL);
	}

	@Test
	void testFindById_UserExists() {
		UserTable userTable = new UserTable();
		userTable.setId(USER_ID);
		when(userDao.findById(USER_ID)).thenReturn(userTable);

		User user = userRepository.findById(USER_ID);

		assertNotNull(user);
		assertEquals(USER_ID, user.getId());
		verify(userDao).findById(USER_ID);
	}

	@Test
	void testFindById_UserDoesNotExist() {
		when(userDao.findById(USER_ID)).thenReturn(null);

		assertThrows(NotFoundException.class, () -> userRepository.findById(USER_ID));
		verify(userDao).findById(USER_ID);
	}

	@Test
	void testPatchUser() {
		User user = new User();
		user.setEmail(UPDATED_EMAIL);
		user.setFirstName(UPDATED_FIRST_NAME);
		user.setLastName(UPDATED_LAST_NAME);
		user.setPassword(UPDATED_PASSWORD);

		UserTable existingUser = new UserTable();
		existingUser.setId(USER_ID);
		existingUser.setEmail(OLD_EMAIL);
		existingUser.setFirstName(OLD_FIRST_NAME);
		existingUser.setLastName(OLD_LAST_NAME);
		existingUser.setPassword(OLD_PASSWORD);

		UserTable updatedUser = new UserTable();
		updatedUser.setId(USER_ID);
		updatedUser.setEmail(user.getEmail());
		updatedUser.setFirstName(user.getFirstName());
		updatedUser.setLastName(user.getLastName());
		updatedUser.setPassword(HASHED_PASSWORD);
		updatedUser.setModifiedAt(LocalDateTime.now());

		when(userDao.findById(USER_ID)).thenReturn(existingUser);
		when(userDao.patchUser(any(UserTable.class))).thenReturn(updatedUser);

		User patchedUser = userRepository.patchUser(USER_ID, user);

		assertNotNull(patchedUser);
		assertEquals(user.getEmail(), patchedUser.getEmail());
		assertEquals(user.getFirstName(), patchedUser.getFirstName());
		assertEquals(user.getLastName(), patchedUser.getLastName());

		verify(userDao).findById(USER_ID);
		verify(userDao).patchUser(any(UserTable.class));
	}

	@Test
	void testPatchUser_NoFieldsToUpdate() {
		User user = new User();

		UserTable existingUser = new UserTable();
		existingUser.setId(USER_ID);
		existingUser.setEmail(OLD_EMAIL);
		existingUser.setFirstName(OLD_FIRST_NAME);
		existingUser.setLastName(OLD_LAST_NAME);
		existingUser.setPassword(OLD_PASSWORD);

		UserTable updatedUser = new UserTable();
		updatedUser.setId(USER_ID);
		updatedUser.setEmail(existingUser.getEmail());
		updatedUser.setFirstName(existingUser.getFirstName());
		updatedUser.setLastName(existingUser.getLastName());
		updatedUser.setPassword(existingUser.getPassword());
		updatedUser.setModifiedAt(LocalDateTime.now());

		when(userDao.findById(USER_ID)).thenReturn(existingUser);
		when(userDao.patchUser(any(UserTable.class))).thenReturn(updatedUser);

		User patchedUser = userRepository.patchUser(USER_ID, user);

		assertNotNull(patchedUser);
		assertEquals(existingUser.getEmail(), patchedUser.getEmail());
		assertEquals(existingUser.getFirstName(), patchedUser.getFirstName());
		assertEquals(existingUser.getLastName(), patchedUser.getLastName());

		verify(userDao).findById(USER_ID);
		verify(userDao).patchUser(any(UserTable.class));
	}

	@Test
	void testDeleteById() {
		doNothing().when(userDao).deleteById(USER_ID);

		userRepository.deleteById(USER_ID);

		verify(userDao).deleteById(USER_ID);
	}

	@Test
	void testGetAllUsers() {
		UserTable userTable1 = new UserTable();
		userTable1.setEmail(USER_EMAIL);
		UserTable userTable2 = new UserTable();
		userTable2.setEmail(OLD_EMAIL);

		when(userDao.getAllUsers()).thenReturn(List.of(userTable1, userTable2));

		List<User> expectedUsers = List.of(UserMapper.map(userTable1), UserMapper.map(userTable2));

		List<User> users = userRepository.getAllUsers();

		assertEquals(expectedUsers, users);
		verify(userDao).getAllUsers();
	}
}
