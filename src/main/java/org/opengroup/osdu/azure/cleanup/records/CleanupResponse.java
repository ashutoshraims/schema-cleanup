package org.opengroup.osdu.azure.cleanup.records;

import org.springframework.http.HttpStatus;

import java.util.List;

public record CleanupResponse(int schemasDeletedSize, List<String> schemaIds, HttpStatus statusCode) {
}
