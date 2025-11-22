package projeto.collendar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projeto.collendar.dto.CalendarioDTO;
import projeto.collendar.dto.CompartilhamentoDTO;
import projeto.collendar.enums.TipoPermissao;
import projeto.collendar.model.Calendario;
import projeto.collendar.model.Compartilhamento;
import projeto.collendar.service.CompartilhamentoService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/compartilhamentos")
@RequiredArgsConstructor
@Tag(name = "Compartilhamentos", description = "Gerenciamento de compartilhamento de calendários entre usuários")
public class CompartilhamentoController {

    private final CompartilhamentoService compartilhamentoService;

    @PostMapping
    @Operation(
            summary = "Compartilhar calendário",
            description = "Compartilha um calendário com outro usuário definindo o tipo de permissão (VISUALIZAR ou EDITAR)",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Calendário compartilhado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Calendário já compartilhado ou tentativa de compartilhar consigo mesmo"),
                    @ApiResponse(responseCode = "404", description = "Calendário ou usuário não encontrado")
            }
    )
    public ResponseEntity<CompartilhamentoDTO> compartilhar(
            @Parameter(description = "ID do calendário a ser compartilhado", required = true)
            @RequestParam UUID calendarioId,
            @Parameter(description = "ID do usuário que receberá o compartilhamento", required = true)
            @RequestParam UUID usuarioId,
            @Parameter(description = "Tipo de permissão: VISUALIZAR ou EDITAR", required = true)
            @RequestParam TipoPermissao permissao) {
        try {
            Compartilhamento compartilhamento = compartilhamentoService.compartilhar(calendarioId, usuarioId, permissao);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(compartilhamento));
        } catch (IllegalArgumentException e) {
            // Se a mensagem contém "não encontrado", retorna 404, senão 400
            if (e.getMessage().toLowerCase().contains("não encontrado")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar compartilhamento por ID",
            description = "Retorna os dados de um compartilhamento específico",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Compartilhamento encontrado"),
                    @ApiResponse(responseCode = "404", description = "Compartilhamento não encontrado")
            }
    )
    public ResponseEntity<CompartilhamentoDTO> buscarPorId(
            @Parameter(description = "ID do compartilhamento", required = true)
            @PathVariable UUID id) {
        return compartilhamentoService.buscarPorId(id)
                .map(compartilhamento -> ResponseEntity.ok(toDTO(compartilhamento)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/calendario/{calendarioId}")
    @Operation(
            summary = "Listar compartilhamentos do calendário",
            description = "Lista todos os usuários com quem o calendário foi compartilhado",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de compartilhamentos retornada")
            }
    )
    public ResponseEntity<List<CompartilhamentoDTO>> listarPorCalendario(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID calendarioId) {
        List<CompartilhamentoDTO> compartilhamentos = compartilhamentoService.listarPorCalendario(calendarioId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(compartilhamentos);
    }

    @GetMapping("/usuario/{usuarioId}/calendarios")
    @Operation(
            summary = "Listar calendários compartilhados com o usuário",
            description = "Retorna todos os calendários que foram compartilhados com o usuário",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de calendários compartilhados")
            }
    )
    public ResponseEntity<List<CalendarioDTO>> listarCalendariosCompartilhados(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable UUID usuarioId) {
        List<CalendarioDTO> calendarios = compartilhamentoService.listarCalendariosCompartilhados(usuarioId)
                .stream()
                .map(this::toCalendarioDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(calendarios);
    }

    @GetMapping("/usuario/{usuarioId}/recebidos")
    @Operation(
            summary = "Listar compartilhamentos recebidos",
            description = "Lista todos os compartilhamentos que o usuário recebeu",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de compartilhamentos recebidos")
            }
    )
    public ResponseEntity<List<CompartilhamentoDTO>> listarCompartilhamentosRecebidos(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable UUID usuarioId) {
        List<CompartilhamentoDTO> compartilhamentos = compartilhamentoService.listarCompartilhamentosRecebidos(usuarioId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(compartilhamentos);
    }

    @GetMapping("/calendario/{calendarioId}/usuario/{usuarioId}")
    @Operation(
            summary = "Buscar compartilhamento específico",
            description = "Busca o compartilhamento de um calendário com um usuário específico",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Compartilhamento encontrado"),
                    @ApiResponse(responseCode = "404", description = "Compartilhamento, calendário ou usuário não encontrado")
            }
    )
    public ResponseEntity<CompartilhamentoDTO> buscarCompartilhamento(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID calendarioId,
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable UUID usuarioId) {
        try {
            return compartilhamentoService.buscarCompartilhamento(calendarioId, usuarioId)
                    .map(compartilhamento -> ResponseEntity.ok(toDTO(compartilhamento)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/permissao")
    @Operation(
            summary = "Atualizar permissão do compartilhamento",
            description = "Altera o tipo de permissão de um compartilhamento existente",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Permissão atualizada com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Compartilhamento não encontrado")
            }
    )
    public ResponseEntity<CompartilhamentoDTO> atualizarPermissao(
            @Parameter(description = "ID do compartilhamento", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Nova permissão (VISUALIZAR ou EDITAR)", required = true)
            @RequestParam TipoPermissao permissao) {
        try {
            Compartilhamento compartilhamento = compartilhamentoService.atualizarPermissao(id, permissao);
            return ResponseEntity.ok(toDTO(compartilhamento));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/calendario/{calendarioId}/usuario/{usuarioId}")
    @Operation(
            summary = "Remover compartilhamento",
            description = "Remove o compartilhamento de um calendário com um usuário específico",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Compartilhamento removido com sucesso")
            }
    )
    public ResponseEntity<Void> removerCompartilhamento(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID calendarioId,
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable UUID usuarioId) {
        compartilhamentoService.removerCompartilhamento(calendarioId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Deletar compartilhamento",
            description = "Remove um compartilhamento pelo ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Compartilhamento deletado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Compartilhamento não encontrado")
            }
    )
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do compartilhamento", required = true)
            @PathVariable UUID id) {
        try {
            compartilhamentoService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/calendario/{calendarioId}/usuario/{usuarioId}/tem-acesso")
    @Operation(
            summary = "Verificar acesso ao calendário",
            description = "Verifica se um usuário tem acesso a um calendário (proprietário ou compartilhado)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Verificação realizada"),
                    @ApiResponse(responseCode = "404", description = "Calendário ou usuário não encontrado")
            }
    )
    public ResponseEntity<Boolean> temAcesso(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID calendarioId,
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable UUID usuarioId) {
        try {
            boolean temAcesso = compartilhamentoService.temAcesso(calendarioId, usuarioId);
            return ResponseEntity.ok(temAcesso);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/calendario/{calendarioId}/usuario/{usuarioId}/pode-editar")
    @Operation(
            summary = "Verificar permissão de edição",
            description = "Verifica se um usuário pode editar um calendário (proprietário ou permissão EDITAR)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Verificação realizada"),
                    @ApiResponse(responseCode = "404", description = "Calendário ou usuário não encontrado")
            }
    )
    public ResponseEntity<Boolean> podeEditar(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID calendarioId,
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable UUID usuarioId) {
        try {
            boolean podeEditar = compartilhamentoService.podeEditar(calendarioId, usuarioId);
            return ResponseEntity.ok(podeEditar);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/calendario/{calendarioId}/contar")
    @Operation(
            summary = "Contar compartilhamentos do calendário",
            description = "Retorna a quantidade de usuários com quem o calendário foi compartilhado",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Contagem realizada")
            }
    )
    public ResponseEntity<Long> contarPorCalendario(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID calendarioId) {
        long quantidade = compartilhamentoService.contarPorCalendario(calendarioId);
        return ResponseEntity.ok(quantidade);
    }

    private CompartilhamentoDTO toDTO(Compartilhamento compartilhamento) {
        CompartilhamentoDTO dto = new CompartilhamentoDTO();
        dto.setId(compartilhamento.getId());
        dto.setCalendarioId(compartilhamento.getCalendario().getId());
        dto.setCalendarioNome(compartilhamento.getCalendario().getNome());
        dto.setUsuarioId(compartilhamento.getUsuario().getId());
        dto.setUsuarioNome(compartilhamento.getUsuario().getNome());
        dto.setPermissao(compartilhamento.getPermissao());
        dto.setCreatedAt(compartilhamento.getCreatedAt());
        return dto;
    }

    private CalendarioDTO toCalendarioDTO(Calendario calendario) {
        CalendarioDTO dto = new CalendarioDTO();
        dto.setId(calendario.getId());
        dto.setNome(calendario.getNome());
        dto.setDescricao(calendario.getDescricao());
        dto.setCor(calendario.getCor());
        dto.setUsuarioId(calendario.getUsuario().getId());
        dto.setUsuarioNome(calendario.getUsuario().getNome());
        dto.setCreatedAt(calendario.getCreatedAt());
        dto.setUpdatedAt(calendario.getUpdatedAt());
        return dto;
    }
}