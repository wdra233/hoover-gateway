package com.eric.projects.hoover.core.trace;

import org.springframework.http.HttpHeaders;

public abstract class HttpEntity {

    protected HttpHeaders headers;

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }
}
