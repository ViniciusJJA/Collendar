package projeto.collendar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import projeto.collendar.enums.TipoPermissao;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompartilhamentoDTO {
    private UUID id;
    private UUID calendarioId;
    private String calendarioNome;
    private UUID usuarioId;
    private String usuarioNome;
    private TipoPermissao permissao;
    private LocalDateTime createdAt;
}