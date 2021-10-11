package com.eric.projects.hoover.core.mappings;

import com.eric.projects.common.env.EnvConfig;
import com.eric.projects.common.services.Service;
import com.eric.projects.common.services.ServiceDirectory;
import com.eric.projects.hoover.config.HooverProperties;
import com.eric.projects.hoover.config.MappingProperties;
import com.eric.projects.hoover.core.http.HttpClientProvider;
import org.springframework.boot.autoconfigure.web.ServerProperties;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ProgrammaticMappingsProvider extends MappingsProvider {
    protected final EnvConfig envConfig;


    public ProgrammaticMappingsProvider(ServerProperties serverProperties,
                                        HooverProperties hooverProperties,
                                        MappingsValidator mappingsValidator,
                                        HttpClientProvider httpClientProvider,
                                        EnvConfig envConfig) {
        super(serverProperties, hooverProperties, mappingsValidator, httpClientProvider);
        this.envConfig = envConfig;
    }

    @Override
    protected boolean shouldUpdateMappings(HttpServletRequest request) {
        return false;
    }

    @Override
    protected List<MappingProperties> retrieveMappings() {
        List<MappingProperties> mappings = new ArrayList<>();
        Map<String, Service> serviceMap = ServiceDirectory.getMapping();
        for (String key : serviceMap.keySet()) {
            String subDomain = key.toLowerCase();
            Service service = serviceMap.get(key);
            MappingProperties mappingProperties = new MappingProperties();
            mappingProperties.setName(subDomain + "_route");
            mappingProperties.setHost(subDomain + "." + envConfig.getExternalApex());

            String dest = "http://" + service.getBackendDomain();
            mappingProperties.setDestinations(Arrays.asList(dest));
            mappings.add(mappingProperties);
        }
        return mappings;
    }
}
