package com.eric.projects.hoover.core.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;

import static com.eric.projects.hoover.core.utils.BodyConverter.convertStringToBody;

public class RequestData extends UnmodifiableRequestData {
    private boolean needRedirect;
    private String redirectUrl;

    public RequestData(HttpMethod method,
                       String uri,
                       String host,
                       HttpHeaders headers,
                       byte[] body,
                       HttpServletRequest originRequest) {
        super(method, uri, host, headers, body, originRequest);
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void setBody(String body) {
        this.body = convertStringToBody(body);
    }

    public void setNeedRedirect(boolean needRedirect) {
        this.needRedirect = needRedirect;
    }

    public boolean isNeedRedirect() {
        return this.needRedirect;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getRedirectUrl() {
        return this.redirectUrl;
    }


}
