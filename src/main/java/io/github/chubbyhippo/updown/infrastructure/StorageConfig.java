package io.github.chubbyhippo.updown.infrastructure;

import io.github.chubbyhippo.updown.domain.StorageService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {
    @Bean
    ApplicationRunner applicationRunner(StorageService storageService) {
        return args -> {
            storageService.deleteAll();
            storageService.init();
        };
    }
}
