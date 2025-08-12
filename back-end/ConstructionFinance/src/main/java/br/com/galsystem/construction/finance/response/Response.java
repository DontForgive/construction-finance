package br.com.galsystem.construction.finance.response;

import java.util.ArrayList;
import java.util.List;

public class Response<T> {

    private T data;                // O dado retornado (ex.: UserDTO, lista, etc.)
    private List<String> erros;    // Lista de mensagens de erro
    private int status;            // Código HTTP (opcional)
    private String message;        // Mensagem adicional (opcional)

    public Response() {
        this.erros = new ArrayList<>();
    }

    public Response(T data) {
        this();
        this.data = data;
    }

    public Response(int status, String message, T data) {
        this();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<String> getErros() {
        return erros;
    }

    public void setErros(List<String> erros) {
        this.erros = erros;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
