package ee.jvm.nirgi_java.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cors")
public record CorsProperties(String allowedOrigins) {
}
