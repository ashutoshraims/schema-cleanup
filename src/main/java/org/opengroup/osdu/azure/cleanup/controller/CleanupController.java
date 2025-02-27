package org.opengroup.osdu.azure.cleanup.controller;

import org.opengroup.osdu.azure.cleanup.records.CleanupResponse;
import org.opengroup.osdu.azure.cleanup.service.CleanupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("schemas")
public class CleanupController {
    @Autowired
    private CleanupService cleanupService;

    @PostMapping()
    public Mono<ResponseEntity<CleanupResponse>> cleanupSchemas() {
        return cleanupService.cleanupRecords().map( cleanupResponse -> new ResponseEntity<>(cleanupResponse, cleanupResponse.statusCode()));
    }

}
