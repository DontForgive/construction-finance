package br.com.galsystem.construction.finance.dto.supplier;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

/**
 * Valor de um campo de atualização parcial. Distingue três estados:
 * campo omitido (ausente), campo enviado como null e campo com valor.
 */
public final class CampoOpcional<T> {

    private final boolean presente;
    private final T valor;

    private CampoOpcional(boolean presente, T valor) {
        this.presente = presente;
        this.valor = valor;
    }

    /** Campo não enviado: o valor atual deve ser mantido. */
    public static <T> CampoOpcional<T> ausente() {
        return new CampoOpcional<>(false, null);
    }

    /** Campo enviado; o valor pode ser null (apagar). */
    public static <T> CampoOpcional<T> de(T valor) {
        return new CampoOpcional<>(true, valor);
    }

    public boolean isPresente() {
        return presente;
    }

    public T valor() {
        return valor;
    }

    /** Desserializador para campos de texto: omitido gera "ausente", null explícito gera "presente com null". */
    public static final class DeserializadorTexto extends StdDeserializer<CampoOpcional<String>> {

        public DeserializadorTexto() {
            super(CampoOpcional.class);
        }

        @Override
        public CampoOpcional<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return CampoOpcional.de(ctxt.readValue(p, String.class));
        }

        @Override
        public CampoOpcional<String> getNullValue(DeserializationContext ctxt) {
            return CampoOpcional.de(null);
        }

        @Override
        public CampoOpcional<String> getAbsentValue(DeserializationContext ctxt) {
            return CampoOpcional.ausente();
        }
    }
}
