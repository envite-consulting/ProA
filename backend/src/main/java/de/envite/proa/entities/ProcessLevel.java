package de.envite.proa.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessLevel {

    private Long relatedProcessModelId;
    private String processName;
    private Integer level;
}
