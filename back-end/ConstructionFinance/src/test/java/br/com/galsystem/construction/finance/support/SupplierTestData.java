package br.com.galsystem.construction.finance.support;

import br.com.galsystem.construction.finance.dto.supplier.SupplierCreateDTO;
import br.com.galsystem.construction.finance.models.Supplier;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Dados sintéticos compartilhados pelos testes de endereço do fornecedor (HU-1.1).
 */
public final class SupplierTestData {

    public static final String[] ADDRESS_FIELDS = {
            "zipCode", "street", "number", "complement", "neighborhood", "city", "state", "country", "reference"
    };

    private SupplierTestData() {
    }

    public static String repeat(final int size) {
        return "a".repeat(size);
    }

    /** Fornecedor "Construtora Beta" com endereço completo e válido. */
    public static Supplier supplierWithAddress() {
        return Supplier.builder()
                .id(1L)
                .name("Construtora Beta")
                .worker(false)
                .keyPix("pix-beta")
                .zipCode("01310100")
                .street("Avenida Paulista")
                .number("1000")
                .complement("Sala 10")
                .neighborhood("Bela Vista")
                .city("Sao Paulo")
                .state("SP")
                .country("BR")
                .reference("Proximo ao metro")
                .build();
    }

    /** Fornecedor antigo, sem nenhum campo de endereço. */
    public static Supplier supplierWithoutAddress() {
        return Supplier.builder()
                .id(2L)
                .name("Fornecedor Antigo Ltda")
                .worker(true)
                .keyPix("pix-antigo")
                .build();
    }

    public static Map<String, String> addressOf(final Supplier s) {
        final Map<String, String> m = new LinkedHashMap<>();
        m.put("zipCode", s.getZipCode());
        m.put("street", s.getStreet());
        m.put("number", s.getNumber());
        m.put("complement", s.getComplement());
        m.put("neighborhood", s.getNeighborhood());
        m.put("city", s.getCity());
        m.put("state", s.getState());
        m.put("country", s.getCountry());
        m.put("reference", s.getReference());
        return m;
    }

    public static Builder create() {
        return new Builder();
    }

    /** Builder de {@link SupplierCreateDTO} com nome e worker válidos por padrão. */
    public static final class Builder {
        private String name = "Construtora Beta";
        private Boolean worker = false;
        private String keyPix;
        private String zipCode;
        private String street;
        private String number;
        private String complement;
        private String neighborhood;
        private String city;
        private String state;
        private String country;
        private String reference;

        public Builder name(final String v) {
            this.name = v;
            return this;
        }

        public Builder worker(final Boolean v) {
            this.worker = v;
            return this;
        }

        public Builder keyPix(final String v) {
            this.keyPix = v;
            return this;
        }

        public Builder zipCode(final String v) {
            this.zipCode = v;
            return this;
        }

        public Builder street(final String v) {
            this.street = v;
            return this;
        }

        public Builder number(final String v) {
            this.number = v;
            return this;
        }

        public Builder complement(final String v) {
            this.complement = v;
            return this;
        }

        public Builder neighborhood(final String v) {
            this.neighborhood = v;
            return this;
        }

        public Builder city(final String v) {
            this.city = v;
            return this;
        }

        public Builder state(final String v) {
            this.state = v;
            return this;
        }

        public Builder country(final String v) {
            this.country = v;
            return this;
        }

        public Builder reference(final String v) {
            this.reference = v;
            return this;
        }

        public Builder fullAddress() {
            return zipCode("01310100").street("Avenida Paulista").number("1000").complement("Sala 10")
                    .neighborhood("Bela Vista").city("Sao Paulo").state("SP").country("BR")
                    .reference("Proximo ao metro");
        }

        public SupplierCreateDTO build() {
            return new SupplierCreateDTO(name, worker, keyPix, zipCode, street, number, complement,
                    neighborhood, city, state, country, reference);
        }
    }
}
