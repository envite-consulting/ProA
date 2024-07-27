package de.envite.proa.entities;

import lombok.Data;

@Data
public class DataStoreConnection {
	private Long id;
	
	private Long processid;
	private Long dataStoreId;
	private DataAccess access;
}
