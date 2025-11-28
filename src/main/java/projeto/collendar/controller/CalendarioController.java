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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projeto.collendar.dtos.request.CalendarioRequestDTO;
import projeto.collendar.dtos.response.CalendarioResponseDTO;
import projeto.collendar.exception.AccessDeniedException;
import projeto.collendar.service.CalendarioService;
import projeto.collendar.service.CompartilhamentoService;
import projeto.collendar.utils.SecurityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/calendarios")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(
        name = "Calendários",
        description = "Endpoints para gerenciamento de calendários. " +
                "Permite criar, consultar, atualizar e excluir calendários, " +
                "além de gerenciar permissões de acesso e visualização."
)
public class CalendarioController {

    private final CalendarioService calendarioService;
    private final CompartilhamentoService compartilhamentoService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(
            summary = "Criar novo calendário",
            description = "Cria um calendário vinculado ao usuário autenticado. " +
                    "O usuário automaticamente se torna o proprietário do calendário.",
            tags = {"Calendários"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Calendário criado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CalendarioResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos. Verifique se o nome foi fornecido.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<CalendarioResponseDTO> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do calendário a ser criado",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CalendarioRequestDTO.class),
                            mediaType = "application/json"
                    )
            )
            @RequestBody @Valid CalendarioRequestDTO dto) {
        UUID usuarioId = securityUtils.getLoggedUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(calendarioService.create(dto, usuarioId));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar calendário por ID",
            description = "Retorna os detalhes de um calendário específico. " +
                    "O usuário deve ser o proprietário ou ter acesso compartilhado ao calendário.",
            tags = {"Calendários"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Calendário encontrado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CalendarioResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado. Você não tem permissão para visualizar este calendário.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Calendário não encontrado.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<CalendarioResponseDTO> findById(
            @Parameter(
                    description = "ID único do calendário (UUID)",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID id) {
        UUID usuarioId = securityUtils.getLoggedUserId();

        if (!compartilhamentoService.hasAccess(id, usuarioId)) {
            throw new AccessDeniedException("Você não tem acesso a este calendário");
        }

        return ResponseEntity.ok(calendarioService.findById(id));
    }

    @GetMapping("/meus")
    @Operation(
            summary = "Listar meus calendários",
            description = "Retorna todos os calendários dos quais o usuário autenticado é proprietário.",
            tags = {"Calendários"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de calendários retornada com sucesso.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<CalendarioResponseDTO>> listMeus() {
        UUID usuarioId = securityUtils.getLoggedUserId();
        return ResponseEntity.ok(calendarioService.listByUsuario(usuarioId));
    }

    @GetMapping("/acessiveis")
    @Operation(
            summary = "Listar calendários acessíveis",
            description = "Retorna todos os calendários que o usuário pode acessar, " +
                    "incluindo os que ele possui e os que foram compartilhados com ele.",
            tags = {"Calendários"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de calendários acessíveis retornada com sucesso.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<CalendarioResponseDTO>> listAcessiveis() {
        UUID usuarioId = securityUtils.getLoggedUserId();

        List<CalendarioResponseDTO> todos = new ArrayList<>();
        todos.addAll(calendarioService.listByUsuario(usuarioId));
        todos.addAll(compartilhamentoService.listSharedWithUsuario(usuarioId));

        return ResponseEntity.ok(todos);
    }

    @GetMapping("/meus/paginado")
    @Operation(
            summary = "Listar meus calendários (paginado)",
            description = "Retorna os calendários do usuário com paginação. " +
                    "Útil para listas grandes de calendários.",
            tags = {"Calendários"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Página de calendários retornada com sucesso.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Page<CalendarioResponseDTO>> listMeusPaginado(
            @Parameter(
                    description = "Configurações de paginação (page, size, sort)",
                    example = "page=0&size=10&sort=nome,asc"
            )
            Pageable pageable) {
        UUID usuarioId = securityUtils.getLoggedUserId();
        return ResponseEntity.ok(calendarioService.listByUsuarioPaginated(usuarioId, pageable));
    }

    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar calendários por nome",
            description = "Realiza uma busca de calendários cujo nome contenha o texto fornecido. " +
                    "A busca é case-insensitive.",
            tags = {"Calendários"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resultados da busca retornados com sucesso.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Page<CalendarioResponseDTO>> searchByNome(
            @Parameter(
                    description = "Nome ou parte do nome do calendário",
                    required = true,
                    example = "Trabalho"
            )
            @RequestParam String nome,
            @Parameter(
                    description = "Configurações de paginação"
            )
            Pageable pageable) {
        return ResponseEntity.ok(calendarioService.searchByNome(nome, pageable));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar calendário",
            description = "Atualiza as informações de um calendário. " +
                    "Apenas o proprietário do calendário pode realizar esta operação.",
            tags = {"Calendários"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Calendário atualizado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CalendarioResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado. Apenas o proprietário pode editar o calendário.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Calendário não encontrado.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<CalendarioResponseDTO> update(
            @Parameter(
                    description = "ID do calendário a ser atualizado",
                    required = true
            )
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Novos dados do calendário",
                    required = true
            )
            @RequestBody @Valid CalendarioRequestDTO dto) {
        UUID usuarioId = securityUtils.getLoggedUserId();

        if (!calendarioService.isOwner(id, usuarioId)) {
            throw new AccessDeniedException("Apenas o proprietário pode editar o calendário");
        }

        return ResponseEntity.ok(calendarioService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Excluir calendário",
            description = "Exclui permanentemente um calendário e todos os seus eventos. " +
                    "Apenas o proprietário pode realizar esta operação. " +
                    "Esta ação não pode ser desfeita.",
            tags = {"Calendários"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Calendário excluído com sucesso."
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado. Apenas o proprietário pode deletar o calendário.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Calendário não encontrado.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Void> delete(
            @Parameter(
                    description = "ID do calendário a ser excluído",
                    required = true
            )
            @PathVariable UUID id) {
        UUID usuarioId = securityUtils.getLoggedUserId();

        if (!calendarioService.isOwner(id, usuarioId)) {
            throw new AccessDeniedException("Apenas o proprietário pode deletar o calendário");
        }

        calendarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/posso-editar")
    @Operation(
            summary = "Verificar permissão de edição",
            description = "Verifica se o usuário autenticado tem permissão para editar eventos no calendário especificado. " +
                    "Retorna true se o usuário é proprietário ou possui permissão de edição.",
            tags = {"Calendários"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Verificação realizada com sucesso.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Calendário não encontrado.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Boolean> canEdit(
            @Parameter(
                    description = "ID do calendário",
                    required = true
            )
            @PathVariable UUID id) {
        UUID usuarioId = securityUtils.getLoggedUserId();
        return ResponseEntity.ok(compartilhamentoService.canEdit(id, usuarioId));
    }

    @GetMapping("/{id}/contar-eventos")
    @Operation(
            summary = "Contar eventos do calendário",
            description = "Retorna o número total de eventos cadastrados no calendário. " +
                    "O usuário deve ter acesso ao calendário para visualizar esta informação.",
            tags = {"Calendários"}
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
    public ResponseEntity<Long> countEventos(
            @Parameter(
                    description = "ID do calendário",
                    required = true
            )
            @PathVariable UUID id) {
        UUID usuarioId = securityUtils.getLoggedUserId();

        if (!compartilhamentoService.hasAccess(id, usuarioId)) {
            throw new AccessDeniedException("Você não tem acesso a este calendário");
        }

        return ResponseEntity.ok(calendarioService.countByUsuario(id));
    }
}