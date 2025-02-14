package de.envite.proa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RelatedProcessModelRequest {
    private List<Long> relatedProcessModelIds;
}
