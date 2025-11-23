package projeto.collendar.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import projeto.collendar.enums.TipoRecorrencia;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventoRequestDTO(
        @NotBlank(message = "Título é obrigatório")
        String titulo,

        String descricao,

        @NotNull(message = "Data de início é obrigatória")
        LocalDateTime dataInicio,

        @NotNull(message = "Data de fim é obrigatória")
        LocalDateTime dataFim,

        String local,

        String cor,

        Boolean diaInteiro,

        Boolean recorrente,

        TipoRecorrencia tipoRecorrencia,

        @NotNull(message = "ID do calendário é obrigatório")
        UUID calendarioId
) { }