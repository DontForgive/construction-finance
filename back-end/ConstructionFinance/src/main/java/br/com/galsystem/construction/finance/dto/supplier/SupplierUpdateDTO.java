package br.com.galsystem.construction.finance.dto.supplier;

import br.com.galsystem.construction.finance.utils.AddressNormalizer;
import br.com.galsystem.construction.finance.validation.PaisValido;
import br.com.galsystem.construction.finance.validation.UfValida;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

/**
 * DTO de atualização do fornecedor.
 * O Jackson só chama o setter quando a chave existe no JSON. Os setters de endereço registram
 * a presença do campo, o que distingue "ausente" (mantém o valor) de "null" ou "" (limpa o valor).
 */
public class SupplierUpdateDTO {

    public static final String ZIP_CODE = "zipCode";
    public static final String STREET = "street";
    public static final String NUMBER = "number";
    public static final String COMPLEMENT = "complement";
    public static final String NEIGHBORHOOD = "neighborhood";
    public static final String CITY = "city";
    public static final String STATE = "state";
    public static final String COUNTRY = "country";
    public static final String REFERENCE = "reference";

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
    @Size(min = 2, message = "O nome deve ter no minimo 2 caracteres")
    private String name;

    @NotNull(message = "Trabalhador não pode ser nulo ")
    private Boolean worker;

    private String keyPix;

    @Pattern(regexp = "\\d{8}", message = "CEP deve conter exatamente 8 dígitos numéricos")
    private String zipCode;

    @Size(max = 255, message = "Logradouro deve ter no máximo 255 caracteres")
    private String street;

    @Size(max = 20, message = "Número deve ter no máximo 20 caracteres")
    private String number;

    @Size(max = 150, message = "Complemento deve ter no máximo 150 caracteres")
    private String complement;

    @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres")
    private String neighborhood;

    @Size(max = 120, message = "Cidade deve ter no máximo 120 caracteres")
    private String city;

    @UfValida(message = "UF inválida: informe uma das 27 unidades federativas (2 letras)")
    private String state;

    @PaisValido(message = "País inválido: informe um código ISO 3166-1 alpha-2 (2 letras)")
    private String country;

    @Size(max = 255, message = "Referência deve ter no máximo 255 caracteres")
    private String reference;

    @JsonIgnore
    private final Set<String> presentFields = new HashSet<>();

    public SupplierUpdateDTO() {
    }

    public SupplierUpdateDTO(final String name, final Boolean worker, final String keyPix) {
        this.name = name;
        this.worker = worker;
        this.keyPix = keyPix;
    }

    /** Indica se o campo de endereço veio no JSON (mesmo que com null ou vazio). */
    public boolean isPresent(final String field) {
        return presentFields.contains(field);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Boolean getWorker() {
        return worker;
    }

    public void setWorker(final Boolean worker) {
        this.worker = worker;
    }

    public String getKeyPix() {
        return keyPix;
    }

    public void setKeyPix(final String keyPix) {
        this.keyPix = keyPix;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(final String zipCode) {
        this.zipCode = AddressNormalizer.zipCode(zipCode);
        presentFields.add(ZIP_CODE);
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(final String street) {
        this.street = AddressNormalizer.text(street);
        presentFields.add(STREET);
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = AddressNormalizer.text(number);
        presentFields.add(NUMBER);
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(final String complement) {
        this.complement = AddressNormalizer.text(complement);
        presentFields.add(COMPLEMENT);
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(final String neighborhood) {
        this.neighborhood = AddressNormalizer.text(neighborhood);
        presentFields.add(NEIGHBORHOOD);
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = AddressNormalizer.text(city);
        presentFields.add(CITY);
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = AddressNormalizer.upper(state);
        presentFields.add(STATE);
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = AddressNormalizer.upper(country);
        presentFields.add(COUNTRY);
    }

    public String getReference() {
        return reference;
    }

    public void setReference(final String reference) {
        this.reference = AddressNormalizer.text(reference);
        presentFields.add(REFERENCE);
    }
}
