package projeto.collendar.mappers;

import projeto.collendar.dtos.request.CalendarioRequestDTO;
import projeto.collendar.dtos.response.CalendarioResponseDTO;
import projeto.collendar.enums.TipoPermissao;
import projeto.collendar.model.Calendario;
import projeto.collendar.model.Usuario;

public class CalendarioMapper {

    public static CalendarioResponseDTO toDTO(Calendario entity) {
        return toDTO(entity, null, null);
    }

    public static CalendarioResponseDTO toDTO(Calendario entity, Boolean proprietario, TipoPermissao permissao) {
        return new CalendarioResponseDTO(
                entity.getId(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getCor(),
                entity.getUsuario().getId(),
                entity.getUsuario().getNome(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                proprietario,
                permissao
        );
    }

    public static Calendario toEntity(CalendarioRequestDTO dto, Usuario usuario) {
        Calendario c = new Calendario();
        c.setNome(dto.nome());
        c.setDescricao(dto.descricao());
        c.setCor(dto.cor());
        c.setUsuario(usuario);
        return c;
    }
}