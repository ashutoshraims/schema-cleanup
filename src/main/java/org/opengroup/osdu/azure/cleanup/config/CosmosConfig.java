package org.opengroup.osdu.azure.cleanup.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class CosmosConfig {
    @Value("${cosmos.key}")
    private String cosmosKey;
    @Value("${cosmos.host}")
    private String cosmosHost;

    @Value("${system.cosmos.key}")
    private String systemCosmosKey;
    @Value("${system.cosmos.host}")
    private String systemCosmosHost;
}
