package br.com.galsystem.construction.finance.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
public class Response<T> {
    private int status;
    private String message;
    private T data;
    private List<String> erros = new ArrayList<>();
}


