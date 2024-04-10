package de.envite.proa.camundacloud;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CamundaCloudImportConfiguration {

	@JsonProperty("client_id")
	private String clientId;

	@JsonProperty("client_secret")
	private String clientSecret;

	private String email;
}