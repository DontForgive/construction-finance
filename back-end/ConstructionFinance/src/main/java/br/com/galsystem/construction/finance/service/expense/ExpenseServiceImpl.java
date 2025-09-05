package br.com.galsystem.construction.finance.service.expense;

import br.com.galsystem.construction.finance.dto.expense.ExpenseCreateDTO;
import br.com.galsystem.construction.finance.dto.expense.ExpenseDTO;
import br.com.galsystem.construction.finance.dto.expense.ExpenseUpdateDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDTO;
import br.com.galsystem.construction.finance.exception.ResourceNotFoundException;
import br.com.galsystem.construction.finance.files.UploadArea;
import br.com.galsystem.construction.finance.mapper.ExpenseMapper;
import br.com.galsystem.construction.finance.mapper.SupplierMapper;
import br.com.galsystem.construction.finance.models.*;
import br.com.galsystem.construction.finance.repository.*;
import br.com.galsystem.construction.finance.security.auth.CurrentUser;
import br.com.galsystem.construction.finance.service.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.cache.annotation.Cacheable;
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
    private final SupplierMapper supplierMapper;
    private final CurrentUser currentUser;
    private final FileStorageService storageService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable("ExpenseList")
    public Page<ExpenseDTO> list(
            final String description,
            final Long supplierId,
            final Long payerId,
            final Long categoryId,
            String paymentMethod,
            final LocalDate startDate,
            final LocalDate endDateDate,
            final Pageable pageable
    ) {
        if (paymentMethod != null && paymentMethod.isBlank()) {
            paymentMethod = null;
        }

        return repository.findByFilters(description, supplierId, payerId, categoryId, paymentMethod, startDate, endDateDate, pageable)
                .map(mapper::toDTO);
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable("ExpenseFindById")
    public ExpenseDTO findById(final Long id) {
        final Expense entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Despesa com ID %d não encontrada".formatted(id)));
        return mapper.toDTO(entity);
    }


    @Override
    @Transactional
    public ExpenseDTO create(final ExpenseCreateDTO dto) {
        final Expense entity = mapper.toEntity(dto);

        if (dto.supplierId() != null) {
            final Supplier supplier = supplierRepository.findById(dto.supplierId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Fornecedor com ID %d não encontrado".formatted(dto.supplierId())));
            entity.setSupplier(supplier);
        }

        if (dto.payerId() != null) {
            final Payer payer = payerRepository.findById(dto.payerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Pagador com ID %d não encontrado".formatted(dto.payerId())));
            entity.setPayer(payer);
        }

        if (dto.categoryId() != null) {
            final Category category = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Categoria com ID %d não encontrada".formatted(dto.categoryId())));
            entity.setCategory(category);
        }

        // >>> usuário do token (NÃO aceite userId no JSON)
        final Long uid = currentUser.id();
        final User user = userRepository.findById(uid)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado"));
        entity.setUser(user);

        final Expense saved = repository.save(entity);
        return mapper.toDTO(saved);
    }


    @Override
    @Transactional
    public ExpenseDTO update(final Long id, final ExpenseUpdateDTO dto) {
        final Expense entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Despesa com ID %d não encontrada".formatted(id)));

        mapper.updateEntity(entity, dto);

        if (dto.supplierId() != null) {
            final Supplier supplier = supplierRepository.findById(dto.supplierId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Fornecedor com ID %d não encontrado".formatted(dto.supplierId())));
            entity.setSupplier(supplier);
        }

        if (dto.payerId() != null) {
            final Payer payer = payerRepository.findById(dto.payerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Pagador com ID %d não encontrado".formatted(dto.payerId())));
            entity.setPayer(payer);
        }

        if (dto.categoryId() != null) {
            final Category category = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Categoria com ID %d não encontrada".formatted(dto.categoryId())));
            entity.setCategory(category);
        }

        final Expense saved = repository.save(entity);
        return mapper.toDTO(saved);
    }


    @Override
    @Transactional
    public void delete(final Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Despesa com ID %d não encontrada".formatted(id));
        }
        repository.deleteById(id);
    }


    @Override
    @Transactional
    public ExpenseDTO attachFile(final Long id, final MultipartFile file) {
        final Expense entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa com ID %d não encontrada".formatted(id)));

        final Long uid = currentUser.id();
        if (!entity.getUser().getId().equals(uid)) {
            throw new AccessDeniedException("Acesso negado");
        }

        if (entity.getAttachmentUrl() != null && !entity.getAttachmentUrl().isBlank()) {
            storageService.deleteByPublicUrl(entity.getAttachmentUrl());
        }

        final String url = storageService.store(UploadArea.EXPENSES, file);
        entity.setAttachmentUrl(url);

        final Expense saved = repository.save(entity);
        return mapper.toDTO(saved);
    }


    @Override
    @Transactional
    public void removeAttachment(final Long id) {
        final Expense entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa com ID %d não encontrada".formatted(id)));

        final Long uid = currentUser.id();
        if (!entity.getUser().getId().equals(uid)) {
            throw new AccessDeniedException("Acesso negado");
        }

        if (entity.getAttachmentUrl() != null && !entity.getAttachmentUrl().isBlank()) {
            storageService.deleteByPublicUrl(entity.getAttachmentUrl());
            entity.setAttachmentUrl(null);
            repository.save(entity);
        }
    }

    public SupplierDTO findOrCreateSupplierByName(final String name) {
//        return supplierRepository.findByNameIgnoreCase(name)
//
//                .orElseGet(() -> {
//                    final SupplierCreateDTO dto = new SupplierCreateDTO(name);
//                    final Supplier newSupplier = supplierMapper.toEntity(dto);
//                    final Supplier saved = supplierRepository.save(newSupplier);
//                    return supplierMapper.toDTO(saved);
//                });
        return null;
    }

//    public Payer findOrCreatePayerByName(final String name) {
//        return payerRepository.findByNameIgnoreCase(name)
//                .orElseGet(() -> payerRepository.save(new Payer(name)));
//    }
//
//    public Category findOrCreateCategoryByName(final String name) {
//        return categoryRepository.findByNameIgnoreCase(name)
//                .orElseGet(() -> categoryRepository.save(new Category(name)));
//    }

//    @Override
//    public List<ExpenseCreateDTO> ExpenseCreateByFileDTO(final MultipartFile file) {
//        final List<ExpenseCreateDTO> expenses = new ArrayList<>();
//
//        try (final InputStream inputStream = file.getInputStream();
//             final Workbook workbook = new XSSFWorkbook(inputStream)) {
//
//            final Sheet sheet = workbook.getSheetAt(0);
//
//            for (final Row row : sheet) {
//                if (row.getRowNum() == 0) continue;
//
//                final String supplierName = row.getCell(2).getStringCellValue();
//                final SupplierCreateDTO supplier = findOrCreateSupplierByName(supplierName);
//
//                final String payerName = row.getCell(3).getStringCellValue();
//                final Payer payer = findOrCreatePayerByName(payerName);
//
//                final String categoryName = row.getCell(4).getStringCellValue();
//                final Category category = findOrCreateCategoryByName(categoryName);
//
//
//                final ExpenseCreateDTO expense = new ExpenseCreateDTO(
//                        row.getCell(0).getLocalDateTimeCellValue().toLocalDate(),
//                        row.getCell(1).getStringCellValue(),
//                        supplier.getId(),
//                        payer.getId(),
//                        category.getId(),
//                        row.getCell(5).getStringCellValue(),
//                        BigDecimal.valueOf(row.getCell(6).getNumericCellValue()),
//                        row.getCell(7).getStringCellValue()
//                );
//
//                expenses.add(expense);

    /// /                mapper.toEntity(expense);
//
//            }
//
//        } catch (final IOException e) {
//            throw new RuntimeException("Erro ao processar o arquivo Excel", e);
//        }
//
//        return expenses;
//    }
    private Long getLongValue(final Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> (long) cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Long.parseLong(cell.getStringCellValue());
                } catch (final NumberFormatException e) {
                    yield null;
                }
            }
            default -> null;
        };
    }


}
