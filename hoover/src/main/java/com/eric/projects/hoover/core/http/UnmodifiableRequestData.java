package com.eric.projects.hoover.core.http;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;

public class UnmodifiableRequestData {
    protected HttpMethod method;
    protected String uri;
    protected String host;
    protected HttpHeaders headers;
    protected byte[] body;
    protected HttpServletRequest originRequest;
    public UnmodifiableRequestData(RequestData requestData) {
        this(
                requestData.getMethod(),
                requestData.getHost(),
                requestData.getUri(),
                requestData.getHeaders(),
                requestData.getBody(),
                requestData.getOriginRequest()
        );
    }

    public UnmodifiableRequestData(HttpMethod method, String uri, String host, HttpHeaders httpHeaders, byte[] body, HttpServletRequest originRequest) {
        this.method = method;
        this.uri = uri;
        this.host = host;
        this.headers = httpHeaders;
        this.body = body;
        this.originRequest = originRequest;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getHost() {
        return host;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    public HttpServletRequest getOriginRequest() {
        return originRequest;
    }
}
