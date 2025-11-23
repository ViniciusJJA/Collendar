package projeto.collendar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@Tag(name = "Compartilhamentos", description = "Gerenciamento de compartilhamento de calendários")
public class CompartilhamentoController {

    private final CompartilhamentoService compartilhamentoService;
    private final CalendarioService calendarioService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(
            summary = "Compartilhar calendário",
            description = "Apenas o dono pode compartilhar",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Compartilhado com sucesso",
                            content = @Content(schema = @Schema(implementation = CompartilhamentoResponseDTO.class))),
                    @ApiResponse(responseCode = "403", description = "Apenas o dono pode compartilhar"),
                    @ApiResponse(responseCode = "400", description = "Já compartilhado ou dados inválidos")
            }
    )
    public ResponseEntity<CompartilhamentoResponseDTO> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do compartilhamento",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CompartilhamentoRequestDTO.class))
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
            description = "Apenas o dono pode ver"
    )
    public ResponseEntity<List<CompartilhamentoResponseDTO>> listByCalendario(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID calendarioId) {
        UUID usuarioId = securityUtils.getLoggedUserId();

        if (!calendarioService.isOwner(calendarioId, usuarioId)) {
            throw new AccessDeniedException("Apenas o proprietário pode ver os compartilhamentos");
        }

        return ResponseEntity.ok(compartilhamentoService.listByCalendario(calendarioId));
    }

    @GetMapping("/recebidos")
    @Operation(summary = "Listar calendários compartilhados comigo")
    public ResponseEntity<List<CalendarioResponseDTO>> listRecebidos() {
        UUID usuarioId = securityUtils.getLoggedUserId();
        return ResponseEntity.ok(compartilhamentoService.listSharedWithUsuario(usuarioId));
    }

    @GetMapping("/recebidos/detalhes")
    @Operation(summary = "Listar detalhes dos compartilhamentos recebidos")
    public ResponseEntity<List<CompartilhamentoResponseDTO>> listRecebidosDetalhes() {
        UUID usuarioId = securityUtils.getLoggedUserId();
        return ResponseEntity.ok(compartilhamentoService.listReceivedByUsuario(usuarioId));
    }

    @GetMapping("/calendario/{calendarioId}/minha-permissao")
    @Operation(summary = "Ver minha permissão em um calendário")
    public ResponseEntity<PermissaoResponseDTO> getMyPermission(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID calendarioId) {
        UUID usuarioId = securityUtils.getLoggedUserId();
        return ResponseEntity.ok(compartilhamentoService.getMyPermission(calendarioId, usuarioId));
    }

    @PatchMapping("/{id}/permissao")
    @Operation(
            summary = "Atualizar permissão",
            description = "Apenas o dono do calendário pode alterar"
    )
    public ResponseEntity<CompartilhamentoResponseDTO> updatePermissao(
            @Parameter(description = "ID do compartilhamento", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Nova permissão", required = true)
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
            description = "Dono do calendário ou destinatário podem remover"
    )
    @ApiResponse(responseCode = "204", description = "Removido")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do compartilhamento", required = true)
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
    @Operation(summary = "Contar compartilhamentos do calendário")
    public ResponseEntity<Long> countByCalendario(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID calendarioId) {
        UUID usuarioId = securityUtils.getLoggedUserId();

        if (!calendarioService.isOwner(calendarioId, usuarioId)) {
            throw new AccessDeniedException("Apenas o proprietário pode ver esta informação");
        }

        return ResponseEntity.ok(compartilhamentoService.countByCalendario(calendarioId));
    }
}