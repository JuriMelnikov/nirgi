package ee.jvm.nirgi_java;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import jakarta.annotation.PostConstruct;

@SpringBootTest
@ActiveProfiles("test")
class NirgiJavaApplicationTests {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${test.property}")
    private String testProperty;

    @Test
    void contextLoads() {
        // This method will only be called if context loads successfully
        System.out.println("Datasource URL in test method: " + datasourceUrl);
        System.out.println("Test property in test method: " + testProperty);
    }

    @Configuration
    static class TestConfig {
        @Value("${spring.datasource.url}")
        private String datasourceUrl;

        @Value("${test.property}")
        private String testProperty;

        @PostConstruct
        public void init() {
            System.out.println(">>> TestConfig datasource URL: " + datasourceUrl);
            System.out.println(">>> TestConfig test property: " + testProperty);
        }

        @Bean
        public String testBean() {
            return "test";
        }
    }

}