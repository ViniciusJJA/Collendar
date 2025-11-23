package projeto.collendar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projeto.collendar.dtos.request.EventoRequestDTO;
import projeto.collendar.dtos.response.EventoResponseDTO;
import projeto.collendar.exception.BusinessException;
import projeto.collendar.exception.ResourceNotFoundException;
import projeto.collendar.mappers.EventoMapper;
import projeto.collendar.model.Calendario;
import projeto.collendar.model.Evento;
import projeto.collendar.repository.EventoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final CalendarioService calendarioService;

    @Transactional
    public EventoResponseDTO create(EventoRequestDTO dto) {
        validateDates(dto.dataInicio(), dto.dataFim());
        Calendario calendario = calendarioService.findEntityById(dto.calendarioId());
        Evento evento = EventoMapper.toEntity(dto, calendario);
        eventoRepository.save(evento);
        return EventoMapper.toDTO(evento);
    }

    public EventoResponseDTO findById(UUID id) {
        return eventoRepository.findById(id)
                .map(EventoMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", id.toString()));
    }

    public List<EventoResponseDTO> listAll() {
        return eventoRepository.findAll().stream()
                .map(EventoMapper::toDTO)
                .toList();
    }

    public List<EventoResponseDTO> listByCalendario(UUID calendarioId) {
        return eventoRepository.findByCalendarioId(calendarioId).stream()
                .map(EventoMapper::toDTO)
                .toList();
    }

    public Page<EventoResponseDTO> listByCalendarioPaginated(UUID calendarioId, Pageable pageable) {
        Calendario calendario = calendarioService.findEntityById(calendarioId);
        return eventoRepository.findByCalendario(calendario, pageable)
                .map(EventoMapper::toDTO);
    }

    public List<EventoResponseDTO> findByPeriod(LocalDateTime start, LocalDateTime end) {
        return eventoRepository.findByDataInicioBetween(start, end).stream()
                .map(EventoMapper::toDTO)
                .toList();
    }

    public List<EventoResponseDTO> findByCalendarioAndPeriod(UUID calendarioId, LocalDateTime start, LocalDateTime end) {
        return eventoRepository.findByCalendarioAndDataBetween(calendarioId, start, end).stream()
                .map(EventoMapper::toDTO)
                .toList();
    }

    public Page<EventoResponseDTO> searchByTitulo(String titulo, Pageable pageable) {
        return eventoRepository.findByTituloContainingIgnoreCase(titulo, pageable)
                .map(EventoMapper::toDTO);
    }

    public List<EventoResponseDTO> listRecorrentes() {
        return eventoRepository.findByRecorrente(true).stream()
                .map(EventoMapper::toDTO)
                .toList();
    }

    @Transactional
    public EventoResponseDTO update(UUID id, EventoRequestDTO dto) {
        validateDates(dto.dataInicio(), dto.dataFim());
        Evento evento = findEntityById(id);

        evento.setTitulo(dto.titulo());
        evento.setDescricao(dto.descricao());
        evento.setDataInicio(dto.dataInicio());
        evento.setDataFim(dto.dataFim());
        evento.setLocal(dto.local());
        evento.setCor(dto.cor());
        evento.setDiaInteiro(dto.diaInteiro());
        evento.setRecorrente(dto.recorrente());
        evento.setTipoRecorrencia(dto.tipoRecorrencia());

        return EventoMapper.toDTO(eventoRepository.save(evento));
    }

    @Transactional
    public void delete(UUID id) {
        if (!eventoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Evento", id.toString());
        }
        eventoRepository.deleteById(id);
    }

    public long countByCalendario(UUID calendarioId) {
        return eventoRepository.findByCalendarioId(calendarioId).size();
    }

    public UUID getCalendarioIdByEvento(UUID eventoId) {
        Evento evento = findEntityById(eventoId);
        return evento.getCalendario().getId();
    }

    public Evento findEntityById(UUID id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", id.toString()));
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new BusinessException("Datas de início e fim são obrigatórias");
        }
        if (end.isBefore(start)) {
            throw new BusinessException("Data de fim deve ser posterior à data de início");
        }
    }
}