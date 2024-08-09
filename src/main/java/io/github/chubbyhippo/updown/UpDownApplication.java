package io.github.chubbyhippo.updown;

import io.github.chubbyhippo.updown.infrastructure.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class UpDownApplication {

    public static void main(String[] args) {
        SpringApplication.run(UpDownApplication.class, args);
    }

}
