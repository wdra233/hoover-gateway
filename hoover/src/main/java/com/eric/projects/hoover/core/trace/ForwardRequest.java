package com.eric.projects.hoover.core.trace;

public class ForwardRequest extends IncomingRequest {

    protected String mappingName;
    protected byte[] body;

    public String getMappingName() {
        return mappingName;
    }

    public void setMappingName(String mappingName) {
        this.mappingName = mappingName;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
