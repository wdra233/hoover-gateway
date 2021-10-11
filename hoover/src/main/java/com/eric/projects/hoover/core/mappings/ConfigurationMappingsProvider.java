package com.eric.projects.hoover.core.mappings;

import com.eric.projects.hoover.config.HooverProperties;
import com.eric.projects.hoover.config.MappingProperties;
import com.eric.projects.hoover.core.http.HttpClientProvider;
import org.springframework.boot.autoconfigure.web.ServerProperties;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationMappingsProvider extends MappingsProvider {


    public ConfigurationMappingsProvider(ServerProperties serverProperties, HooverProperties hooverProperties, MappingsValidator mappingsValidator, HttpClientProvider httpClientProvider) {
        super(serverProperties, hooverProperties, mappingsValidator, httpClientProvider);
    }

    @Override
    protected boolean shouldUpdateMappings(HttpServletRequest request) {
        return false;
    }

    @Override
    protected List<MappingProperties> retrieveMappings() {
        return hooverProperties.getMappings().stream()
                .map(MappingProperties::copy)
                .collect(Collectors.toList());
    }
}
