package org.opengroup.osdu.azure.cleanup.provider;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import org.opengroup.osdu.azure.cleanup.config.CosmosConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class CosmosContainerProvider {
    private CosmosAsyncClient cosmosAsyncClient;
    private CosmosAsyncClient systemCosmosAsyncClient;
    @Autowired
    private CosmosConfig config;

    @PostConstruct
    private void initializeClients() {
        cosmosAsyncClient = getCosmosClient(config.getCosmosHost(), config.getCosmosKey());
        systemCosmosAsyncClient = getCosmosClient(config.getSystemCosmosHost(),config.getSystemCosmosKey());
    }

    private CosmosAsyncClient getCosmosClient(String host, String key) {
        return new CosmosClientBuilder()
                .endpoint(host)
                .key(key)
                .buildAsyncClient();
    }

    public CosmosAsyncContainer getCosmosContainer() {
        return cosmosAsyncClient.getDatabase("osdu-db").getContainer("SchemaInfo");
    }

    public CosmosAsyncContainer getSystemCosmosContainer() {
        return systemCosmosAsyncClient.getDatabase("osdu-system-db").getContainer("SchemaInfo");
    }
}
