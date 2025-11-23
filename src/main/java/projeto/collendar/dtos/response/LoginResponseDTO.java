package projeto.collendar.dtos.response;

import java.util.Set;
import java.util.UUID;

public record LoginResponseDTO(
        String token,
        String tipo,
        UUID usuarioId,
        String nome,
        String email,
        Set<String> roles
) { }