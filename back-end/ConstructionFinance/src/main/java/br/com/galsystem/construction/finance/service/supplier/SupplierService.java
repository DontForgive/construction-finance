package br.com.galsystem.construction.finance.service.supplier;

import br.com.galsystem.construction.finance.dto.payer.PayerCreateDTO;
import br.com.galsystem.construction.finance.dto.payer.PayerDTO;
import br.com.galsystem.construction.finance.dto.payer.PayerUpdateDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDTO;
import br.com.galsystem.construction.finance.exception.ConflictException;
import br.com.galsystem.construction.finance.exception.ResourceNotFoundException;
import br.com.galsystem.construction.finance.models.Payer;
import br.com.galsystem.construction.finance.models.Supplier;
import br.com.galsystem.construction.finance.repository.SupplierRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    public Optional<Supplier> findById(Long id) {
        return supplierRepository.findById(id);
    }

    public Supplier save(Supplier supplier) {
        return supplierRepository.save(supplier);
    }


    @Transactional
    public SupplierDTO create(SupplierDTO dto) {
        String name = dto.getName().trim();

        if (supplierRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Já existe um fornecedor com o nome informado.");
        }

        Supplier entity = new Supplier();
        entity.setName(name);

        try {
            return toDTO(supplierRepository.save(entity));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Já existe um fornecedor com o nome informado.");
        }
    }

    @Transactional
    public SupplierDTO update(Long id, SupplierDTO dto) {
        Supplier entity = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor não encontrado."));

        String name = dto.getName().trim();

        if (supplierRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Já existe um fornecedor com o nome informado.");
        }

        entity.setName(name);

        try {
            return toDTO(supplierRepository.save(entity));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Ocorreu um erro ao editar: " + e.getMessage());
        }
    }

    private SupplierDTO toDTO(Supplier p) {
        SupplierDTO out = new SupplierDTO();
        out.setId(p.getId());
        out.setName(p.getName());
        return out;
    }


    public void deleteById(Long id) {
        supplierRepository.deleteById(id);
    }
}
