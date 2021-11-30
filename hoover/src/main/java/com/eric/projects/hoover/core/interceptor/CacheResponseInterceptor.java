package com.eric.projects.hoover.core.interceptor;

import com.eric.projects.hoover.config.MappingProperties;
import com.eric.projects.hoover.core.http.ResponseData;
import org.springframework.http.HttpHeaders;

import java.util.List;

public class CacheResponseInterceptor implements PostForwardResponseInterceptor {
    @Override
    public void intercept(ResponseData data, MappingProperties mapping) {
        HttpHeaders headers = data.getHeaders();
        if (headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            List<String> values = headers.get(HttpHeaders.CONTENT_TYPE);
            if (values.contains("text/html")) {
                // insert header to prevent caching
                headers.set(HttpHeaders.CACHE_CONTROL, "no-cache");
            }
        }
    }
}
