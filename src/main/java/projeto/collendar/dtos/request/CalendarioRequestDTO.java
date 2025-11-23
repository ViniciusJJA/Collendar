package projeto.collendar.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record CalendarioRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        String descricao,

        String cor
) { }
