package org.opengroup.osdu.azure.cleanup.provider;

import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.batch.BlobBatchAsyncClient;
import com.azure.storage.blob.batch.BlobBatchClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import org.opengroup.osdu.azure.cleanup.config.BlobConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class BatchBlobClientProvider {

    @Autowired
    private BlobConfig config;

    private BlobServiceAsyncClient blobServiceAsyncClient;
    private BlobServiceAsyncClient systemBlobServiceAsyncClient;

    @PostConstruct
    private void initializeClients() {
        this.blobServiceAsyncClient = createBlobServiceClient(config.getStorageAccountName(), config.getStorageAccountKey());
        this.systemBlobServiceAsyncClient = createBlobServiceClient(config.getSystemStorageAccountName(), config.getSystemStorageAccountKey());
    }

    private BlobServiceAsyncClient createBlobServiceClient(String accountName, String accountKey) {
        RequestRetryOptions retryOptions = new RequestRetryOptions();
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);

        return new BlobServiceClientBuilder()
                .endpoint(String.format("https://%s.blob.core.windows.net", accountName))
                .credential(credential)
                .retryOptions(retryOptions)
                .buildAsyncClient();
    }

    public BlobBatchAsyncClient getBlobBatchClient() {
        return new BlobBatchClientBuilder(blobServiceAsyncClient).buildAsyncClient();
    }

    public BlobBatchAsyncClient getSystemBlobBatchClient() {
        return new BlobBatchClientBuilder(systemBlobServiceAsyncClient).buildAsyncClient();
    }
}
