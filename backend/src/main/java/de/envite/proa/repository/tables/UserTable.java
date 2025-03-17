package de.envite.proa.repository.tables;

import de.envite.proa.entities.authentication.Role;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class UserTable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	private String email;
	private String firstName;
	private String lastName;
	private String password;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
	private Role role;

	@OneToMany
	private List<ProjectVersionTable> projects;

	@OneToOne(fetch = FetchType.LAZY)
	private SettingsTable settings;

	private Integer failedLoginAttempts = 0;
}