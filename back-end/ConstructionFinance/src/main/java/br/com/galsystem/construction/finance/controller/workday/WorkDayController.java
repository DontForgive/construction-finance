package br.com.galsystem.construction.finance.controller.workday;


import br.com.galsystem.construction.finance.dto.workday.WorkDayBulkPaymentDTO;
import br.com.galsystem.construction.finance.dto.workday.WorkDayCreateDTO;
import br.com.galsystem.construction.finance.dto.workday.WorkDayDTO;
import br.com.galsystem.construction.finance.dto.workday.WorkDayUpdateDTO;
import br.com.galsystem.construction.finance.response.Response;
import br.com.galsystem.construction.finance.service.workday.WorkDayService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/work-days")
@RequiredArgsConstructor
@Slf4j
public class WorkDayController {

    private final WorkDayService workDayService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<Response<WorkDayDTO>> create(@RequestBody WorkDayCreateDTO dto) {
        WorkDayDTO created = workDayService.create(dto);
        Response<WorkDayDTO> response = new Response<>();
        response.setStatus(HttpStatus.CREATED.value());
        response.setMessage("Dia de trabalho registrado com sucesso");
        response.setData(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(
            value = "/pay-bulk",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Response<Void>> payBulk(
            @RequestParam("workdayIds") String workdayIdsJson,
            @RequestParam("supplierId") Long supplierId,
            @RequestParam("description") String description,
            @RequestParam("paymentDate") String paymentDate,
            @RequestParam("payerId") Long payerId,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "serviceContractId", required = false) Long serviceContractId,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam("amount") Double amount,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws Exception {

        List<Long> workdayIds = objectMapper.readValue(workdayIdsJson, new TypeReference<List<Long>>() {
        });

        WorkDayBulkPaymentDTO dto = new WorkDayBulkPaymentDTO(
                workdayIds,
                supplierId,
                description,
                java.time.LocalDate.parse(paymentDate),
                payerId,
                categoryId,
                serviceContractId,
                paymentMethod,
                amount
        );

        workDayService.registerBulkPayment(dto, file);

        return ResponseEntity.ok(new Response<>(200, "Pagamento em lote registrado com sucesso", null, null));
    }

    @PostMapping("/pay")
    public ResponseEntity<Response<Void>> pay(@RequestBody WorkDayBulkPaymentDTO dto) {
        workDayService.registerBulkPayment(dto, null);
        return ResponseEntity.ok(new Response<>(200, "Pagamento registrado com sucesso", null, null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<WorkDayDTO>> update(@PathVariable Long id, @RequestBody WorkDayUpdateDTO dto) {
        WorkDayDTO updated = workDayService.update(id, dto);
        Response<WorkDayDTO> response = new Response<>();
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Registro atualizado com sucesso");
        response.setData(updated);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Response<List<WorkDayDTO>>> list(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Long supplierId) {

        List<WorkDayDTO> list = workDayService.findByMonth(year, month, supplierId);
        Response<List<WorkDayDTO>> response = new Response<>();
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Registros encontrados");
        response.setData(list);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> delete(@PathVariable Long id) {
        workDayService.delete(id);
        Response<Void> response = new Response<>();
        response.setStatus(HttpStatus.NO_CONTENT.value());
        response.setMessage("Registro removido com sucesso");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

}
