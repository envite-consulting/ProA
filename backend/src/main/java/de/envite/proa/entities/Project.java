package de.envite.proa.entities;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Project {

    private Long id;
    private String name;
    private String version;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}