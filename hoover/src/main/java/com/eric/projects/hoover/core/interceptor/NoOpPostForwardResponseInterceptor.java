package com.eric.projects.hoover.core.interceptor;

import com.eric.projects.hoover.config.MappingProperties;
import com.eric.projects.hoover.core.http.ResponseData;

public class NoOpPostForwardResponseInterceptor implements PostForwardResponseInterceptor {
    @Override
    public void intercept(ResponseData data, MappingProperties mapping) {

    }
}
