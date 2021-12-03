package com.eric.projects.hoover.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppProperties {
    private String signingSecret;
}
