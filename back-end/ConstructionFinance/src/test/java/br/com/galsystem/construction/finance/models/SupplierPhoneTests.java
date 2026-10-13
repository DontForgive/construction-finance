package br.com.galsystem.construction.finance.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;

class SupplierPhoneTests {

    // CT-1.1 (CA2): campo phone com @Column(length = 50) e nullable
    @Test
    void ct1_1_campoPhoneTemLimite50ENaoEhObrigatorio() throws NoSuchFieldException {
        var field = Supplier.class.getDeclaredField("phone");
        Column column = field.getAnnotation(Column.class);

        assertTrue(column != null, "campo phone deve ter @Column");
        assertEquals(50, column.length());
        assertTrue(column.nullable(), "phone deve ser nullable");
        assertEquals(String.class, field.getType());
    }

    // CT-1.2 (CA2): builder sem telefone deixa phone nulo; com telefone, devolve o mesmo valor
    @Test
    void ct1_2_builderSemTelefoneDeixaNuloEComTelefoneDevolveValor() {
        Supplier semTelefone = Supplier.builder().name("Fornecedor A").build();
        assertNull(semTelefone.getPhone());

        Supplier comTelefone = Supplier.builder().name("Fornecedor B").phone("11999998888").build();
        assertEquals("11999998888", comTelefone.getPhone());
    }
}
