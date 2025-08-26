package br.com.galsystem.construction.finance.dto.images;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhotoDTO {
    private String filename;
    private String url;
    private LocalDateTime uploadedAt;
    private String type;
    private String mimeType;

}
