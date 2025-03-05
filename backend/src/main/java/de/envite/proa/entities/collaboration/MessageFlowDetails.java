package de.envite.proa.entities.collaboration;

import de.envite.proa.entities.process.ProcessElementType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageFlowDetails {

    private String bpmnId;
    private String name;
    private String description;

    private Long callingProcessId;
    private Long calledProcessId;

    private ProcessElementType callingElementType;
    private ProcessElementType calledElementType;
}
