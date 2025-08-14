package br.com.galsystem.construction.finance.service.expense;

import br.com.galsystem.construction.finance.dto.expense.ExpenseCreateDTO;
import br.com.galsystem.construction.finance.dto.expense.ExpenseDTO;
import br.com.galsystem.construction.finance.dto.expense.ExpenseUpdateDTO;
import br.com.galsystem.construction.finance.exception.ResourceNotFoundException;
import br.com.galsystem.construction.finance.mapper.ExpenseMapper;
import br.com.galsystem.construction.finance.models.Expense;
import br.com.galsystem.construction.finance.models.Payer;
import br.com.galsystem.construction.finance.models.Supplier;
import br.com.galsystem.construction.finance.models.User;
import br.com.galsystem.construction.finance.repository.ExpenseRepository;
import br.com.galsystem.construction.finance.repository.PayerRepository;
import br.com.galsystem.construction.finance.repository.SupplierRepository;
import br.com.galsystem.construction.finance.repository.UserRepository;
import br.com.galsystem.construction.finance.security.auth.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository repository;
    private final SupplierRepository supplierRepository;
    private final PayerRepository payerRepository;
    private final UserRepository userRepository;
    private final ExpenseMapper mapper;
    private final CurrentUser currentUser;

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseDTO> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);

    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDTO findById(Long id) {
        Expense entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Despesa com ID %d não encontrada".formatted(id)));
        return mapper.toDTO(entity);
    }


    @Override
    @Transactional
    public ExpenseDTO create(ExpenseCreateDTO dto) {
        Expense entity = mapper.toEntity(dto);

        if (dto.supplierId() != null) {
            Supplier supplier = supplierRepository.findById(dto.supplierId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Fornecedor com ID %d não encontrado".formatted(dto.supplierId())));
            entity.setSupplier(supplier);
        }

        if (dto.payerId() != null) {
            Payer payer = payerRepository.findById(dto.payerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Pagador com ID %d não encontrado".formatted(dto.payerId())));
            entity.setPayer(payer);
        }

        // >>> usuário do token (NÃO aceite userId no JSON)
        Long uid = currentUser.id();
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado"));
        entity.setUser(user);

        Expense saved = repository.save(entity);
        return mapper.toDTO(saved);
    }


    @Override
    @Transactional
    public ExpenseDTO update(Long id, ExpenseUpdateDTO dto) {
        Expense entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Despesa com ID %d não encontrada".formatted(id)));

        // Atualiza campos simples
        mapper.updateEntity(entity, dto);

        // Atualiza associações se enviado no DTO
        if (dto.supplierId() != null) {
            Supplier supplier = supplierRepository.findById(dto.supplierId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Fornecedor com ID %d não encontrado".formatted(dto.supplierId())));
            entity.setSupplier(supplier);
        }

        if (dto.payerId() != null) {
            Payer payer = payerRepository.findById(dto.payerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Pagador com ID %d não encontrado".formatted(dto.payerId())));
            entity.setPayer(payer);
        }

        Expense saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Despesa com ID %d não encontrada".formatted(id));
        }
        repository.deleteById(id);
    }
}
