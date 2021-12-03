package com.eric.projects.hoover.config;

import com.eric.projects.common.config.HooverWebConfig;
import com.eric.projects.common.env.EnvConfig;
import com.eric.projects.hoover.core.balancer.LoadBalancer;
import com.eric.projects.hoover.core.balancer.RandomLoadBalancer;
import com.eric.projects.hoover.core.filter.FaviconFilter;
import com.eric.projects.hoover.core.filter.HealthCheckFilter;
import com.eric.projects.hoover.core.filter.NakedDomainFilter;
import com.eric.projects.hoover.core.filter.SecurityFilter;
import com.eric.projects.hoover.core.http.HttpClientProvider;
import com.eric.projects.hoover.core.http.RequestDataExtractor;
import com.eric.projects.hoover.core.http.RequestForwarder;
import com.eric.projects.hoover.core.http.ReverseProxyFilter;
import com.eric.projects.hoover.core.interceptor.AuthRequestInterceptor;
import com.eric.projects.hoover.core.interceptor.CacheResponseInterceptor;
import com.eric.projects.hoover.core.interceptor.PostForwardResponseInterceptor;
import com.eric.projects.hoover.core.interceptor.PreForwardRequestInterceptor;
import com.eric.projects.hoover.core.mappings.ConfigurationMappingsProvider;
import com.eric.projects.hoover.core.mappings.MappingsProvider;
import com.eric.projects.hoover.core.mappings.MappingsValidator;
import com.eric.projects.hoover.core.mappings.ProgrammaticMappingsProvider;
import com.eric.projects.hoover.core.trace.LoggingTraceInterceptor;
import com.eric.projects.hoover.core.trace.ProxyingTraceInterceptor;
import com.eric.projects.hoover.core.trace.TraceInterceptor;
import com.eric.projects.hoover.view.AssetLoader;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import java.util.Optional;

@Configuration
@EnableConfigurationProperties({HooverProperties.class, AppProperties.class})
@Import(value = HooverWebConfig.class)
public class HooverConfiguration {

    protected final HooverProperties hooverProperties;
    protected final ServerProperties serverProperties;
    protected final AppProperties appProperties;
    protected final AssetLoader assetLoader;

    public HooverConfiguration(HooverProperties hooverProperties, ServerProperties serverProperties, AppProperties appProperties, AssetLoader assetLoader) {
        this.hooverProperties = hooverProperties;
        this.serverProperties = serverProperties;
        this.appProperties = appProperties;
        this.assetLoader = assetLoader;
    }

    @Bean
    public FilterRegistrationBean<ReverseProxyFilter> hooverReverseProxyFilterRegistrationBean(ReverseProxyFilter proxyFilter) {
        FilterRegistrationBean<ReverseProxyFilter> registrationBean = new FilterRegistrationBean<>(proxyFilter);
        registrationBean.setOrder(hooverProperties.getFilterOrder()); // by default to Ordered.HIGHEST_PRECEDENCE + 100
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<NakedDomainFilter> nakedDomainFilterFilterRegistrationBean(EnvConfig envConfig) {
        FilterRegistrationBean<NakedDomainFilter> registrationBean = new FilterRegistrationBean<>(new NakedDomainFilter(envConfig));
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 90); // before ReverseProxyFilter
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<SecurityFilter> securityFilterRegistrationBean(EnvConfig envConfig) {
        FilterRegistrationBean<SecurityFilter> registrationBean =
                new FilterRegistrationBean<>(new SecurityFilter(envConfig));
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 80); // before nakedDomainFilter
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<FaviconFilter> faviconFilterRegistrationBean() {
        FilterRegistrationBean<FaviconFilter> registrationBean =
                new FilterRegistrationBean<>(new FaviconFilter(assetLoader.getFaviconFile()));
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 75); // before securityFilter
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<HealthCheckFilter> healthCheckFilterRegistrationBean() {
        FilterRegistrationBean<HealthCheckFilter> registrationBean =
                new FilterRegistrationBean<>(new HealthCheckFilter());
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 70); // before faviconFilter
        return registrationBean;
    }

    @Bean
    @ConditionalOnMissingBean
    public ReverseProxyFilter hooverReverseProxyFilter(
            RequestDataExtractor extractor,
            MappingsProvider mappingsProvider,
            RequestForwarder requestForwarder,
            ProxyingTraceInterceptor traceInterceptor,
            PreForwardRequestInterceptor requestInterceptor
    ) {
        return new ReverseProxyFilter(hooverProperties, extractor, mappingsProvider,
                requestForwarder, traceInterceptor, requestInterceptor);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public HttpClientProvider hooverHttpClientProvider() {
        return new HttpClientProvider();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public RequestDataExtractor hooverRequestDataExtractor() {
        return new RequestDataExtractor();
    }


    @Bean
    @ConditionalOnMissingBean
    public MappingsProvider hooverConfigurationMappingsProvider(EnvConfig envConfig,
                                                                MappingsValidator mappingsValidator,
                                                                HttpClientProvider httpClientProvider) {
        if (hooverProperties.isEnableProgrammaticMapping()) {
            return new ProgrammaticMappingsProvider(
                    serverProperties,
                    hooverProperties, mappingsValidator,
                    httpClientProvider, envConfig);
        } else {
            return new ConfigurationMappingsProvider(
                    serverProperties,
                    hooverProperties, mappingsValidator,
                    httpClientProvider);
        }
    }


    @Bean
    @ConditionalOnMissingBean
    public LoadBalancer hooverLoadBalancer() {
        return new RandomLoadBalancer();
    }

    @Bean
    @ConditionalOnMissingBean
    public MappingsValidator hooverMappingsValidator() {
        return new MappingsValidator();
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestForwarder hooverRequestForwarder(
            HttpClientProvider httpClientProvider,
            MappingsProvider mappingsProvider,
            LoadBalancer loadBalancer,
            Optional<MeterRegistry> meterRegistry,
            ProxyingTraceInterceptor traceInterceptor,
            PostForwardResponseInterceptor responseInterceptor
    ) {
        return new RequestForwarder(
                serverProperties, hooverProperties, httpClientProvider,
                mappingsProvider, loadBalancer, meterRegistry,
                traceInterceptor, responseInterceptor);
    }

    @Bean
    @ConditionalOnMissingBean
    public TraceInterceptor hooverTraceInterceptor() {
        return new LoggingTraceInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public ProxyingTraceInterceptor hooverProxyingTraceInterceptor(TraceInterceptor traceInterceptor) {
        return new ProxyingTraceInterceptor(hooverProperties, traceInterceptor);
    }

    @Bean
    @ConditionalOnMissingBean
    public PreForwardRequestInterceptor hooverPreForwardRequestInterceptor(EnvConfig envConfig) {
        //return new NoOpPreForwardRequestInterceptor();
        return new AuthRequestInterceptor(appProperties.getSigningSecret(), envConfig);
    }

    @Bean
    @ConditionalOnMissingBean
    public PostForwardResponseInterceptor hooverPostForwardResponseInterceptor() {
        //return new NoOpPostForwardResponseInterceptor();
        return new CacheResponseInterceptor();
    }



}
