package br.com.galsystem.construction.finance.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
public class Response<T> {
    private int status;
    private String message;
    private T data;
    private List<String> erros = new ArrayList<>();
//
//    public Response() {
//    }
//
//
//    public int getStatus() {
//        return status;
//    }
//
//    public void setStatus(final int status) {
//        this.status = status;
//    }
//
//    public String getMessage() {
//        return message;
//    }
//
//    public void setMessage(final String message) {
//        this.message = message;
//    }
//
//    public T getData() {
//        return data;
//    }
//
//    public void setData(final T data) {
//        this.data = data;
//    }
//
//    public List<String> getErros() {
//        return erros;
//    }
}
