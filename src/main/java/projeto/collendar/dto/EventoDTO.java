package projeto.collendar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import projeto.collendar.enums.TipoRecorrencia;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoDTO {
    private UUID id;
    private String titulo;
    private String descricao;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private String local;
    private String cor;
    private Boolean diaInteiro;
    private Boolean recorrente;
    private TipoRecorrencia tipoRecorrencia;
    private UUID calendarioId;
    private String calendarioNome;
}
//comentário