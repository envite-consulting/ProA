package de.envite.proa.entities.settings;

import lombok.Data;

@Data
public class Settings {
    private String geminiApiKey;
    private String modelerClientId;
    private String modelerClientSecret;
    private String operateClientId;
    private String operateClientSecret;
    private String operateRegionId;
    private String operateClusterId;
}
