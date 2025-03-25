package com.vba.tiktok;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@SpringBootApplication
public class TiktokApplication {

    public static void main(String[] args) {
        SpringApplication.run(TiktokApplication.class, args);
    }

}
