package com.josemaba.marquesitasapi.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(String provider, String publicBaseUrl, Local local) {

    public record Local(String path) {
    }
}
