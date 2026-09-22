package br.com.galsystem.construction.finance.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OpenSearchConfig {
    private static final Logger log = LoggerFactory.getLogger(OpenSearchConfig.class);

    @Value("${app.opensearch.url:http://localhost:9200}")
    private String opensearchUrl;

    @Value("${app.opensearch.index-name:cf-errors}")
    private String indexName;

    @Bean
    public String opensearchUrl() {
        return opensearchUrl;
    }

    @Bean
    public String opensearchIndexName() {
        return indexName;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
