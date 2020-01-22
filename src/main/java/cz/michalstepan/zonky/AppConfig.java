package cz.michalstepan.zonky;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableRetry
@EnableScheduling
public class AppConfig {

    @Bean
    public RestTemplate restTemplateZonky() {
        return new RestTemplateBuilder()
                .rootUri("https://api.zonky.cz")
                .build();
    }
}
