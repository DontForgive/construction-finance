package br.com.galsystem.construction.finance.utils;

public class ResultSetUtils {

    public static String safeObjOrEmpty(Object obj) {
        return obj != null ? obj.toString() : "Não Informado";
    }
}
