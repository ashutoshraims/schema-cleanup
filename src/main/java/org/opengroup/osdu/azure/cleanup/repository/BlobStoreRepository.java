package org.opengroup.osdu.azure.cleanup.repository;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.batch.BlobBatchAsyncClient;
import com.azure.storage.blob.batch.BlobBatchClient;
import com.azure.storage.blob.batch.BlobBatchClientBuilder;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.azure.cleanup.config.BlobConfig;
import org.opengroup.osdu.azure.cleanup.records.CosmosResponseSchemaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Repository
public class BlobStoreRepository {
    @Autowired
    private BlobConfig config;

    private String getBlobUrlFromSchemaId(String schemaId) {
        return String.format(
                "https://%s.blob.core.windows.net/opendes/%s.json", config.getStorageAccountName(), schemaId);
    }

    public Mono<Void> bulkDeleteSchemaBlobItems(BlobBatchAsyncClient blobBatchClient, List<CosmosResponseSchemaObject> schemaIdsToDelete) {
        return Flux.fromIterable(schemaIdsToDelete).map(schemaObject -> getBlobUrlFromSchemaId(schemaObject.id())).collectList()
                .flatMap(blobUrls -> blobBatchClient.deleteBlobs(blobUrls, DeleteSnapshotsOptionType.INCLUDE)
                        .flatMap(response -> {
                            log.info(String.format("Deleting blob with URL %s completed with status code %d%n",
                                    response.getRequest().getUrl(), response.getStatusCode()));
                            return Mono.just(response);
                        })
                        .then()).doOnError(throwable -> log.error("error while executing bulk ops: ", throwable));
    }
}
