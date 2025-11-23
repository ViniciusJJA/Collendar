package projeto.collendar.mappers;

import projeto.collendar.dtos.request.UsuarioRequestDTO;
import projeto.collendar.dtos.response.UsuarioResponseDTO;
import projeto.collendar.model.Role;
import projeto.collendar.model.Usuario;

import java.util.stream.Collectors;

public class UsuarioMapper {

    public static UsuarioResponseDTO toDTO(Usuario entity) {
        return new UsuarioResponseDTO(
                entity.getId(),
                entity.getNome(),
                entity.getEmail(),
                entity.getAtivo(),
                entity.getRoles().stream()
                        .map(Role::getNome)
                        .collect(Collectors.toSet())
        );
    }

    public static Usuario toEntity(UsuarioRequestDTO dto) {
        Usuario u = new Usuario();
        u.setNome(dto.nome());
        u.setEmail(dto.email());
        u.setSenha(dto.senha());
        return u;
    }
}