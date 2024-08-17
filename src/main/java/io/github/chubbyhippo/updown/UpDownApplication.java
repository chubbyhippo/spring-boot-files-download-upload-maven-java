package io.github.chubbyhippo.updown;

import io.github.chubbyhippo.updown.infrastructure.FileSystemStorageService;
import io.github.chubbyhippo.updown.infrastructure.StorageProperties;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class UpDownApplication {

    public static void main(String[] args) {
        SpringApplication.run(UpDownApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(FileSystemStorageService storageService) {
        return args -> {
            storageService.deleteAll();
            storageService.init();
        };
    }
}
