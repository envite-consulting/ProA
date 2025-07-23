package de.envite.proa.dto;

import lombok.Data;

import java.util.List;

@Data
public class RelatedProcessModelRequest {
	private List<Long> relatedProcessModelIds;
}