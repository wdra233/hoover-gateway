package com.eric.projects.hoover.core.http;


import com.eric.projects.hoover.config.MappingProperties;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class HttpClientProvider {

    protected Map<String, RestTemplate> httpClients = new HashMap<>();

    public void updateHttpClients(List<MappingProperties> mappings) {
        httpClients = mappings.stream().collect(toMap(MappingProperties::getName, this::createRestTemplate));
    }

    public RestTemplate getHttpClient(String mappingName) {
        return httpClients.get(mappingName);
    }

    protected RestTemplate createRestTemplate(MappingProperties mapping) {
        CloseableHttpClient client = createHttpClient().build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(client);
        requestFactory.setConnectTimeout(mapping.getTimeout().getConnect());
        requestFactory.setReadTimeout(mapping.getTimeout().getRead());
        return new RestTemplate(requestFactory);
    }

    protected HttpClientBuilder createHttpClient() {
        return HttpClientBuilder.create().useSystemProperties().disableRedirectHandling().disableCookieManagement();
    }




}
