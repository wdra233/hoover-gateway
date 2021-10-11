package com.eric.projects.hoover.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricsProperties {

    /**
     * Global metrics name prefix
     */
    private String namesPrefix ="hoover";
}
