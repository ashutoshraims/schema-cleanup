package org.opengroup.osdu.azure.cleanup.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class BlobConfig {
    @Value("${blob.account_name}")
    private String storageAccountName;

    @Value("${blob.account_key}")
    private String storageAccountKey;

    @Value("${system.blob.account_name}")
    private String systemStorageAccountName;

    @Value("${system.blob.account_key}")
    private String systemStorageAccountKey;

}
