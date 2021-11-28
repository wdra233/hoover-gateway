package com.eric.projects.hoover.core.trace;

import com.eric.projects.hoover.core.utils.BodyConverter;
import org.springframework.http.HttpStatus;

public class ReceivedResponse extends HttpEntity {

    protected HttpStatus status;
    protected byte[] body;

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public String getBodyAsString() {
        return BodyConverter.convertBodyToString(body);
    }
}
