package projeto.collendar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Eventos", description = "Gerenciamento de eventos em calendários")
public class EventoController {

    private final EventoService eventoService;

    @PostMapping
    @Operation(
            summary = "Criar novo evento",
            description = "Cria um novo evento em um calendário específico",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Evento criado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos ou calendário não encontrado")
            }
    )
    public ResponseEntity<EventoDTO> criar(
            @RequestBody Evento evento,
            @Parameter(description = "ID do calendário", required = true)
            @RequestParam UUID calendarioId) {
        try {
            Evento novoEvento = eventoService.criar(evento, calendarioId);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(novoEvento));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar evento por ID",
            description = "Retorna os dados de um evento específico",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Evento encontrado"),
                    @ApiResponse(responseCode = "404", description = "Evento não encontrado")
            }
    )
    public ResponseEntity<EventoDTO> buscarPorId(
            @Parameter(description = "ID do evento", required = true)
            @PathVariable UUID id) {
        return eventoService.buscarPorId(id)
                .map(evento -> ResponseEntity.ok(toDTO(evento)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(
            summary = "Listar todos os eventos",
            description = "Retorna lista de todos os eventos do sistema",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
            }
    )
    public ResponseEntity<List<EventoDTO>> listarTodos() {
        List<EventoDTO> eventos = eventoService.listarTodos()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/calendario/{calendarioId}")
    @Operation(
            summary = "Listar eventos do calendário",
            description = "Retorna todos os eventos de um calendário específico",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de eventos retornada")
            }
    )
    public ResponseEntity<List<EventoDTO>> listarPorCalendario(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID calendarioId) {
        List<EventoDTO> eventos = eventoService.listarPorCalendario(calendarioId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/calendario/{calendarioId}/paginado")
    @Operation(
            summary = "Listar eventos do calendário (paginado)",
            description = "Retorna eventos do calendário com paginação",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Página retornada com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Calendário não encontrado")
            }
    )
    public ResponseEntity<Page<EventoDTO>> listarPorCalendarioPaginado(
            @Parameter(description = "ID do calendário", required = true)
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
    @Operation(
            summary = "Buscar eventos por período",
            description = "Busca eventos que ocorrem em um intervalo de datas",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Eventos encontrados")
            }
    )
    public ResponseEntity<List<EventoDTO>> buscarPorPeriodo(
            @Parameter(description = "Data/hora inicial (formato: 2025-01-01T00:00:00)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @Parameter(description = "Data/hora final (formato: 2025-01-31T23:59:59)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {
        List<EventoDTO> eventos = eventoService.buscarPorPeriodo(dataInicio, dataFim)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/calendario/{calendarioId}/periodo")
    @Operation(
            summary = "Buscar eventos por calendário e período",
            description = "Busca eventos de um calendário específico em um intervalo de datas",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Eventos encontrados")
            }
    )
    public ResponseEntity<List<EventoDTO>> buscarPorCalendarioEPeriodo(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID calendarioId,
            @Parameter(description = "Data/hora inicial", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @Parameter(description = "Data/hora final", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {
        List<EventoDTO> eventos = eventoService.buscarPorCalendarioEPeriodo(calendarioId, dataInicio, dataFim)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar eventos por título",
            description = "Busca eventos que contenham o texto informado no título",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resultados da busca retornados")
            }
    )
    public ResponseEntity<Page<EventoDTO>> buscarPorTitulo(
            @Parameter(description = "Título ou parte do título do evento", required = true)
            @RequestParam String titulo,
            Pageable pageable) {
        Page<EventoDTO> eventos = eventoService.buscarPorTitulo(titulo, pageable)
                .map(this::toDTO);
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/recorrentes")
    @Operation(
            summary = "Listar eventos recorrentes",
            description = "Retorna todos os eventos marcados como recorrentes",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de eventos recorrentes")
            }
    )
    public ResponseEntity<List<EventoDTO>> listarRecorrentes() {
        List<EventoDTO> eventos = eventoService.listarRecorrentes()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventos);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar evento",
            description = "Atualiza os dados de um evento existente",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Evento atualizado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                    @ApiResponse(responseCode = "404", description = "Evento não encontrado")
            }
    )
    public ResponseEntity<EventoDTO> atualizar(
            @Parameter(description = "ID do evento", required = true)
            @PathVariable UUID id,
            @RequestBody Evento evento) {
        try {
            Evento eventoAtualizado = eventoService.atualizar(id, evento);
            return ResponseEntity.ok(toDTO(eventoAtualizado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Deletar evento",
            description = "Remove permanentemente um evento do calendário",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Evento deletado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Evento não encontrado")
            }
    )
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do evento", required = true)
            @PathVariable UUID id) {
        try {
            eventoService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/calendario/{calendarioId}/contar")
    @Operation(
            summary = "Contar eventos do calendário",
            description = "Retorna a quantidade total de eventos de um calendário",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Contagem realizada")
            }
    )
    public ResponseEntity<Long> contarPorCalendario(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID calendarioId) {
        long quantidade = eventoService.contarPorCalendario(calendarioId);
        return ResponseEntity.ok(quantidade);
    }

    @GetMapping("/{eventoId}/pertence-calendario/{calendarioId}")
    @Operation(summary = "Verificar pertencimento ao calendário",
            description = "Verifica se um evento pertence a um calendário específico",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Verificação realizada"),
                    @ApiResponse(responseCode = "400", description = "Evento não encontrado")
            }
    )
    public ResponseEntity<Boolean> pertenceAoCalendario(
            @Parameter(description = "ID do evento", required = true)
            @PathVariable UUID eventoId,
            @Parameter(description = "ID do calendário", required = true)
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