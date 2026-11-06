package br.com.galsystem.construction.finance.dto.supplier;

/**
 * Leitura de um fornecedor por id, com os campos de endereço. A listagem continua usando {@link SupplierDTO}.
 */
public record SupplierDetailDTO(
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
        String country,
        String reference
) {
}
