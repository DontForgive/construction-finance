package br.com.galsystem.construction.finance.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valida código de país ISO 3166-1 alpha-2. Valor nulo ou vazio é aceito (campo opcional).
 */
@Documented
@Constraint(validatedBy = IsoCountryValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface IsoCountry {
    String message() default "País deve ser um código ISO 3166-1 alpha-2 válido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
