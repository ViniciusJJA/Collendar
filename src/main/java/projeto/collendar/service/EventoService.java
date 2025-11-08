package projeto.collendar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projeto.collendar.model.Calendario;
import projeto.collendar.model.Evento;
import projeto.collendar.repository.CalendarioRepository;
import projeto.collendar.repository.EventoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final CalendarioRepository calendarioRepository;

    @Transactional
    public Evento criar(Evento evento, UUID calendarioId) {
        Calendario calendario = calendarioRepository.findById(calendarioId)
                .orElseThrow(() -> new IllegalArgumentException("Calendário não encontrado"));

        validarDatas(evento.getDataInicio(), evento.getDataFim());

        evento.setCalendario(calendario);
        return eventoRepository.save(evento);
    }

    public Optional<Evento> buscarPorId(UUID id) {
        return eventoRepository.findById(id);
    }

    public List<Evento> listarTodos() {
        return eventoRepository.findAll();
    }

    public List<Evento> listarPorCalendario(UUID calendarioId) {
        return eventoRepository.findByCalendarioId(calendarioId);
    }

    public Page<Evento> listarPorCalendarioPaginado(UUID calendarioId, Pageable pageable) {
        Calendario calendario = calendarioRepository.findById(calendarioId)
                .orElseThrow(() -> new IllegalArgumentException("Calendário não encontrado"));
        return eventoRepository.findByCalendario(calendario, pageable);
    }

    public List<Evento> buscarPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return eventoRepository.findByDataInicioBetween(dataInicio, dataFim);
    }

    public List<Evento> buscarPorCalendarioEPeriodo(UUID calendarioId,
                                                    LocalDateTime dataInicio,
                                                    LocalDateTime dataFim) {
        return eventoRepository.findByCalendarioAndDataBetween(calendarioId, dataInicio, dataFim);
    }

    public Page<Evento> buscarPorTitulo(String titulo, Pageable pageable) {
        return eventoRepository.findByTituloContainingIgnoreCase(titulo, pageable);
    }

    public List<Evento> listarRecorrentes() {
        return eventoRepository.findByRecorrente(true);
    }

    @Transactional
    public Evento atualizar(UUID id, Evento eventoAtualizado) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado"));

        validarDatas(eventoAtualizado.getDataInicio(), eventoAtualizado.getDataFim());

        evento.setTitulo(eventoAtualizado.getTitulo());
        evento.setDescricao(eventoAtualizado.getDescricao());
        evento.setDataInicio(eventoAtualizado.getDataInicio());
        evento.setDataFim(eventoAtualizado.getDataFim());
        evento.setLocal(eventoAtualizado.getLocal());
        evento.setCor(eventoAtualizado.getCor());
        evento.setDiaInteiro(eventoAtualizado.getDiaInteiro());
        evento.setRecorrente(eventoAtualizado.getRecorrente());
        evento.setTipoRecorrencia(eventoAtualizado.getTipoRecorrencia());

        return eventoRepository.save(evento);
    }

    @Transactional
    public void deletar(UUID id) {
        if (!eventoRepository.existsById(id)) {
            throw new IllegalArgumentException("Evento não encontrado");
        }
        eventoRepository.deleteById(id);
    }

    public long contarPorCalendario(UUID calendarioId) {
        return eventoRepository.findByCalendarioId(calendarioId).size();
    }

    public boolean pertenceAoCalendario(UUID eventoId, UUID calendarioId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado"));
        return evento.getCalendario().getId().equals(calendarioId);
    }

    private void validarDatas(LocalDateTime dataInicio, LocalDateTime dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Datas de início e fim são obrigatórias");
        }
        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Data de fim deve ser posterior à data de início");
        }
    }
}