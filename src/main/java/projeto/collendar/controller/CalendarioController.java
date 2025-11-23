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
@Tag(name = "Calendários", description = "Gerenciamento de calendários")
public class CalendarioController {

    private final CalendarioService calendarioService;
    private final CompartilhamentoService compartilhamentoService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(
            summary = "Criar novo calendário",
            description = "Cria um calendário para o usuário logado",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Calendário criado",
                            content = @Content(schema = @Schema(implementation = CalendarioResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos")
            }
    )
    public ResponseEntity<CalendarioResponseDTO> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do calendário",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CalendarioRequestDTO.class))
            )
            @RequestBody @Valid CalendarioRequestDTO dto) {
        UUID usuarioId = securityUtils.getLoggedUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(calendarioService.create(dto, usuarioId));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar calendário por ID",
            description = "Retorna se o usuário for dono ou tiver acesso",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Calendário encontrado"),
                    @ApiResponse(responseCode = "403", description = "Sem acesso"),
                    @ApiResponse(responseCode = "404", description = "Não encontrado")
            }
    )
    public ResponseEntity<CalendarioResponseDTO> findById(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID id) {
        UUID usuarioId = securityUtils.getLoggedUserId();

        if (!compartilhamentoService.hasAccess(id, usuarioId)) {
            throw new AccessDeniedException("Você não tem acesso a este calendário");
        }

        return ResponseEntity.ok(calendarioService.findById(id));
    }

    @GetMapping("/meus")
    @Operation(summary = "Listar meus calendários", description = "Calendários que o usuário é dono")
    public ResponseEntity<List<CalendarioResponseDTO>> listMeus() {
        UUID usuarioId = securityUtils.getLoggedUserId();
        return ResponseEntity.ok(calendarioService.listByUsuario(usuarioId));
    }

    @GetMapping("/acessiveis")
    @Operation(summary = "Listar calendários acessíveis", description = "Próprios + compartilhados")
    public ResponseEntity<List<CalendarioResponseDTO>> listAcessiveis() {
        UUID usuarioId = securityUtils.getLoggedUserId();

        List<CalendarioResponseDTO> todos = new ArrayList<>();
        todos.addAll(calendarioService.listByUsuario(usuarioId));
        todos.addAll(compartilhamentoService.listSharedWithUsuario(usuarioId));

        return ResponseEntity.ok(todos);
    }

    @GetMapping("/meus/paginado")
    @Operation(summary = "Listar meus calendários (paginado)")
    public ResponseEntity<Page<CalendarioResponseDTO>> listMeusPaginado(Pageable pageable) {
        UUID usuarioId = securityUtils.getLoggedUserId();
        return ResponseEntity.ok(calendarioService.listByUsuarioPaginated(usuarioId, pageable));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar calendários por nome")
    public ResponseEntity<Page<CalendarioResponseDTO>> searchByNome(
            @Parameter(description = "Nome ou parte do nome", required = true)
            @RequestParam String nome,
            Pageable pageable) {
        return ResponseEntity.ok(calendarioService.searchByNome(nome, pageable));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar calendário",
            description = "Apenas o dono pode atualizar",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Atualizado"),
                    @ApiResponse(responseCode = "403", description = "Sem permissão"),
                    @ApiResponse(responseCode = "404", description = "Não encontrado")
            }
    )
    public ResponseEntity<CalendarioResponseDTO> update(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID id,
            @RequestBody @Valid CalendarioRequestDTO dto) {
        UUID usuarioId = securityUtils.getLoggedUserId();

        if (!calendarioService.isOwner(id, usuarioId)) {
            throw new AccessDeniedException("Apenas o proprietário pode editar o calendário");
        }

        return ResponseEntity.ok(calendarioService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Deletar calendário",
            description = "Apenas o dono pode deletar"
    )
    @ApiResponse(responseCode = "204", description = "Deletado")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID id) {
        UUID usuarioId = securityUtils.getLoggedUserId();

        if (!calendarioService.isOwner(id, usuarioId)) {
            throw new AccessDeniedException("Apenas o proprietário pode deletar o calendário");
        }

        calendarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/posso-editar")
    @Operation(summary = "Verificar se pode editar eventos no calendário")
    public ResponseEntity<Boolean> canEdit(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID id) {
        UUID usuarioId = securityUtils.getLoggedUserId();
        return ResponseEntity.ok(compartilhamentoService.canEdit(id, usuarioId));
    }

    @GetMapping("/{id}/contar-eventos")
    @Operation(summary = "Contar eventos do calendário")
    public ResponseEntity<Long> countEventos(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID id) {
        UUID usuarioId = securityUtils.getLoggedUserId();

        if (!compartilhamentoService.hasAccess(id, usuarioId)) {
            throw new AccessDeniedException("Você não tem acesso a este calendário");
        }

        return ResponseEntity.ok(calendarioService.countByUsuario(id));
    }
}