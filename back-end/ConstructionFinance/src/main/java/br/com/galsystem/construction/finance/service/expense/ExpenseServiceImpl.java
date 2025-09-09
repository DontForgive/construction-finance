package br.com.galsystem.construction.finance.service.expense;

import br.com.galsystem.construction.finance.dto.category.CategoryCreateDTO;
import br.com.galsystem.construction.finance.dto.category.CategoryDTO;
import br.com.galsystem.construction.finance.dto.expense.ExpenseCreateDTO;
import br.com.galsystem.construction.finance.dto.expense.ExpenseDTO;
import br.com.galsystem.construction.finance.dto.expense.ExpenseUpdateDTO;
import br.com.galsystem.construction.finance.dto.payer.PayerCreateDTO;
import br.com.galsystem.construction.finance.dto.payer.PayerDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierCreateDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDTO;
import br.com.galsystem.construction.finance.exception.ResourceNotFoundException;
import br.com.galsystem.construction.finance.files.UploadArea;
import br.com.galsystem.construction.finance.mapper.CategoryMapper;
import br.com.galsystem.construction.finance.mapper.ExpenseMapper;
import br.com.galsystem.construction.finance.mapper.PayerMapper;
import br.com.galsystem.construction.finance.mapper.SupplierMapper;
import br.com.galsystem.construction.finance.models.*;
import br.com.galsystem.construction.finance.repository.*;
import br.com.galsystem.construction.finance.security.auth.CurrentUser;
import br.com.galsystem.construction.finance.service.file.FileStorageService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


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
    private final PayerMapper payerMapper;
    private final CategoryMapper categoryMapper;
    private final CurrentUser currentUser;
    private final FileStorageService storageService;
    private EntityManager em;


    @Override
    @Transactional(readOnly = true)
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
        return supplierRepository.findByNameIgnoreCase(name)
                .map(supplierMapper::toDTO)
                .orElseGet(() -> {
                    final SupplierCreateDTO dto = new SupplierCreateDTO(name);
                    final Supplier newSupplier = supplierMapper.toEntity(dto);
                    final Supplier saved = supplierRepository.save(newSupplier);
                    return supplierMapper.toDTO(saved);
                });

    }

    public PayerDTO findOrCreatePayerByName(final String name) {
        return payerRepository.findByNameIgnoreCase(name)
                .map(payerMapper::toDTO)
                .orElseGet(() -> {
                    final PayerCreateDTO dto = new PayerCreateDTO(name);
                    final Payer newPayer = payerMapper.toEntity(dto);
                    final Payer saved = payerRepository.save(newPayer);
                    return payerMapper.toDTO(saved);
                });
    }

    public CategoryDTO findOrCreateCategoryByName(final String name) {
        return categoryRepository.findByNameIgnoreCase(name)
                .map(categoryMapper::toDTO)
                .orElseGet(() -> {
                    final CategoryCreateDTO dto = new CategoryCreateDTO(name, null);
                    final Category newCategory = categoryMapper.toEntity(dto);
                    final Category saved = categoryRepository.save(newCategory);
                    return categoryMapper.toDTO(saved);
                });
    }


    @Override
    @Transactional
    public List<ExpenseCreateDTO> ExpenseCreateByFileDTO(final MultipartFile file) {
        final List<ExpenseCreateDTO> expenses = new ArrayList<>();

        final Long uid = currentUser.id();
        final User user = userRepository.findById(uid)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado"));

        final int BATCH_SIZE = 1000;
        final List<Expense> buffer = new ArrayList<>(BATCH_SIZE);

        try (final InputStream inputStream = file.getInputStream();
             final Workbook workbook = new XSSFWorkbook(inputStream)) {

            final Sheet sheet = workbook.getSheetAt(0);

            for (final Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                final String supplierName = row.getCell(2).getStringCellValue();
                final SupplierDTO supplier = findOrCreateSupplierByName(supplierName);

                final String payerName = row.getCell(3).getStringCellValue();
                final PayerDTO payer = findOrCreatePayerByName(payerName);

                final String categoryName = row.getCell(4).getStringCellValue();
                final CategoryDTO category = findOrCreateCategoryByName(categoryName);

                final ExpenseCreateDTO dto = new ExpenseCreateDTO(
                        row.getCell(0).getLocalDateTimeCellValue().toLocalDate(),
                        row.getCell(1).getStringCellValue(),
                        supplier.id(),
                        payer.id(),
                        category.id(),
                        row.getCell(5).getStringCellValue(),
                        BigDecimal.valueOf(row.getCell(6).getNumericCellValue()),
                        row.getCell(7).getStringCellValue()
                );
                expenses.add(dto);

                final Expense entity = mapper.toEntity(dto);
                entity.setUser(user);

                if (dto.supplierId() != null) {
                    entity.setSupplier(supplierRepository.getReferenceById(dto.supplierId()));
                }
                if (dto.payerId() != null) {
                    entity.setPayer(payerRepository.getReferenceById(dto.payerId()));
                }
                if (dto.categoryId() != null) {
                    entity.setCategory(categoryRepository.getReferenceById(dto.categoryId()));
                }

                buffer.add(entity);

                if (buffer.size() >= BATCH_SIZE) {
                    repository.saveAll(buffer);
                    repository.flush();
                    buffer.clear();
                }
            }

            if (!buffer.isEmpty()) {
                repository.saveAll(buffer);
                repository.flush();
                buffer.clear();
            }

        } catch (final IOException e) {
            throw new RuntimeException("Erro ao processar o arquivo Excel", e);
        }

        return expenses;
    }


    @Override
    public byte[] generateExpensesTemplate() {

        try (final Workbook wb = new XSSFWorkbook(); final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final Sheet sheet = wb.createSheet("Despesas");

            final CreationHelper helper = wb.getCreationHelper();
            final CellStyle headerStyle = wb.createCellStyle();
            final Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            final CellStyle dateStyle = wb.createCellStyle();
            final short dateFormat = helper.createDataFormat().getFormat("yyyy-mm-dd");
            dateStyle.setDataFormat(dateFormat);

            final CellStyle moneyStyle = wb.createCellStyle();
            final short moneyFormat = helper.createDataFormat().getFormat("#,##0.00");
            moneyStyle.setDataFormat(moneyFormat);

            // Cabeçalho
            final Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Data (yyyy-MM-dd)");
            header.createCell(1).setCellValue("Descrição");
            header.createCell(2).setCellValue("Fornecedor (nome)");
            header.createCell(3).setCellValue("Pagador (nome)");
            header.createCell(4).setCellValue("Categoria (nome)");
            header.createCell(5).setCellValue("Método de Pagamento");
            header.createCell(6).setCellValue("Valor (numérico)");
            header.createCell(7).setCellValue("URL do Anexo (opcional)");
            for (int i = 0; i <= 7; i++) header.getCell(i).setCellStyle(headerStyle);

            //DADOS DE EXEMPLO - 2 LINHAS
            // LINHA 1
            for (int i = 1; i <= 20000; i++) {
                final Row r1 = sheet.createRow(i);
                final Cell cDate1 = r1.createCell(0);
                cDate1.setCellValue(java.sql.Date.valueOf(java.time.LocalDate.of(2025, (int) (Math.random() * 12) + 1, (int) (Math.random() * 28) + 1)));
                cDate1.setCellStyle(dateStyle);

                r1.createCell(1).setCellValue("Compra de material - " + String.valueOf(i));
                r1.createCell(2).setCellValue(Math.random() > 0.5 ? "ConstruMais" : "MaisConstru");
                r1.createCell(3).setCellValue(Math.random() > 0.5 ? "João Silva" : "Pedro Santos");
                r1.createCell(4).setCellValue(Math.random() > 0.5 ? "Construção" : "Reforma");
                r1.createCell(5).setCellValue(Math.random() > 0.5 ? "Cartão" : "Dinheiro");

                final Cell cVal1 = r1.createCell(6);
                cVal1.setCellValue(new java.math.BigDecimal(String.format("%.2f", Math.random() * 5000)).doubleValue());
                cVal1.setCellStyle(moneyStyle);

                r1.createCell(7).setCellValue(Math.random() > 0.7 ? "https://exemplo.com/nota" + i + ".pdf" : "");

            }

            for (int i = 0; i <= 7; i++) sheet.autoSizeColumn(i);

            wb.write(out);
            return out.toByteArray();
        } catch (final Exception e) {
            throw new RuntimeException("Erro ao gerar template de despesas", e);
        }
    }


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
