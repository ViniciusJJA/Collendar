package projeto.collendar.dtos.response;

import projeto.collendar.enums.TipoPermissao;

import java.time.LocalDateTime;
import java.util.UUID;

public record CompartilhamentoResponseDTO(
        UUID id,
        UUID calendarioId,
        String calendarioNome,
        UUID usuarioId,
        String usuarioNome,
        String usuarioEmail,
        TipoPermissao permissao,
        LocalDateTime createdAt
) { }