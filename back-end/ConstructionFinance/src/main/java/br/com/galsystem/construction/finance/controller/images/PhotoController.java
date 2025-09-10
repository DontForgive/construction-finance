package br.com.galsystem.construction.finance.controller.images;


import br.com.galsystem.construction.finance.dto.images.PhotoDTO;
import br.com.galsystem.construction.finance.response.Response;
import br.com.galsystem.construction.finance.service.images.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<PhotoDTO>> upload(@RequestPart("file") final MultipartFile file) {
        return ResponseEntity.ok(new Response<>(200, "Foto enviada com sucesso", photoService.store(file), null));
    }

    @GetMapping
    public ResponseEntity<Response<List<Integer>>> listYears() {
        return ResponseEntity.ok(new Response<>(200, "Anos disponíveis", photoService.listYears(), null));
    }

    @GetMapping("/{year}")
    public ResponseEntity<Response<List<Integer>>> listMonths(@PathVariable final int year) {
        return ResponseEntity.ok(new Response<>(200, "Meses disponíveis", photoService.listMonths(year), null));
    }

    @GetMapping("/{year}/{month}")
    public ResponseEntity<Response<List<PhotoDTO>>> listPhotos(@PathVariable final int year, @PathVariable final int month) {
        return ResponseEntity.ok(new Response<>(200, "Fotos do período", photoService.listPhotos(year, month), null));
    }

    @GetMapping("/all")
    public ResponseEntity<Response<List<PhotoDTO>>> listAll() {
        return ResponseEntity.ok(new Response<>(200, "Todas as fotos", photoService.getAll(), null));
    }
 //int year, int month, String filename
    @DeleteMapping("/{year}/{month}/{name}")
    public ResponseEntity<Response<PhotoDTO>> removeFile(@PathVariable final int year, @PathVariable final int month, @PathVariable final String name) {
        return ResponseEntity.ok(new Response<>(200, "Arquivo Removido com Sucesso", photoService.delete(year,month,name), null));
    }

}
