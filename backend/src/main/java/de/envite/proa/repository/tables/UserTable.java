package de.envite.proa.repository.tables;

import java.time.LocalDateTime;
import java.util.List;

import de.envite.proa.entities.authentication.Role;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class UserTable {

    @Id
    @GeneratedValue
    public Long id;

    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Role role;

    @OneToMany
    private List<ProjectTable> projects;

    @OneToOne
    private SettingsTable settings;

    private Integer failedLoginAttempts = 0;
}