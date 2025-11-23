package projeto.collendar.dtos.response;

import projeto.collendar.enums.TipoPermissao;

import java.time.LocalDateTime;
import java.util.UUID;

public record CalendarioResponseDTO(
        UUID id,
        String nome,
        String descricao,
        String cor,
        UUID usuarioId,
        String usuarioNome,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean proprietario,
        TipoPermissao permissao
) { }