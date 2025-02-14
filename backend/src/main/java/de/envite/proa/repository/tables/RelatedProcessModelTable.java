package de.envite.proa.repository.tables;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class RelatedProcessModelTable {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private Long relatedProcessModelId;

    @Column
    private String processName;

    @Column
    private Integer level;

    @Column
    private boolean manuallyAdded;

    @ManyToOne
    private ProcessModelTable processModel;
}
