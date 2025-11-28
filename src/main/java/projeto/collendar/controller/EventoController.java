package projeto.collendar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "bearer-jwt")
@Tag(
        name = "Eventos",
        description = "Endpoints para gerenciamento de eventos em calendários. " +
                "Permite criar, consultar, atualizar e excluir eventos, " +
                "além de realizar buscas por período, título e eventos recorrentes. " +
                "As operações de criação/edição/exclusão requerem permissão de EDITAR no calendário."
)
public class EventoController {

    private final EventoService eventoService;
    private final CompartilhamentoService compartilhamentoService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(
            summary = "Criar novo evento",
            description = "Cria um evento em um calendário específico. " +
                    "O usuário deve ter permissão de EDITAR no calendário (ser proprietário ou ter compartilhamento com permissão EDITAR). " +
                    "A data de fim deve ser posterior à data de início.",
            tags = {"Eventos"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Evento criado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EventoResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos. Possíveis causas: título ausente, datas inválidas, data fim anterior à data início.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado. Você não tem permissão para criar eventos neste calendário.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Calendário não encontrado.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<EventoResponseDTO> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do evento a ser criado",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = EventoRequestDTO.class),
                            mediaType = "application/json"
                    )
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
            description = "Retorna os detalhes de um evento específico. " +
                    "O usuário deve ter acesso ao calendário do evento (ser proprietário ou ter compartilhamento).",
            tags = {"Eventos"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Evento encontrado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EventoResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado. Você não tem acesso a este evento.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Evento não encontrado.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<EventoResponseDTO> findById(
            @Parameter(
                    description = "ID único do evento (UUID)",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID id) {
        UUID usuarioId = securityUtils.getLoggedUserId();
        UUID calendarioId = eventoService.getCalendarioIdByEvento(id);

        if (!compartilhamentoService.hasAccess(calendarioId, usuarioId)) {
            throw new AccessDeniedException("Você não tem acesso a este evento");
        }

        return ResponseEntity.ok(eventoService.findById(id));
    }

    @GetMapping("/calendario/{calendarioId}")
    @Operation(
            summary = "Listar eventos do calendário",
            description = "Retorna todos os eventos de um calendário específico. " +
                    "O usuário deve ter acesso ao calendário.",
            tags = {"Eventos"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de eventos retornada com sucesso.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado ao calendário.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Calendário não encontrado.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<EventoResponseDTO>> listByCalendario(
            @Parameter(
                    description = "ID do calendário",
                    required = true
            )
            @PathVariable UUID calendarioId) {
        UUID usuarioId = securityUtils.getLoggedUserId();

        if (!compartilhamentoService.hasAccess(calendarioId, usuarioId)) {
            throw new AccessDeniedException("Você não tem acesso a este calendário");
        }

        return ResponseEntity.ok(eventoService.listByCalendario(calendarioId));
    }

    @GetMapping("/calendario/{calendarioId}/paginado")
    @Operation(
            summary = "Listar eventos do calendário (paginado)",
            description = "Retorna os eventos de um calendário com paginação. " +
                    "Útil para calendários com muitos eventos.",
            tags = {"Eventos"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Página de eventos retornada com sucesso.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado ao calendário.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Page<EventoResponseDTO>> listByCalendarioPaginado(
            @Parameter(
                    description = "ID do calendário",
                    required = true
            )
            @PathVariable UUID calendarioId,
            @Parameter(
                    description = "Configurações de paginação (page, size, sort)",
                    example = "page=0&size=20&sort=dataInicio,asc"
            )
            Pageable pageable) {
        UUID usuarioId = securityUtils.getLoggedUserId();

        if (!compartilhamentoService.hasAccess(calendarioId, usuarioId)) {
            throw new AccessDeniedException("Você não tem acesso a este calendário");
        }

        return ResponseEntity.ok(eventoService.listByCalendarioPaginated(calendarioId, pageable));
    }

    @GetMapping("/calendario/{calendarioId}/periodo")
    @Operation(
            summary = "Buscar eventos por período",
            description = "Retorna eventos de um calendário específico dentro de um período de datas. " +
                    "Útil para visualização de calendário mensal ou semanal. " +
                    "As datas devem estar no formato ISO 8601 (yyyy-MM-dd'T'HH:mm:ss).",
            tags = {"Eventos"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Eventos do período retornados com sucesso.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Formato de data inválido.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado ao calendário.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<EventoResponseDTO>> findByCalendarioAndPeriodo(
            @Parameter(
                    description = "ID do calendário",
                    required = true
            )
            @PathVariable UUID calendarioId,
            @Parameter(
                    description = "Data e hora de início do período (formato ISO 8601)",
                    required = true,
                    example = "2025-01-01T00:00:00"
            )
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @Parameter(
                    description = "Data e hora de fim do período (formato ISO 8601)",
                    required = true,
                    example = "2025-01-31T23:59:59"
            )
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {

        UUID usuarioId = securityUtils.getLoggedUserId();

        if (!compartilhamentoService.hasAccess(calendarioId, usuarioId)) {
            throw new AccessDeniedException("Você não tem acesso a este calendário");
        }

        return ResponseEntity.ok(eventoService.findByCalendarioAndPeriod(calendarioId, dataInicio, dataFim));
    }

    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar eventos por título",
            description = "Realiza uma busca de eventos cujo título contenha o texto fornecido. " +
                    "A busca é case-insensitive e retorna resultados paginados.",
            tags = {"Eventos"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resultados da busca retornados com sucesso.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Page<EventoResponseDTO>> searchByTitulo(
            @Parameter(
                    description = "Título ou parte do título do evento",
                    required = true,
                    example = "Reunião"
            )
            @RequestParam String titulo,
            @Parameter(
                    description = "Configurações de paginação"
            )
            Pageable pageable) {
        return ResponseEntity.ok(eventoService.searchByTitulo(titulo, pageable));
    }

    @GetMapping("/recorrentes")
    @Operation(
            summary = "Listar eventos recorrentes",
            description = "Retorna todos os eventos marcados como recorrentes no sistema. " +
                    "Eventos recorrentes repetem-se em intervalos regulares (diário, semanal, mensal, anual).",
            tags = {"Eventos"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de eventos recorrentes retornada com sucesso.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<EventoResponseDTO>> listRecorrentes() {
        return ResponseEntity.ok(eventoService.listRecorrentes());
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar evento",
            description = "Atualiza as informações de um evento existente. " +
                    "O usuário deve ter permissão de EDITAR no calendário do evento. " +
                    "A data de fim deve continuar sendo posterior à data de início.",
            tags = {"Eventos"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Evento atualizado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EventoResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado. Você não tem permissão para editar eventos neste calendário.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Evento não encontrado.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<EventoResponseDTO> update(
            @Parameter(
                    description = "ID do evento a ser atualizado",
                    required = true
            )
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Novos dados do evento",
                    required = true
            )
            @RequestBody @Valid EventoRequestDTO dto) {
        UUID usuarioId = securityUtils.getLoggedUserId();
        UUID calendarioId = eventoService.getCalendarioIdByEvento(id);

        if (!compartilhamentoService.canEdit(calendarioId, usuarioId)) {
            throw new AccessDeniedException("Você não tem permissão para editar eventos neste calendário");
        }

        return ResponseEntity.ok(eventoService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Excluir evento",
            description = "Exclui permanentemente um evento. " +
                    "O usuário deve ter permissão de EDITAR no calendário do evento. " +
                    "Esta ação não pode ser desfeita.",
            tags = {"Eventos"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Evento excluído com sucesso."
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado. Você não tem permissão para deletar eventos neste calendário.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Evento não encontrado.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Void> delete(
            @Parameter(
                    description = "ID do evento a ser excluído",
                    required = true
            )
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
    @Operation(
            summary = "Contar eventos do calendário",
            description = "Retorna o número total de eventos cadastrados em um calendário. " +
                    "O usuário deve ter acesso ao calendário.",
            tags = {"Eventos"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contagem realizada com sucesso.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado ao calendário.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Calendário não encontrado.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Long> countByCalendario(
            @Parameter(
                    description = "ID do calendário",
                    required = true
            )
            @PathVariable UUID calendarioId) {
        UUID usuarioId = securityUtils.getLoggedUserId();

        if (!compartilhamentoService.hasAccess(calendarioId, usuarioId)) {
            throw new AccessDeniedException("Você não tem acesso a este calendário");
        }

        return ResponseEntity.ok(eventoService.countByCalendario(calendarioId));
    }
}