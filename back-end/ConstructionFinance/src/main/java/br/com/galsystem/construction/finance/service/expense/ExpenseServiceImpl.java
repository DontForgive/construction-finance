package br.com.galsystem.construction.finance.service.expense;
import br.com.galsystem.construction.finance.dto.category.CategoryDTO;
import br.com.galsystem.construction.finance.dto.expense.ExpenseCreateDTO;
import br.com.galsystem.construction.finance.dto.expense.ExpenseDTO;
import br.com.galsystem.construction.finance.dto.expense.ExpenseUpdateDTO;
import br.com.galsystem.construction.finance.dto.payer.PayerDTO;
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
import br.com.galsystem.construction.finance.service.category.CategoryService;
import br.com.galsystem.construction.finance.service.file.FileStorageService;
import br.com.galsystem.construction.finance.service.payer.PayerService;
import br.com.galsystem.construction.finance.service.supplier.SupplierService;
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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
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
    private final SupplierService supplierService;
    private final PayerService payerService;
    private final CategoryService categoryService;




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
        return supplierService.findOrCreateByName(name);
    }

    public PayerDTO findOrCreatePayerByName(final String name) {
        return payerService.findOrCreateByName(name);
    }

    public CategoryDTO findOrCreateCategoryByName(final String name) {
        return categoryService.findOrCreateByName(name);
    }



    @Override
    @Transactional(readOnly = true)
    public List<ExpenseCreateDTO> ExpenseCreateByFileDTO(final MultipartFile file) {
        final List<ExpenseCreateDTO> items = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // cabeçalho
            if (header == null) {
                return items;
            }
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] cols = line.split(";");
                LocalDate date = LocalDate.parse(cols[0].trim());
                String description = cols[1].trim();
                String supplierName = cols[2].trim();
                String payerName = cols[3].trim();
                String categoryName = cols[4].trim();
                String paymentMethod = cols[5].trim();
                BigDecimal amount = new BigDecimal(cols[6].trim());
                String attachmentUrl = cols.length > 7 ? cols[7].trim() : null;

                Long supplierId = supplierName.isEmpty() ? null : findOrCreateSupplierByName(supplierName).id();
                Long payerId = payerName.isEmpty() ? null : findOrCreatePayerByName(payerName).id();
                Long categoryId = categoryName.isEmpty() ? null : findOrCreateCategoryByName(categoryName).id();

                items.add(new ExpenseCreateDTO(
                        date,
                        description,
                        supplierId,
                        payerId,
                        categoryId,
                        paymentMethod,
                        amount,
                        attachmentUrl
                ));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Falha ao ler arquivo de despesas: " + e.getMessage(), e);
        }
        return items;
    }



    @Override
    public byte[] generateExpensesTemplate() {

        try(Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()){
            Sheet sheet = wb.createSheet("Despesas");

            CreationHelper helper = wb.getCreationHelper();
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            CellStyle dateStyle = wb.createCellStyle();
            short dateFormat = helper.createDataFormat().getFormat("yyyy-mm-dd");
            dateStyle.setDataFormat(dateFormat);

            CellStyle moneyStyle = wb.createCellStyle();
            short moneyFormat = helper.createDataFormat().getFormat("#,##0.00");
            moneyStyle.setDataFormat(moneyFormat);

            // Cabeçalho
            Row header = sheet.createRow(0);
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
            for (int i = 1; i <= 100000; i += 2) {
                Row r1 = sheet.createRow(i);
                Cell cDate1 = r1.createCell(0);
                cDate1.setCellValue(java.sql.Date.valueOf(java.time.LocalDate.of(2025, 1, 15)));
                cDate1.setCellStyle(dateStyle);

                r1.createCell(1).setCellValue("Compra de material");
                r1.createCell(2).setCellValue("ConstruMais");
                r1.createCell(3).setCellValue("João Silva");
                r1.createCell(4).setCellValue("Construção");
                r1.createCell(5).setCellValue("Cartão");

                Cell cVal1 = r1.createCell(6);
                cVal1.setCellValue(new java.math.BigDecimal("1234.56").doubleValue());
                cVal1.setCellStyle(moneyStyle);

                r1.createCell(7).setCellValue("https://exemplo.com/nota1.pdf");

                // LINHA 2
                Row r2 = sheet.createRow(i + 1);
                Cell cDate2 = r2.createCell(0);
                cDate2.setCellValue(java.sql.Date.valueOf(java.time.LocalDate.of(2025, 1, 20)));
                cDate2.setCellStyle(dateStyle);

                r2.createCell(1).setCellValue("Serviço de transporte");
                r2.createCell(2).setCellValue("TransRápido");
                r2.createCell(3).setCellValue("Maria Souza");
                r2.createCell(4).setCellValue("Logística");
                r2.createCell(5).setCellValue("Pix");

                Cell cVal2 = r2.createCell(6);
                cVal2.setCellValue(new java.math.BigDecimal("350.00").doubleValue());
                cVal2.setCellStyle(moneyStyle);

                r2.createCell(7).setCellValue("");
            }

            for (int i = 0; i <= 7; i++) sheet.autoSizeColumn(i);

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
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
