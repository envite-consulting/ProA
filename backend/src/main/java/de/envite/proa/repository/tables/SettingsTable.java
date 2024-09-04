package de.envite.proa.repository.tables;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Data
@Entity
public class SettingsTable {

	@Id
	@GeneratedValue
	public Long id;

	private String geminiApiKey;
	private String modelerClientId;
	private String modelerClientSecret;
	private String operateClientId;
	private String operateClientSecret;
	private String operateRegionId;
	private String operateClusterId;

	@OneToOne
	private UserTable user;
}