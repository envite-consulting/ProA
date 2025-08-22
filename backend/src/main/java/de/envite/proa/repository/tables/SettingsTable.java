package de.envite.proa.repository.tables;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SettingsTable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	private String geminiApiKey;
	private String modelerClientId;
	private String modelerClientSecret;
	private String operateClientId;
	private String operateClientSecret;
	private String operateRegionId;
	private String operateClusterId;

	@OneToOne(fetch = FetchType.LAZY)
	private UserTable user;
}