package com.eric.projects.hoover.core.interceptor;

import com.eric.projects.hoover.config.MappingProperties;
import com.eric.projects.hoover.core.http.RequestData;

public class NoOpPreForwardRequestInterceptor implements PreForwardRequestInterceptor {
    @Override
    public void intercept(RequestData data, MappingProperties mapping) {

    }
}
