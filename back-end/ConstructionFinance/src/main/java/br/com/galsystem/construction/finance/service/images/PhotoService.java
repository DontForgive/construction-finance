package br.com.galsystem.construction.finance.service.images;

import br.com.galsystem.construction.finance.dto.images.PhotoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PhotoService {

    List<PhotoDTO> store(List<MultipartFile> file);

    List<Integer> listYears();

    List<Integer> listMonths(int year);

    List<PhotoDTO> listPhotos(int year, int month);

    List<PhotoDTO> getAll();

    PhotoDTO delete(int year, int month, String filename);

}
