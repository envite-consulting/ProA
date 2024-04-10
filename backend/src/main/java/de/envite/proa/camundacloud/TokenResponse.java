package de.envite.proa.camundacloud;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class TokenResponse {
	
	@JsonProperty("access_token")
	private String token;

}
