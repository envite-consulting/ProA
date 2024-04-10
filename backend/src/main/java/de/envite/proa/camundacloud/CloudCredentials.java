package de.envite.proa.camundacloud;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CloudCredentials {

	@JsonProperty("client_id")
	private String clientId;

	@JsonProperty("client_secret")
	private String clientSecret;

	@JsonProperty("grant_type")
	private String grantType = "client_credentials";

	@JsonProperty("audience")
	private String audience = "api.cloud.camunda.io";
}