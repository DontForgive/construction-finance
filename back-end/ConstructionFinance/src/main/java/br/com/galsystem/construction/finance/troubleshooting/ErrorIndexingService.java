package br.com.galsystem.construction.finance.troubleshooting;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ErrorIndexingService {
    private static final Logger log = LoggerFactory.getLogger(ErrorIndexingService.class);

    private final RestTemplate restTemplate;
    private final String opensearchUrl;
    private final String indexName;
    private final ObjectMapper objectMapper;

    @Autowired
    public ErrorIndexingService(RestTemplate restTemplate, String opensearchUrl, String opensearchIndexName) {
        this.restTemplate = restTemplate;
        this.opensearchUrl = opensearchUrl;
        this.indexName = opensearchIndexName;
        this.objectMapper = new ObjectMapper();
    }

    @Async
    public void indexError(ErrorDocument errorDocument) {
        try {
            String indexUrl = String.format("%s/%s/_doc", opensearchUrl, indexName);

            ResponseEntity<String> response = restTemplate.postForEntity(indexUrl, errorDocument, String.class);

            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                log.debug("Erro indexado no OpenSearch (correlationId: {})", errorDocument.getCorrelationId());
            } else {
                log.warn("Resposta inesperada ao indexar no OpenSearch: {} (correlationId: {})",
                        response.getStatusCode(), errorDocument.getCorrelationId());
            }
        } catch (Exception e) {
            log.error("Erro ao indexar documento no OpenSearch (correlationId: {}): {}",
                    errorDocument.getCorrelationId(), e.getMessage(), e);
        }
    }
}
