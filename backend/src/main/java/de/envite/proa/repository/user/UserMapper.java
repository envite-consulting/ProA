package de.envite.proa.repository.user;

import de.envite.proa.entities.authentication.Role;
import de.envite.proa.entities.authentication.User;
import de.envite.proa.repository.tables.UserTable;

import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {
	public static UserTable map(User user) {
		UserTable table = new UserTable();
		table.setEmail(user.getEmail());
		table.setFirstName(user.getFirstName());
		table.setLastName(user.getLastName());
		table.setPassword(user.getPassword());
		table.setCreatedAt(user.getCreatedAt());
		table.setModifiedAt(user.getModifiedAt());
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

	public static Set<User> map(Set<UserTable> tables) {
		return tables.stream().map(UserMapper::map).collect(Collectors.toSet());
	}

	public static User map(UserTable table) {
		User user = new User();
		user.setId(table.getId());
		user.setEmail(table.getEmail());
		user.setFirstName(table.getFirstName());
		user.setLastName(table.getLastName());
		user.setCreatedAt(table.getCreatedAt());
		user.setModifiedAt(table.getModifiedAt());

		user.setRole(table.getRole() == Role.Admin ? "Admin" : "User");

		return user;
	}
}
