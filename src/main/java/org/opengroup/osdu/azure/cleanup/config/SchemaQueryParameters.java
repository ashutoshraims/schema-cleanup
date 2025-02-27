package org.opengroup.osdu.azure.cleanup.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class SchemaQueryParameters {
    @Value("${schema.query_params.id}")
    private String id;

    @Value("${system.schema.query_params.id}")
    private String systemId;
}
