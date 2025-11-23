package projeto.collendar.dtos.response;

import projeto.collendar.enums.TipoPermissao;

public record PermissaoResponseDTO(
        Boolean proprietario,
        Boolean podeVisualizar,
        Boolean podeEditar,
        TipoPermissao permissao
) { }