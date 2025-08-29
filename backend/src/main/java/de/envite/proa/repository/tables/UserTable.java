package de.envite.proa.repository.tables;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import de.envite.proa.entities.authentication.Role;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

	@OneToOne(cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "user")
	private SettingsTable settings;

	private Integer failedLoginAttempts = 0;
	
	@OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "user")
	private Set<ProjectUserRelationTable> userRelations = new HashSet<>();
}