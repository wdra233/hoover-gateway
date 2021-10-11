package com.eric.projects.common.config;

import com.eric.projects.common.aop.SentryClientAspect;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Use this common config for Web App
 */
@Configuration
@Import(value = {HooverConfig.class, SentryClientAspect.class,})
public class HooverWebConfig {
}
