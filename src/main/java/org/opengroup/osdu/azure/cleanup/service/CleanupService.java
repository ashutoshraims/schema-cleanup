package org.opengroup.osdu.azure.cleanup.service;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.guava25.collect.Lists;
import com.azure.storage.blob.batch.BlobBatchAsyncClient;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.azure.cleanup.config.SchemaQueryParameters;
import org.opengroup.osdu.azure.cleanup.provider.BatchBlobClientProvider;
import org.opengroup.osdu.azure.cleanup.provider.CosmosContainerProvider;
import org.opengroup.osdu.azure.cleanup.records.CleanupResponse;
import org.opengroup.osdu.azure.cleanup.records.CosmosResponseSchemaObject;
import org.opengroup.osdu.azure.cleanup.repository.BlobStoreRepository;
import org.opengroup.osdu.azure.cleanup.repository.CosmosDbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CleanupService {
    @Autowired
    private CosmosDbRepository cosmosDbRepository;
    @Autowired
    private BlobStoreRepository blobStoreRepository;
    @Autowired
    private SchemaQueryParameters schemaQueryParameters;

    @Autowired
    private BatchBlobClientProvider batchBlobClientProvider;

    @Autowired
    private CosmosContainerProvider cosmosContainerProvider;

    public Mono<CleanupResponse> cleanupRecords() {
       return cleanSchemas()
               .zipWith(cleanSystemSchemas(), (schemaIdsDeleted, systemSchemaIdsDeleted) ->{
                  List<String> allSchemaIdsDeleted = new ArrayList<>(schemaIdsDeleted);
                  allSchemaIdsDeleted.addAll(systemSchemaIdsDeleted);
                  return allSchemaIdsDeleted;
               })
               .doOnSubscribe(subscription -> log.info("Starting cleanup"))
               .doOnNext(allSchemaIdsDeleted -> log.info("All Schema IDs to delete: {}", allSchemaIdsDeleted))
               .map(allSchemaIdsDeleted -> new CleanupResponse(allSchemaIdsDeleted.size(), allSchemaIdsDeleted, HttpStatus.OK))
               .doOnSuccess(cleanupResponse -> log.info("Cleanup process completed successfully with {} schemas deleted", cleanupResponse.schemasDeletedSize()));

    }

    private Mono<List<String>> cleanSchemas() {
        return cosmosDbRepository.getRecordsToDelete(cosmosContainerProvider.getCosmosContainer(), schemaQueryParameters.getId())
                .collectList()
                .flatMap(schemasToBeDeleted -> {
                    if (schemasToBeDeleted.isEmpty())
                        return Mono.just(new ArrayList<>());

                    return deleteFromCosmosInBatches(cosmosContainerProvider.getCosmosContainer(), schemasToBeDeleted)
                            .doOnSuccess(aVoid -> log.info("Successfully deleted items from Cosmos DB, proceeding to delete from Blob Storage."))
                            .then(deleteFromBlobStorageInBatches(batchBlobClientProvider.getBlobBatchClient(), schemasToBeDeleted))
                            .thenReturn(schemasToBeDeleted.stream()
                                    .map(CosmosResponseSchemaObject::id)
                                    .collect(Collectors.toList()));
                });
    }

    private Mono<List<String>> cleanSystemSchemas() {
        return cosmosDbRepository.getRecordsToDelete(cosmosContainerProvider.getSystemCosmosContainer(), schemaQueryParameters.getSystemId())
                .collectList().flatMap(schemasToBeDeleted -> {
                    if (schemasToBeDeleted.isEmpty())
                        return Mono.just(new ArrayList<>());

                    return deleteFromCosmosInBatches(cosmosContainerProvider.getSystemCosmosContainer(), schemasToBeDeleted)
                            .doOnSuccess(aVoid -> log.info("Successfully deleted items from Cosmos DB, proceeding to delete from Blob Storage."))
                            .then(deleteFromBlobStorageInBatches(batchBlobClientProvider.getSystemBlobBatchClient(), schemasToBeDeleted))
                            .thenReturn(schemasToBeDeleted.stream()
                                    .map(CosmosResponseSchemaObject::id).collect(Collectors.toList()));
                });
    }


    private Mono<Void> deleteFromBlobStorageInBatches(BlobBatchAsyncClient blobBatchClient, List<CosmosResponseSchemaObject> schemasToBeDeleted) {
        List<List<CosmosResponseSchemaObject>> listsToDelete = Lists.partition(schemasToBeDeleted, 100);
        return Flux.fromIterable(listsToDelete)
                .flatMap(batch -> blobStoreRepository.bulkDeleteSchemaBlobItems(blobBatchClient, batch))
                .then()
                .doOnError(throwable -> log.error("Exception while deleting from the blob store", throwable));
    }

    private Mono<Void> deleteFromCosmosInBatches(CosmosAsyncContainer container, List<CosmosResponseSchemaObject> schemasToBeDeleted) {
        List<List<CosmosResponseSchemaObject>> listsToDelete = Lists.partition(schemasToBeDeleted, 100);
        return Flux.fromIterable(listsToDelete)
                .flatMap(batch -> cosmosDbRepository.bulkDeleteItems(container, batch))
                .then()
                .doOnError(throwable -> log.error("Exception while deleting from the cosmos", throwable));
    }
}
