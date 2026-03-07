package com.library_management.library_management_artifact.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {

    private Jwt jwt = new Jwt();
    private EmailVerification emailVerification = new EmailVerification();
    private String baseUrl = "http://localhost:8080";
    private Rag rag = new Rag();
    private Fine fine = new Fine();
    private Cors cors = new Cors();

    @Data
    public static class Jwt {
        private String secret;
        private long accessTokenExpiryMs;
        private long refreshTokenExpiryMs;
    }

    @Data
    public static class EmailVerification {
        private long expiryMinutes = 30;
    }

    @Data
    public static class Rag {
        private int topK = 5;
    }

    @Data
    public static class Fine {
        private double ratePerDay = 0.50;
        private double maxAmount = 10.00;
        private int gracePeriodDays = 2;
        private int loanPeriodDays = 14;
    }

    @Data
    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:3000", "http://localhost:5173");
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");
        private List<String> allowedHeaders = List.of("*");
        private boolean allowCredentials = true;
        private long maxAge = 3600;
    }
}
