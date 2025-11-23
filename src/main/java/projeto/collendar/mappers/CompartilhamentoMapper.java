package projeto.collendar.mappers;

import projeto.collendar.dtos.response.CompartilhamentoResponseDTO;
import projeto.collendar.enums.TipoPermissao;
import projeto.collendar.model.Calendario;
import projeto.collendar.model.Compartilhamento;
import projeto.collendar.model.Usuario;

public class CompartilhamentoMapper {

    public static CompartilhamentoResponseDTO toDTO(Compartilhamento entity) {
        return new CompartilhamentoResponseDTO(
                entity.getId(),
                entity.getCalendario().getId(),
                entity.getCalendario().getNome(),
                entity.getUsuario().getId(),
                entity.getUsuario().getNome(),
                entity.getUsuario().getEmail(),
                entity.getPermissao(),
                entity.getCreatedAt()
        );
    }

    public static Compartilhamento toEntity(Calendario calendario, Usuario usuario, TipoPermissao permissao) {
        Compartilhamento c = new Compartilhamento();
        c.setCalendario(calendario);
        c.setUsuario(usuario);
        c.setPermissao(permissao);
        return c;
    }
}