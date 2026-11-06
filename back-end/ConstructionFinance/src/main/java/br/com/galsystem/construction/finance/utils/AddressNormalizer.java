package br.com.galsystem.construction.finance.utils;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Normalização dos campos de endereço do fornecedor.
 * Texto: trim e vazio vira null. CEP: somente dígitos quando vier em formato com máscara válido.
 * UF e país: maiúsculas.
 */
public final class AddressNormalizer {

    private static final Pattern MASKED_ZIP = Pattern.compile("\\d{5}[-.]?\\d{3}|\\d{2}\\.\\d{3}-\\d{3}");

    private AddressNormalizer() {
    }

    public static String text(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String upper(final String value) {
        final String trimmed = text(value);
        return trimmed == null ? null : trimmed.toUpperCase(Locale.ROOT);
    }

    /**
     * Remove a máscara apenas quando o valor tem formato de CEP válido (8 dígitos com máscara).
     * Qualquer outro valor é mantido (já aparado) para que a validação o rejeite.
     */
    public static String zipCode(final String value) {
        final String trimmed = text(value);
        if (trimmed == null) {
            return null;
        }
        if (MASKED_ZIP.matcher(trimmed).matches()) {
            return trimmed.replaceAll("\\D", "");
        }
        return trimmed;
    }
}
