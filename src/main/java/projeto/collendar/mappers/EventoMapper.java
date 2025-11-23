package projeto.collendar.mappers;

import projeto.collendar.dtos.request.EventoRequestDTO;
import projeto.collendar.dtos.response.EventoResponseDTO;
import projeto.collendar.model.Calendario;
import projeto.collendar.model.Evento;

public class EventoMapper {

    public static EventoResponseDTO toDTO(Evento entity) {
        return new EventoResponseDTO(
                entity.getId(),
                entity.getTitulo(),
                entity.getDescricao(),
                entity.getDataInicio(),
                entity.getDataFim(),
                entity.getLocal(),
                entity.getCor(),
                entity.getDiaInteiro(),
                entity.getRecorrente(),
                entity.getTipoRecorrencia(),
                entity.getCalendario().getId(),
                entity.getCalendario().getNome()
        );
    }

    public static Evento toEntity(EventoRequestDTO dto, Calendario calendario) {
        Evento e = new Evento();
        e.setTitulo(dto.titulo());
        e.setDescricao(dto.descricao());
        e.setDataInicio(dto.dataInicio());
        e.setDataFim(dto.dataFim());
        e.setLocal(dto.local());
        e.setCor(dto.cor());
        e.setDiaInteiro(dto.diaInteiro());
        e.setRecorrente(dto.recorrente());
        e.setTipoRecorrencia(dto.tipoRecorrencia());
        e.setCalendario(calendario);
        return e;
    }
}