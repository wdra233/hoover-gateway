package com.eric.projects.hoover.core.interceptor;

import com.eric.projects.hoover.config.MappingProperties;
import com.eric.projects.hoover.core.http.ResponseData;

public interface PostForwardRequestInterceptor {
    void intercept(ResponseData data, MappingProperties mapping);
}
