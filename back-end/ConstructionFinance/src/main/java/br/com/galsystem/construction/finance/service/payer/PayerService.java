package br.com.galsystem.construction.finance.service.payer;

import br.com.galsystem.construction.finance.dto.payer.PayerCreateDTO;
import br.com.galsystem.construction.finance.dto.payer.PayerDTO;
import br.com.galsystem.construction.finance.dto.payer.PayerUpdateDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PayerService {

    Page<PayerDTO> listar(String name, Pageable pageable);
    PayerDTO findById(Long id);
    PayerDTO create(PayerCreateDTO dto);
    PayerDTO update(Long id, PayerUpdateDTO dto);
    void delete(Long id);
    PayerDTO findOrCreateByName(String name);
    PayerDTO createAndCache(String name);
}
