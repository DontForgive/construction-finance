package br.com.galsystem.construction.finance.troubleshooting;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ErrorDocument {
    @JsonProperty("timestamp")
    private Instant timestamp;

    @JsonProperty("correlationId")
    private String correlationId;

    @JsonProperty("fingerprint")
    private String fingerprint;

    @JsonProperty("exceptionClass")
    private String exceptionClass;

    @JsonProperty("message")
    private String message;

    @JsonProperty("path")
    private String path;

    @JsonProperty("method")
    private String method;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("stacktrace")
    private String stacktrace;
}
