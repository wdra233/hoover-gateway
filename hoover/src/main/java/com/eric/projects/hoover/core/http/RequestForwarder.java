package com.eric.projects.hoover.core.http;

import com.eric.projects.hoover.config.HooverProperties;
import com.eric.projects.hoover.config.MappingProperties;
import com.eric.projects.hoover.core.balancer.LoadBalancer;
import com.eric.projects.hoover.core.interceptor.PostForwardResponseInterceptor;
import com.eric.projects.hoover.core.mappings.MappingsProvider;
import com.eric.projects.hoover.core.trace.ProxyingTraceInterceptor;
import com.eric.projects.hoover.exceptions.HooverException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Optional;

import static java.lang.System.nanoTime;
import static java.time.Duration.ofNanos;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.ResponseEntity.status;

public class RequestForwarder {
    private static final ILogger log = SLoggerFactory.getLogger(RequestForwarder.class);

    protected final ServerProperties serverProperties;
    protected final HooverProperties hooverProperties;
    protected final HttpClientProvider httpClientProvider;
    protected final MappingsProvider mappingsProvider;
    protected final LoadBalancer loadBalancer;
    protected final Optional<MeterRegistry> meterRegistry;
    protected final ProxyingTraceInterceptor traceInterceptor;
    protected final PostForwardResponseInterceptor postForwardResponseInterceptor;


    public RequestForwarder(ServerProperties serverProperties,
                            HooverProperties hooverProperties,
                            HttpClientProvider httpClientProvider,
                            MappingsProvider mappingsProvider,
                            LoadBalancer loadBalancer,
                            Optional<MeterRegistry> meterRegistry,
                            ProxyingTraceInterceptor traceInterceptor,
                            PostForwardResponseInterceptor postForwardResponseInterceptor) {
        this.serverProperties = serverProperties;
        this.hooverProperties = hooverProperties;
        this.httpClientProvider = httpClientProvider;
        this.mappingsProvider = mappingsProvider;
        this.loadBalancer = loadBalancer;
        this.meterRegistry = meterRegistry;
        this.traceInterceptor = traceInterceptor;
        this.postForwardResponseInterceptor = postForwardResponseInterceptor;
    }

    public ResponseEntity<byte[]> forwardHttpRequest(RequestData data, String traceId, MappingProperties mapping) {
        ForwardDestination destination = resolveForwardDestination(data.getUri(), mapping);
        prepareForwardedRequestHeaders(data, destination);
        traceInterceptor.onForwardStart(traceId, destination.getMappingName(),
                data.getMethod(), data.getHost(), destination.getUri().toString(), data.getBody(), data.getHeaders());

        RequestEntity<byte[]> request = new RequestEntity<>(data.getBody(), data.getHeaders(), data.getMethod(), destination.getUri());
        ResponseData response = sendRequest(traceId, request, mapping, destination.getMappingMetricsName(), data);

        log.debug(String.format("Forwarded: %s %s %s -> %s %d", data.getMethod(), data.getHost(), data.getUri(), destination.getUri(), response.getStatus().value()));

        traceInterceptor.onForwardComplete(traceId, response.getStatus(), response.getBody(), response.getHeaders());
        postForwardResponseInterceptor.intercept(response, mapping);
        prepareForwardedResponse(response);

        return status(response.getStatus())
                .headers(response.getHeaders())
                .body(response.getBody());

    }

    protected ResponseData sendRequest(String traceId, RequestEntity<byte[]> request, MappingProperties mapping, String mappingMetricsName, RequestData requestData) {
        ResponseEntity<byte[]> response;
        long startTime = nanoTime();
        try {
            response = httpClientProvider.getHttpClient(mapping.getName()).exchange(request, byte[].class);
            recordLatency(mappingMetricsName, startTime);
        } catch (HttpStatusCodeException e) {
            recordLatency(mappingMetricsName, startTime);
            response = status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            recordLatency(mappingMetricsName, startTime);
            traceInterceptor.onForwardFailed(traceId, e);
            throw e;
        }
        UnmodifiableRequestData data = new UnmodifiableRequestData(requestData);
        return new ResponseData(response.getStatusCode(), response.getHeaders(), response.getBody(), data);
    }

    protected void recordLatency(String metricName, long startingTime) {
        meterRegistry.ifPresent(meterRegistry -> meterRegistry.timer(metricName).record(ofNanos(nanoTime() - startingTime)));
    }

    /**
     * Remove any protocol-level headers from the clients request that
     * do not apply to the new request we are sending to the remote server.
     *
     * @param request
     * @param destination
     */
    protected void prepareForwardedRequestHeaders(RequestData request, ForwardDestination destination) {
        HttpHeaders headers = request.getHeaders();
        //headers.set(HOST, destination.getUri().getAuthority());
        headers.remove(TE);
    }


    /**
     * Remove any protocol-level headers from the remote server's response that
     * do not apply to the new response we are sending.
     *
     * @param response
     */
    protected void prepareForwardedResponse(ResponseData response) {
        HttpHeaders headers = response.getHeaders();
        headers.remove(TRANSFER_ENCODING);
        headers.remove(CONNECTION);
        headers.remove("Public-Key-Pins");
        headers.remove(SERVER);
        headers.remove("Strict-Transport-Security");
    }

    protected ForwardDestination resolveForwardDestination(String originUri, MappingProperties mapping) {
        return new ForwardDestination(createDestinationUrl(originUri, mapping), mapping.getName(), resolveMetricsName(mapping));
    }

    protected URI createDestinationUrl(String uri, MappingProperties mapping) {
        String host = loadBalancer.chooseDestination(mapping.getDestinations());
        try {
            return new URI(host + uri);
        } catch (URISyntaxException e) {
            throw new HooverException("Error creating destination URL from HTTP request URI: " + uri + " using mapping " + mapping, e);
        }
    }

    protected String resolveMetricsName(MappingProperties mapping) {
        return hooverProperties.getMetrics().getNamesPrefix() + "." + mapping.getName();
    }


}
