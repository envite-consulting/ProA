package de.envite.proa.repository.tables;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ProcessLevelTable {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private Long relatedProcessModelId;

    @Column
    private String processName;

    @Column
    private Integer level;

    @ManyToOne
    private ProcessModelTable processModel;
}
