package de.envite.proa.entities.process;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelatedProcessModel {

    private Long relatedProcessModelId;
    private String processName;
    private Integer level;
    private boolean manuallyAdded;
}
