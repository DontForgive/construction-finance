package br.com.galsystem.construction.finance.dto.supplier;

/**
 * Item da listagem de fornecedores (GET /supplier), com os campos de endereço.
 * {@link SupplierDTO} continua sendo usado por criação, alteração e findOrCreateByName.
 */
public record SupplierListDTO(
        Long id,
        String name,
        String keyPix,
        Boolean worker,
        String zipCode,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String phone
) {
}
