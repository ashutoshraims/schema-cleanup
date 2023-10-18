package org.opengroup.osdu.azure.cleanup.service;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.implementation.guava25.collect.Lists;
import com.azure.storage.blob.models.BlobStorageException;
import org.opengroup.osdu.azure.cleanup.controller.BlobStoreController;
import org.opengroup.osdu.azure.cleanup.controller.CosmosDbController;
import org.opengroup.osdu.azure.cleanup.records.CleanupResponse;
import org.opengroup.osdu.azure.cleanup.records.CosmosResponseSchemaObject;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class CleanupService {

    private List<String> schemaIdsDeleted = new ArrayList<>();
    private List<CosmosResponseSchemaObject> schemasToBeDeleted = new ArrayList<>();
    @Autowired
    private CosmosDbController cosmosDbController;
    @Autowired
    private BlobStoreController blobStoreController;
    public CleanupResponse cleanupRecords() {
        fetchSchemasToDelete();
        if(schemasToBeDeleted.size() > 0) {
            deleteFromCosmosInBatches();
            deleteFromBlobStorageInBatches();
            schemasToBeDeleted.stream()
                    .parallel()
                    .forEach(item -> schemaIdsDeleted.add(item.id()));
        }
        return new CleanupResponse(schemaIdsDeleted.size(), schemaIdsDeleted, HttpStatus.OK);
    }

    private void deleteFromBlobStorageInBatches() {
        try {
            List<List<CosmosResponseSchemaObject>> listsToDelete = Lists.partition(schemasToBeDeleted, 100);
            for (List<CosmosResponseSchemaObject> listToDelete : listsToDelete) {
                blobStoreController.bulkDeleteSchemaBlobItems(listToDelete);
            }
        }
        catch (BlobStorageException e){
            throw new AppException(e.getStatusCode(), "Exception while deleting the blob", e.getMessage());
        }
        catch (Exception e){
            throw new AppException(500, "Exception while deleting the blob", "Exception while deleting the blob" + e.getMessage());
        }
    }

    private void deleteFromCosmosInBatches() {
        try {
            List<List<CosmosResponseSchemaObject>> listsToDelete = Lists.partition(schemasToBeDeleted, 100);
            for (List<CosmosResponseSchemaObject> listToDelete : listsToDelete) {
                cosmosDbController.bulkDeleteItems(listToDelete);
            }
        }
        catch (CosmosException e){
            throw new AppException(e.getStatusCode(), "Exception while deleting from the cosmos", e.getMessage());
        }
    }

    private void fetchSchemasToDelete() {
        try {
            schemasToBeDeleted = cosmosDbController.GetRecordsToDelete();
        } catch (CosmosException e) {
            throw new AppException(e.getStatusCode(), "Exception while fetching records from the cosmos", e.getMessage());
        }
    }
}
