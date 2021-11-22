package com.eric.projects.hoover.core.interceptor;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.eric.projects.common.auth.AuthConstant;
import com.eric.projects.common.auth.Sessions;
import com.eric.projects.common.crypto.Sign;
import com.eric.projects.common.env.EnvConfig;
import com.eric.projects.common.services.SecurityConstant;
import com.eric.projects.common.services.Service;
import com.eric.projects.common.services.ServiceDirectory;
import com.eric.projects.hoover.config.MappingProperties;
import com.eric.projects.hoover.core.http.RequestData;
import com.eric.projects.hoover.exceptions.ForbiddenException;
import com.eric.projects.hoover.exceptions.HooverException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class AuthRequestInterceptor implements PreForwardRequestInterceptor {
    private final static ILogger log = SLoggerFactory.getLogger(AuthRequestInterceptor.class);

    private final String signingSecret;
    private final EnvConfig envConfig;

    public AuthRequestInterceptor(String signingSecret, EnvConfig envConfig) {
        this.signingSecret = signingSecret;
        this.envConfig = envConfig;
    }

    // Use a map for constant time lookups. Value doesn't matter
    // Hypothetically these should be universally unique, so we don't have to limit by env
    private final Map<String, String> bannedUsers = new HashMap<String, String>() {{
        put("d7b9dbed-9719-4856-5f19-23da2d0e3dec", "hidden");
    }};

    @Override
    public void intercept(RequestData data, MappingProperties mapping) {
        // sanitize incoming requests and set authorization information
        String authorization = this.setAuthHeader(data, mapping);

        this.validateRestrict(mapping);
        this.validateSecurity(data, mapping, authorization);
    }

    private String setAuthHeader(RequestData data, MappingProperties mapping) {
        String authorization = AuthConstant.AUTHORIZATION_ANONYMOUS_WEB;
        HttpHeaders headers = data.getHeaders();
        Session session = this.getSession(data.getOriginRequest());
        if (session != null) {
            if (session.isSupport()) {
                authorization = AuthConstant.AUTHORIZATION_SUPPORT_USER;
            } else {
                authorization = AuthConstant.AUTHORIZATION_AUTHENTICATED_USER;
            }
            this.checkBannedUsers(session.getUserId());
            headers.set(AuthConstant.CURRENT_USER_HEADER, session.getUserId());
        } else {
            // prevent hacking
            headers.remove(AuthConstant.CURRENT_USER_HEADER);
        }
        headers.set(AuthConstant.AUTHORIZATION_HEADER, authorization);
        return authorization;
    }

    private void checkBannedUsers(String userId) {
        if (bannedUsers.containsKey(userId)) {
            log.warn(String.format("Banned user accessing service - user %s", userId));
            throw new ForbiddenException("Banned user forbidden!");
        }
    }

    private Service getService(MappingProperties mapping) {
        String host = mapping.getHost();
        String subDomain = host.replace("." + envConfig.getExternalApex(), "");
        Service service = ServiceDirectory.getMapping().get(subDomain.toLowerCase());
        if (service == null) {
            throw new HooverException("Unsupported sub-domain " + subDomain);
        }
        return service;
    }

    private void validateRestrict(MappingProperties mapping) {
        Service service = this.getService(mapping);
        if (service.isRestrictDev() && !envConfig.isDebug()) {
            throw new HooverException("This service is restrict to dev and test environment only");
        }
    }

    private void validateSecurity(RequestData data, MappingProperties mapping, String authorization) {
        if (AuthConstant.AUTHORIZATION_ANONYMOUS_WEB.equals(authorization)) {
            Service service = this.getService(mapping);
            if (SecurityConstant.SEC_PUBLIC != service.getSecurity()) {
                log.info("Anonymous user want to get access secure service, redirect to login");
                String scheme = "https";
                if (envConfig.isDebug()) {
                    scheme = "http";
                }

                int serverPort = data.getOriginRequest().getServerPort();
                try {
                    URI redirectUrl = new URI(scheme, null, "www." + envConfig.getExternalApex(), serverPort, "/login/", null, null);
                    String redirectTo = data.getHost() + data.getUri();

                    String fullRedirectUrl = redirectUrl.toString() + redirectTo;
                    data.setNeedRedirect(true);
                    data.setRedirectUrl(fullRedirectUrl);
                } catch (URISyntaxException e) {
                    log.error("Fail to redirect url", e);
                }

            }
        }
    }

    private Session getSession(HttpServletRequest request) {
        String token = Sessions.getToken(request);
        if (token == null) return null;
        try {
            DecodedJWT decodedJWT = Sign.verifySessionToken(token, signingSecret);
            String userId = decodedJWT.getClaim(Sign.CLAIM_USER_ID).asString();
            Boolean support = decodedJWT.getClaim(Sign.CLAIM_SUPPORT).asBoolean();
            Session session = Session.builder().userId(userId).support(support).build();
            return session;
        } catch (Exception e) {
            log.error("fail to verify token", "token", token, e);
            return null;
        }

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Session {
        private String userId;
        private boolean support;
    }
}
