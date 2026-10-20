package br.com.galsystem.construction.finance.dto.supplier;

public record SupplierDTO(
        Long id,
        String name,
        String keyPix,
        Boolean worker,
        String rua,
        String numero,
        String bairro,
        String cidade,
        String estado,
        String cep,
        String complemento
) {
}
