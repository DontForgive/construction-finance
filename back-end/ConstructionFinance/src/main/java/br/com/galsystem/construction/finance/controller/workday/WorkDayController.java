package br.com.galsystem.construction.finance.controller.workday;


import br.com.galsystem.construction.finance.dto.workday.WorkDayCreateDTO;
import br.com.galsystem.construction.finance.dto.workday.WorkDayDTO;
import br.com.galsystem.construction.finance.dto.workday.WorkDayUpdateDTO;
import br.com.galsystem.construction.finance.response.Response;
import br.com.galsystem.construction.finance.service.workday.WorkDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/work-days")
@RequiredArgsConstructor
public class WorkDayController {

    private final WorkDayService workDayService;

    @PostMapping
    public ResponseEntity<Response<WorkDayDTO>> create(@RequestBody WorkDayCreateDTO dto) {
        WorkDayDTO created = workDayService.create(dto);
        Response<WorkDayDTO> response = new Response<>();
        response.setStatus(HttpStatus.CREATED.value());
        response.setMessage("Dia de trabalho registrado com sucesso");
        response.setData(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
