package projeto.collendar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projeto.collendar.dto.EventoDTO;
import projeto.collendar.model.Evento;
import projeto.collendar.service.EventoService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    @PostMapping
    public ResponseEntity<EventoDTO> criar(@RequestBody Evento evento, @RequestParam UUID calendarioId) {
        try {
            Evento novoEvento = eventoService.criar(evento, calendarioId);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(novoEvento));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoDTO> buscarPorId(@PathVariable UUID id) {
        return eventoService.buscarPorId(id)
                .map(evento -> ResponseEntity.ok(toDTO(evento)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<EventoDTO>> listarTodos() {
        List<EventoDTO> eventos = eventoService.listarTodos()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/calendario/{calendarioId}")
    public ResponseEntity<List<EventoDTO>> listarPorCalendario(@PathVariable UUID calendarioId) {
        List<EventoDTO> eventos = eventoService.listarPorCalendario(calendarioId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/calendario/{calendarioId}/paginado")
    public ResponseEntity<Page<EventoDTO>> listarPorCalendarioPaginado(
            @PathVariable UUID calendarioId,
            Pageable pageable) {
        try {
            Page<EventoDTO> eventos = eventoService.listarPorCalendarioPaginado(calendarioId, pageable)
                    .map(this::toDTO);
            return ResponseEntity.ok(eventos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<EventoDTO>> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {
        List<EventoDTO> eventos = eventoService.buscarPorPeriodo(dataInicio, dataFim)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/calendario/{calendarioId}/periodo")
    public ResponseEntity<List<EventoDTO>> buscarPorCalendarioEPeriodo(
            @PathVariable UUID calendarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {
        List<EventoDTO> eventos = eventoService.buscarPorCalendarioEPeriodo(calendarioId, dataInicio, dataFim)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<EventoDTO>> buscarPorTitulo(
            @RequestParam String titulo,
            Pageable pageable) {
        Page<EventoDTO> eventos = eventoService.buscarPorTitulo(titulo, pageable)
                .map(this::toDTO);
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/recorrentes")
    public ResponseEntity<List<EventoDTO>> listarRecorrentes() {
        List<EventoDTO> eventos = eventoService.listarRecorrentes()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoDTO> atualizar(@PathVariable UUID id, @RequestBody Evento evento) {
        try {
            Evento eventoAtualizado = eventoService.atualizar(id, evento);
            return ResponseEntity.ok(toDTO(eventoAtualizado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        try {
            eventoService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/calendario/{calendarioId}/contar")
    public ResponseEntity<Long> contarPorCalendario(@PathVariable UUID calendarioId) {
        long quantidade = eventoService.contarPorCalendario(calendarioId);
        return ResponseEntity.ok(quantidade);
    }

    @GetMapping("/{eventoId}/pertence-calendario/{calendarioId}")
    public ResponseEntity<Boolean> pertenceAoCalendario(
            @PathVariable UUID eventoId,
            @PathVariable UUID calendarioId) {
        try {
            boolean pertence = eventoService.pertenceAoCalendario(eventoId, calendarioId);
            return ResponseEntity.ok(pertence);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private EventoDTO toDTO(Evento evento) {
        EventoDTO dto = new EventoDTO();
        dto.setId(evento.getId());
        dto.setTitulo(evento.getTitulo());
        dto.setDescricao(evento.getDescricao());
        dto.setDataInicio(evento.getDataInicio());
        dto.setDataFim(evento.getDataFim());
        dto.setLocal(evento.getLocal());
        dto.setCor(evento.getCor());
        dto.setDiaInteiro(evento.getDiaInteiro());
        dto.setRecorrente(evento.getRecorrente());
        dto.setTipoRecorrencia(evento.getTipoRecorrencia());
        dto.setCalendarioId(evento.getCalendario().getId());
        dto.setCalendarioNome(evento.getCalendario().getNome());
        return dto;
    }
}