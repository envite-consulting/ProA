package de.envite.proa.repository.tables;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class ProjectTable {

    @Id
    @GeneratedValue
    public Long id;

    private String name;
    private String version;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}