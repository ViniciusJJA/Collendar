
package projeto.collendar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private UUID id;
    private String nome;
    private String email;
    private Boolean ativo;
    private Set<String> roles;
}
//comentário