package br.com.galsystem.construction.finance.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Locale;
import java.util.Set;

public class PaisValidator implements ConstraintValidator<PaisValido, String> {

    private static final Set<String> PAISES = Set.of(Locale.getISOCountries());

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return PAISES.contains(value.trim().toUpperCase(Locale.ROOT));
    }
}
