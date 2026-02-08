package io.github.chubbyhippo.updown;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class UpDownApplication {

    public static void main(String[] args) {
        SpringApplication.run(UpDownApplication.class, args);
    }
}
