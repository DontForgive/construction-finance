package br.com.galsystem.construction.finance.service.images;

import br.com.galsystem.construction.finance.dto.images.PhotoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PhotoServiceV1 {

    List<PhotoDTO> store(List<MultipartFile> file);

    List<Integer> listYears();

    List<Integer> listMonths(int year);

    Page<PhotoDTO> listPhotos(int year, int month, Pageable pageable);

    Page<PhotoDTO> getAll(Pageable pageable);

    void generateMissingThumbnails();

    PhotoDTO delete(int year, int month, String filename);
}
