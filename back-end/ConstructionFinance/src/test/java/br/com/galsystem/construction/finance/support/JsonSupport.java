package br.com.galsystem.construction.finance.support;

import br.com.galsystem.construction.finance.dto.supplier.SupplierUpdateDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;
import java.util.stream.Collectors;

/** Utilitários de desserialização JSON e Bean Validation para os testes. */
public final class JsonSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    private JsonSupport() {
    }

    public static SupplierUpdateDTO updateFromJson(final String json) {
        try {
            return MAPPER.readValue(json, SupplierUpdateDTO.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String toJson(final Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static <T> Set<ConstraintViolation<T>> validate(final T value) {
        return VALIDATOR.validate(value);
    }

    public static <T> Set<String> violatedProperties(final T value) {
        return validate(value).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
