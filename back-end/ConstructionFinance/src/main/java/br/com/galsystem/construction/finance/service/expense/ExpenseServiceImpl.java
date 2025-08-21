package br.com.galsystem.construction.finance.service.expense;

import br.com.galsystem.construction.finance.dto.expense.ExpenseCreateDTO;
import br.com.galsystem.construction.finance.dto.expense.ExpenseDTO;
import br.com.galsystem.construction.finance.dto.expense.ExpenseUpdateDTO;
import br.com.galsystem.construction.finance.exception.ResourceNotFoundException;
import br.com.galsystem.construction.finance.files.UploadArea;
import br.com.galsystem.construction.finance.mapper.ExpenseMapper;
import br.com.galsystem.construction.finance.models.*;
import br.com.galsystem.construction.finance.repository.*;
import br.com.galsystem.construction.finance.security.auth.CurrentUser;
import br.com.galsystem.construction.finance.service.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository repository;
    private final SupplierRepository supplierRepository;
    private final PayerRepository payerRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseMapper mapper;
    private final CurrentUser currentUser;
    private final FileStorageService storageService;

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseDTO> list(
            String description,
            Long supplierId,
            Long payerId,
            Long categoryId,
            String paymentMethod,
            LocalDate startDate,
            LocalDate endDateDate,
            Pageable pageable
    ) {
        if (paymentMethod != null && paymentMethod.isBlank()) {
            paymentMethod = null;
        }

        return repository.findByFilters(description, supplierId, payerId, categoryId, paymentMethod, startDate, endDateDate, pageable)
                .map(mapper::toDTO);
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

        if (dto.categoryId() != null) {
            Category category = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Categoria com ID %d não encontrada".formatted(dto.categoryId())));
            entity.setCategory(category);
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

        mapper.updateEntity(entity, dto);

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

        if (dto.categoryId() != null) {
            Category category = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Categoria com ID %d não encontrada".formatted(dto.categoryId())));
            entity.setCategory(category);
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


    @Override
    @Transactional
    public ExpenseDTO attachFile(Long id, MultipartFile file) {
        Expense entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa com ID %d não encontrada".formatted(id)));

        // (opcional) garantir que a despesa pertence ao usuário autenticado
        Long uid = currentUser.id();
        if (!entity.getUser().getId().equals(uid)) {
            throw new AccessDeniedException("Acesso negado");
        }

        // apaga o arquivo antigo se existir
        if (entity.getAttachmentUrl() != null && !entity.getAttachmentUrl().isBlank()) {
            storageService.deleteByPublicUrl(entity.getAttachmentUrl());
        }

        // salva novo arquivo e atualiza a URL
        String url = storageService.store(UploadArea.EXPENSES, file);
        entity.setAttachmentUrl(url);

        Expense saved = repository.save(entity);
        return mapper.toDTO(saved);
    }


    @Override
    @Transactional
    public void removeAttachment(Long id) {
        Expense entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa com ID %d não encontrada".formatted(id)));

        Long uid = currentUser.id();
        if (!entity.getUser().getId().equals(uid)) {
            throw new AccessDeniedException("Acesso negado");
        }

        if (entity.getAttachmentUrl() != null && !entity.getAttachmentUrl().isBlank()) {
            storageService.deleteByPublicUrl(entity.getAttachmentUrl());
            entity.setAttachmentUrl(null);
            repository.save(entity);
        }
    }


}
