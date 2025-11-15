package projeto.collendar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarioDTO {
    private UUID id;
    private String nome;
    private String descricao;
    private String cor;
    private UUID usuarioId;
    private String usuarioNome;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}