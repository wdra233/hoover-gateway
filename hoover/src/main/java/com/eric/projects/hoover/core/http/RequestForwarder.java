package com.eric.projects.hoover.core.http;

import com.eric.projects.hoover.config.MappingProperties;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.http.ResponseEntity;

public class RequestForwarder {
    private static final ILogger logger = SLoggerFactory.getLogger(RequestForwarder.class);

    public ResponseEntity<byte[]> forwardHttpRequest(RequestData data, String traceId, MappingProperties mapping) {

    }
}
