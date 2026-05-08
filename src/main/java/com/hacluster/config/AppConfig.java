package com.hacluster.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Instant;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Value("${app.instance.id:local-dev}")
    private String instanceId;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Bean
    public InstanceInfo instanceInfo() {
        return new InstanceInfo(instanceId, serverPort, appVersion, Instant.now());
    }

    public record InstanceInfo(String instanceId, String port, String version, Instant startTime) {}
}
