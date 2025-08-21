package br.com.galsystem.construction.finance.utils;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class StringToLocalDateConverter implements Converter<String, LocalDate> {

    @Override
    public LocalDate convert(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }

        try {
            // 🔹 Tenta converter ISO (yyyy-MM-dd)
            return LocalDate.parse(source, DateTimeFormatter.ISO_DATE);
        } catch (Exception ignored) {}

        try {
            // 🔹 Tenta converter BR (dd/MM/yyyy)
            return LocalDate.parse(source, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception ignored) {}

        try {
            // 🔹 Tenta converter formato do JS (Mon Aug 18 2025 ...)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy HH:mm:ss 'GMT'Z '('zzzz')'", Locale.ENGLISH);
            return LocalDate.parse(source, formatter);
        } catch (Exception ignored) {}

        throw new IllegalArgumentException("Formato de data inválido: " + source);
    }
}
