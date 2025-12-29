package br.com.galsystem.construction.finance.service.supplier;

import br.com.galsystem.construction.finance.dto.supplier.SupplierCreateDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupplierService {
    Page<SupplierDTO> listar(String name, Boolean worker, Pageable pageable);

    SupplierDTO findById(Long id);

    SupplierDTO findOrCreateByName(String name);

    SupplierDTO createAndCache(String name);

    SupplierDTO create(SupplierCreateDTO dto);

    SupplierDTO update(Long id, SupplierUpdateDTO dto);

    void delete(Long id);
}
