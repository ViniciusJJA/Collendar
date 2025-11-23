package projeto.collendar.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import projeto.collendar.enums.TipoPermissao;

import java.util.UUID;

public record CompartilhamentoRequestDTO(
        @NotNull(message = "ID do calendário é obrigatório")
        UUID calendarioId,

        @NotBlank(message = "Email do destinatário é obrigatório")
        @Email(message = "Email inválido")
        String emailDestinatario,

        @NotNull(message = "Permissão é obrigatória")
        TipoPermissao permissao
) { }