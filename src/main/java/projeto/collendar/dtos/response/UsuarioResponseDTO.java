package projeto.collendar.dtos.response;

import java.util.Set;
import java.util.UUID;

public record UsuarioResponseDTO(
        UUID id,
        String nome,
        String email,
        Boolean ativo,
        Set<String> roles
) { }