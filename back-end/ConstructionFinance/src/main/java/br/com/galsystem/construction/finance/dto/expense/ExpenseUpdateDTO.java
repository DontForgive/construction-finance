package br.com.galsystem.construction.finance.dto.expense;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseUpdateDTO(
        @NotNull(message = "Data é obrigatória")
        LocalDate date,

        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
        String description,

        Long supplierId,
        Long payerId,

        @Size(max = 60, message = "Forma de pagamento deve ter no máximo 60 caracteres")
        String paymentMethod,

        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser positivo")
        BigDecimal amount,

        @Size(max = 255, message = "URL do anexo deve ter no máximo 255 caracteres")
        String attachmentUrl
) {
}
