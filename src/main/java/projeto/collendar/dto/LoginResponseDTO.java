package projeto.collendar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private String tipo = "Bearer";
    private UUID usuarioId;
    private String nome;
    private String email;
    private Set<String> roles;
}