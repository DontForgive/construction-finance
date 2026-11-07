package br.com.galsystem.construction.finance.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Locale;
import java.util.Set;

public class IsoCountryValidator implements ConstraintValidator<IsoCountry, String> {

    private static final Set<String> CODES = Set.of(Locale.getISOCountries());

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return CODES.contains(value);
    }
}
