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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projeto.collendar.dtos.request.CompartilhamentoRequestDTO;
import projeto.collendar.dtos.response.CalendarioResponseDTO;
import projeto.collendar.dtos.response.CompartilhamentoResponseDTO;
import projeto.collendar.dtos.response.PermissaoResponseDTO;
import projeto.collendar.enums.TipoPermissao;
import projeto.collendar.exception.AccessDeniedException;
import projeto.collendar.service.CalendarioService;
import projeto.collendar.service.CompartilhamentoService;
import projeto.collendar.utils.SecurityUtils;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/compartilhamentos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(
        name = "Compartilhamentos",
        description = "Endpoints para gerenciamento de compartilhamento de calendários. " +
                "Permite compartilhar calendários com outros usuários, gerenciar permissões " +
                "(VISUALIZAR ou EDITAR) e consultar calendários compartilhados."
)
public class CompartilhamentoController {

    private final CompartilhamentoService compartilhamentoService;
    private final CalendarioService calendarioService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(
            summary = "Compartilhar calendário",
            description = "Compartilha um calendário com outro usuário, definindo o nível de permissão. " +
                    "Apenas o proprietário do calendário pode realizar compartilhamentos. " +
                    "Permissões disponíveis: VISUALIZAR (apenas leitura) ou EDITAR (criar/editar/excluir eventos).",
            tags = {"Compartilhamentos"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Calendário compartilhado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CompartilhamentoResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos. Possíveis causas: calendário já compartilhado com o usuário, " +
                            "tentativa de compartilhar consigo mesmo, email não encontrado.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado. Apenas o proprietário pode compartilhar o calendário.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Calendário ou usuário destinatário não encontrado.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<CompartilhamentoResponseDTO> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do compartilhamento (calendário, destinatário e permissão)",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CompartilhamentoRequestDTO.class),
                            mediaType = "application/json"
                    )
            )
            @RequestBody @Valid CompartilhamentoRequestDTO dto) {
        UUID usuarioId = securityUtils.getLoggedUserId();

        if (!calendarioService.isOwner(dto.calendarioId(), usuarioId)) {
            throw new AccessDeniedException("Apenas o proprietário pode compartilhar o calendário");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(compartilhamentoService.create(dto));
    }

    @GetMapping("/calendario/{calendarioId}")
    @Operation(
            summary = "Listar compartilhamentos do calendário",
            description = "Retorna todos os compartilhamentos de um calendário específico, " +
                    "mostrando com quais usuários ele foi compartilhado e suas respectivas permissões. " +
                    "Apenas o proprietário pode visualizar esta informação.",
            tags = {"Compartilhamentos"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de compartilhamentos retornada com sucesso.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado. Apenas o proprietário pode ver os compartilhamentos.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Calendário não encontrado.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<CompartilhamentoResponseDTO>> listByCalendario(
            @Parameter(
                    description = "ID do calendário",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID calendarioId) {
        UUID usuarioId = securityUtils.getLoggedUserId();

        if (!calendarioService.isOwner(calendarioId, usuarioId)) {
            throw new AccessDeniedException("Apenas o proprietário pode ver os compartilhamentos");
        }

        return ResponseEntity.ok(compartilhamentoService.listByCalendario(calendarioId));
    }

    @GetMapping("/recebidos")
    @Operation(
            summary = "Listar calendários compartilhados comigo",
            description = "Retorna todos os calendários que foram compartilhados com o usuário autenticado. " +
                    "Útil para visualizar calendários de outras pessoas aos quais você tem acesso.",
            tags = {"Compartilhamentos"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de calendários compartilhados retornada com sucesso.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<CalendarioResponseDTO>> listRecebidos() {
        UUID usuarioId = securityUtils.getLoggedUserId();
        return ResponseEntity.ok(compartilhamentoService.listSharedWithUsuario(usuarioId));
    }

    @GetMapping("/recebidos/detalhes")
    @Operation(
            summary = "Listar detalhes dos compartilhamentos recebidos",
            description = "Retorna informações detalhadas sobre os compartilhamentos recebidos, " +
                    "incluindo quem compartilhou, quando e qual permissão foi concedida.",
            tags = {"Compartilhamentos"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de compartilhamentos detalhados retornada com sucesso.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<CompartilhamentoResponseDTO>> listRecebidosDetalhes() {
        UUID usuarioId = securityUtils.getLoggedUserId();
        return ResponseEntity.ok(compartilhamentoService.listReceivedByUsuario(usuarioId));
    }

    @GetMapping("/calendario/{calendarioId}/minha-permissao")
    @Operation(
            summary = "Consultar minha permissão em um calendário",
            description = "Retorna o nível de permissão do usuário autenticado em um calendário específico. " +
                    "Informa se o usuário é proprietário, pode visualizar ou pode editar o calendário.",
            tags = {"Compartilhamentos"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Permissão retornada com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PermissaoResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Calendário não encontrado.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<PermissaoResponseDTO> getMyPermission(
            @Parameter(
                    description = "ID do calendário",
                    required = true
            )
            @PathVariable UUID calendarioId) {
        UUID usuarioId = securityUtils.getLoggedUserId();
        return ResponseEntity.ok(compartilhamentoService.getMyPermission(calendarioId, usuarioId));
    }

    @PatchMapping("/{id}/permissao")
    @Operation(
            summary = "Atualizar permissão de compartilhamento",
            description = "Altera o nível de permissão de um compartilhamento existente. " +
                    "Apenas o proprietário do calendário pode alterar permissões. " +
                    "Pode alterar entre VISUALIZAR e EDITAR.",
            tags = {"Compartilhamentos"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Permissão atualizada com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CompartilhamentoResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado. Apenas o proprietário pode alterar permissões.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compartilhamento não encontrado.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<CompartilhamentoResponseDTO> updatePermissao(
            @Parameter(
                    description = "ID do compartilhamento",
                    required = true
            )
            @PathVariable UUID id,
            @Parameter(
                    description = "Nova permissão a ser aplicada",
                    required = true,
                    schema = @Schema(implementation = TipoPermissao.class)
            )
            @RequestParam TipoPermissao permissao) {
        UUID usuarioId = securityUtils.getLoggedUserId();
        UUID calendarioId = compartilhamentoService.getCalendarioIdByCompartilhamento(id);

        if (!calendarioService.isOwner(calendarioId, usuarioId)) {
            throw new AccessDeniedException("Apenas o proprietário pode alterar permissões");
        }

        return ResponseEntity.ok(compartilhamentoService.updatePermissao(id, permissao));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Remover compartilhamento",
            description = "Remove um compartilhamento de calendário. " +
                    "Tanto o proprietário do calendário quanto o destinatário do compartilhamento " +
                    "podem remover o compartilhamento. " +
                    "Útil para quando o usuário não quer mais ter acesso ao calendário compartilhado.",
            tags = {"Compartilhamentos"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Compartilhamento removido com sucesso."
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado. Você não tem permissão para remover este compartilhamento.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compartilhamento não encontrado.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Void> delete(
            @Parameter(
                    description = "ID do compartilhamento a ser removido",
                    required = true
            )
            @PathVariable UUID id) {
        UUID usuarioId = securityUtils.getLoggedUserId();
        UUID calendarioId = compartilhamentoService.getCalendarioIdByCompartilhamento(id);
        UUID destinatarioId = compartilhamentoService.getDestinatarioIdByCompartilhamento(id);

        boolean isOwner = calendarioService.isOwner(calendarioId, usuarioId);
        boolean isDestinatario = destinatarioId.equals(usuarioId);

        if (!isOwner && !isDestinatario) {
            throw new AccessDeniedException("Você não tem permissão para remover este compartilhamento");
        }

        compartilhamentoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/calendario/{calendarioId}/contar")
    @Operation(
            summary = "Contar compartilhamentos do calendário",
            description = "Retorna o número total de usuários com quem o calendário foi compartilhado. " +
                    "Apenas o proprietário pode visualizar esta informação.",
            tags = {"Compartilhamentos"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contagem realizada com sucesso.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado. Apenas o proprietário pode ver esta informação.",
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

        if (!calendarioService.isOwner(calendarioId, usuarioId)) {
            throw new AccessDeniedException("Apenas o proprietário pode ver esta informação");
        }

        return ResponseEntity.ok(compartilhamentoService.countByCalendario(calendarioId));
    }
}