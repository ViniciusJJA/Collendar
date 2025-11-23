package projeto.collendar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projeto.collendar.dtos.request.EventoRequestDTO;
import projeto.collendar.dtos.response.EventoResponseDTO;
import projeto.collendar.exception.AccessDeniedException;
import projeto.collendar.service.CompartilhamentoService;
import projeto.collendar.service.EventoService;
import projeto.collendar.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/eventos")
@RequiredArgsConstructor
@Tag(name = "Eventos", description = "Gerenciamento de eventos em calendários")
public class EventoController {

    private final EventoService eventoService;
    private final CompartilhamentoService compartilhamentoService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(
            summary = "Criar novo evento",
            description = "Cria evento se tiver permissão EDITAR no calendário",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Evento criado",
                            content = @Content(schema = @Schema(implementation = EventoResponseDTO.class))),
                    @ApiResponse(responseCode = "403", description = "Sem permissão"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos")
            }
    )
    public ResponseEntity<EventoResponseDTO> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do evento",
                    required = true,
                    content = @Content(schema = @Schema(implementation = EventoRequestDTO.class))
            )
            @RequestBody @Valid EventoRequestDTO dto) {
        UUID usuarioId = securityUtils.getLoggedUserId();

        if (!compartilhamentoService.canEdit(dto.calendarioId(), usuarioId)) {
            throw new AccessDeniedException("Você não tem permissão para criar eventos neste calendário");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(eventoService.create(dto));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar evento por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Evento encontrado"),
                    @ApiResponse(responseCode = "403", description = "Sem acesso"),
                    @ApiResponse(responseCode = "404", description = "Não encontrado")
            }
    )
    public ResponseEntity<EventoResponseDTO> findById(
            @Parameter(description = "ID do evento", required = true)
            @PathVariable UUID id) {
        UUID usuarioId = securityUtils.getLoggedUserId();
        UUID calendarioId = eventoService.getCalendarioIdByEvento(id);

        if (!compartilhamentoService.hasAccess(calendarioId, usuarioId)) {
            throw new AccessDeniedException("Você não tem acesso a este evento");
        }

        return ResponseEntity.ok(eventoService.findById(id));
    }

    @GetMapping("/calendario/{calendarioId}")
    @Operation(summary = "Listar eventos do calendário")
    public ResponseEntity<List<EventoResponseDTO>> listByCalendario(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID calendarioId) {
        UUID usuarioId = securityUtils.getLoggedUserId();

        if (!compartilhamentoService.hasAccess(calendarioId, usuarioId)) {
            throw new AccessDeniedException("Você não tem acesso a este calendário");
        }

        return ResponseEntity.ok(eventoService.listByCalendario(calendarioId));
    }

    @GetMapping("/calendario/{calendarioId}/paginado")
    @Operation(summary = "Listar eventos do calendário (paginado)")
    public ResponseEntity<Page<EventoResponseDTO>> listByCalendarioPaginado(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID calendarioId,
            Pageable pageable) {
        UUID usuarioId = securityUtils.getLoggedUserId();

        if (!compartilhamentoService.hasAccess(calendarioId, usuarioId)) {
            throw new AccessDeniedException("Você não tem acesso a este calendário");
        }

        return ResponseEntity.ok(eventoService.listByCalendarioPaginated(calendarioId, pageable));
    }

    @GetMapping("/calendario/{calendarioId}/periodo")
    @Operation(summary = "Buscar eventos por calendário e período")
    public ResponseEntity<List<EventoResponseDTO>> findByCalendarioAndPeriodo(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID calendarioId,
            @Parameter(description = "Data/hora inicial", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @Parameter(description = "Data/hora final", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {

        UUID usuarioId = securityUtils.getLoggedUserId();

        if (!compartilhamentoService.hasAccess(calendarioId, usuarioId)) {
            throw new AccessDeniedException("Você não tem acesso a este calendário");
        }

        return ResponseEntity.ok(eventoService.findByCalendarioAndPeriod(calendarioId, dataInicio, dataFim));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar eventos por título")
    public ResponseEntity<Page<EventoResponseDTO>> searchByTitulo(
            @Parameter(description = "Título ou parte do título", required = true)
            @RequestParam String titulo,
            Pageable pageable) {
        return ResponseEntity.ok(eventoService.searchByTitulo(titulo, pageable));
    }

    @GetMapping("/recorrentes")
    @Operation(summary = "Listar eventos recorrentes")
    public ResponseEntity<List<EventoResponseDTO>> listRecorrentes() {
        return ResponseEntity.ok(eventoService.listRecorrentes());
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar evento",
            description = "Atualiza se tiver permissão EDITAR no calendário",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Atualizado"),
                    @ApiResponse(responseCode = "403", description = "Sem permissão"),
                    @ApiResponse(responseCode = "404", description = "Não encontrado")
            }
    )
    public ResponseEntity<EventoResponseDTO> update(
            @Parameter(description = "ID do evento", required = true)
            @PathVariable UUID id,
            @RequestBody @Valid EventoRequestDTO dto) {
        UUID usuarioId = securityUtils.getLoggedUserId();
        UUID calendarioId = eventoService.getCalendarioIdByEvento(id);

        if (!compartilhamentoService.canEdit(calendarioId, usuarioId)) {
            throw new AccessDeniedException("Você não tem permissão para editar eventos neste calendário");
        }

        return ResponseEntity.ok(eventoService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar evento")
    @ApiResponse(responseCode = "204", description = "Deletado")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do evento", required = true)
            @PathVariable UUID id) {
        UUID usuarioId = securityUtils.getLoggedUserId();
        UUID calendarioId = eventoService.getCalendarioIdByEvento(id);

        if (!compartilhamentoService.canEdit(calendarioId, usuarioId)) {
            throw new AccessDeniedException("Você não tem permissão para deletar eventos neste calendário");
        }

        eventoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/calendario/{calendarioId}/contar")
    @Operation(summary = "Contar eventos do calendário")
    public ResponseEntity<Long> countByCalendario(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID calendarioId) {
        UUID usuarioId = securityUtils.getLoggedUserId();

        if (!compartilhamentoService.hasAccess(calendarioId, usuarioId)) {
            throw new AccessDeniedException("Você não tem acesso a este calendário");
        }

        return ResponseEntity.ok(eventoService.countByCalendario(calendarioId));
    }
}