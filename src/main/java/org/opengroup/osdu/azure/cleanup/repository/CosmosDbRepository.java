package org.opengroup.osdu.azure.cleanup.repository;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.azure.cleanup.records.CosmosResponseSchemaObject;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class CosmosDbRepository {
    private CosmosClient cosmosClient;

    public Flux<CosmosResponseSchemaObject> getRecordsToDelete(CosmosAsyncContainer cosmosContainer, String id) {
        try {
            String selectQuery = String.format("SELECT c.id, c.partitionId FROM c WHERE CONTAINS(c.id, \"%s\")", id);
            CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();

            return cosmosContainer.queryItems(selectQuery,
                            queryOptions, CosmosResponseSchemaObject.class)
                            .byPage()
                    .flatMap(page->Flux.fromIterable(page.getElements()))
                    .onErrorMap(CosmosException.class, e ->  new AppException(e.getStatusCode(), "Exception while fetching records from the cosmos", e.getMessage()));
        } catch (Exception e) {
            return Flux.error(new AppException(500, "exception while running the query", e.getMessage()));
        }
    }

    public Mono<Void> bulkDeleteItems(CosmosAsyncContainer cosmosAsyncContainer, List<CosmosResponseSchemaObject> listToDelete) {
        return Flux.fromIterable(listToDelete)
                .map(schemaObject -> CosmosBulkOperations
                        .getDeleteItemOperation(schemaObject.id(), new PartitionKey(schemaObject.partitionId())))
                .collectList()
                .flatMap(cosmosItemOperations -> {
                    if (cosmosItemOperations.isEmpty())
                        return Mono.empty();

                    return cosmosAsyncContainer.executeBulkOperations(
                                    Flux.fromIterable(cosmosItemOperations))
                            .flatMap(response -> {
                                log.info(String.format("Deleting schema %s completed with status code %d%n",
                                        response.getOperation().getId(), response.getResponse().getStatusCode())
                                );
                                return Mono.empty();
                            }).then();
                }).doOnError(throwable -> log.error("error while executing bulk ops: ", throwable));


    }
}
