package projeto.collendar.dtos.response;

import projeto.collendar.enums.TipoRecorrencia;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventoResponseDTO(
        UUID id,
        String titulo,
        String descricao,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        String local,
        String cor,
        Boolean diaInteiro,
        Boolean recorrente,
        TipoRecorrencia tipoRecorrencia,
        UUID calendarioId,
        String calendarioNome
) { }
