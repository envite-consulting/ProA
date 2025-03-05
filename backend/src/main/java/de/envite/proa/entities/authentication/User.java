package de.envite.proa.entities.authentication;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class User {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private String role;
}