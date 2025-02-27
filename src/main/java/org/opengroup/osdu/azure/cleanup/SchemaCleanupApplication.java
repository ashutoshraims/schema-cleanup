package org.opengroup.osdu.azure.cleanup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"org.opengroup.osdu"})
@SpringBootApplication( exclude = {SecurityAutoConfiguration.class })
public class SchemaCleanupApplication {
    public static void main(String[] args) {
        SpringApplication.run(SchemaCleanupApplication.class, args);
    }
}
