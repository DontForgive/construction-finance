package br.com.galsystem.construction.finance.service.serviceContract;

import br.com.galsystem.construction.finance.dto.serviceContract.ServiceContractCreateDTO;
import br.com.galsystem.construction.finance.dto.serviceContract.ServiceContractDTO;
import br.com.galsystem.construction.finance.dto.serviceContract.ServiceContractFilterDTO;
import br.com.galsystem.construction.finance.dto.serviceContract.ServiceContractUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface serviceContract {

    Optional<ServiceContractDTO> findById(Long id);

    ServiceContractDTO create(ServiceContractCreateDTO dto);

    ServiceContractDTO update(Long id, ServiceContractUpdateDTO dto);

    void delete(Long id);

    Page<ServiceContractDTO> listAll(Pageable pageable);

    Page<ServiceContractDTO> findByFilters(ServiceContractFilterDTO filters, Pageable pageable);

}
