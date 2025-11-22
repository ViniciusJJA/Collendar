package projeto.collendar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projeto.collendar.dto.CalendarioDTO;
import projeto.collendar.model.Calendario;
import projeto.collendar.service.CalendarioService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/calendarios")
@RequiredArgsConstructor
@Tag(name = "Calendários", description = "Gerenciamento de calendários")
public class CalendarioController {

    private final CalendarioService calendarioService;

    @PostMapping
    @Operation(
            summary = "Criar novo calendário",
            description = "Cria um novo calendário associado a um usuário",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Calendário criado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
            }
    )
    public ResponseEntity<CalendarioDTO> criar(
            @RequestBody Calendario calendario,
            @Parameter(description = "ID do usuário proprietário", required = true)
            @RequestParam UUID usuarioId) {
        try {
            Calendario novoCalendario = calendarioService.criar(calendario, usuarioId);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(novoCalendario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar calendário por ID",
            description = "Retorna os dados de um calendário específico",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Calendário encontrado"),
                    @ApiResponse(responseCode = "404", description = "Calendário não encontrado")
            }
    )
    public ResponseEntity<CalendarioDTO> buscarPorId(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID id) {
        return calendarioService.buscarPorId(id)
                .map(calendario -> ResponseEntity.ok(toDTO(calendario)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(
            summary = "Listar todos os calendários",
            description = "Retorna lista de todos os calendários do sistema",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
            }
    )
    public ResponseEntity<List<CalendarioDTO>> listarTodos() {
        List<CalendarioDTO> calendarios = calendarioService.listarTodos()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(calendarios);
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(
            summary = "Listar calendários do usuário",
            description = "Retorna todos os calendários pertencentes a um usuário",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de calendários retornada")
            }
    )
    public ResponseEntity<List<CalendarioDTO>> listarPorUsuario(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable UUID usuarioId) {
        List<CalendarioDTO> calendarios = calendarioService.listarPorUsuario(usuarioId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(calendarios);
    }

    @GetMapping("/usuario/{usuarioId}/paginado")
    @Operation(
            summary = "Listar calendários do usuário (paginado)",
            description = "Retorna calendários do usuário com paginação",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Página retornada com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
            }
    )
    public ResponseEntity<Page<CalendarioDTO>> listarPorUsuarioPaginado(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable UUID usuarioId,
            @Parameter(description = "Parâmetros de paginação (page, size, sort)")
            Pageable pageable) {
        try {
            Page<CalendarioDTO> calendarios = calendarioService.listarPorUsuarioPaginado(usuarioId, pageable)
                    .map(this::toDTO);
            return ResponseEntity.ok(calendarios);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar calendários por nome",
            description = "Busca calendários que contenham o texto informado no nome",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resultados da busca retornados")})
    public ResponseEntity<Page<CalendarioDTO>> buscarPorNome(
            @Parameter(description = "Nome ou parte do nome do calendário", required = true)
            @RequestParam String nome,
            Pageable pageable) {
        Page<CalendarioDTO> calendarios = calendarioService.buscarPorNome(nome, pageable)
                .map(this::toDTO);
        return ResponseEntity.ok(calendarios);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar calendário",
            description = "Atualiza os dados de um calendário existente",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Calendário atualizado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Calendário não encontrado")})
    public ResponseEntity<CalendarioDTO> atualizar(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID id,
            @RequestBody Calendario calendario) {
        try {
            Calendario calendarioAtualizado = calendarioService.atualizar(id, calendario);
            return ResponseEntity.ok(toDTO(calendarioAtualizado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar calendário",
            description = "Remove permanentemente um calendário e todos os seus eventos",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Calendário deletado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Calendário não encontrado")})
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID id) {
        try {
            calendarioService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{calendarioId}/verificar-proprietario/{usuarioId}")
    @Operation(summary = "Verificar proprietário do calendário",
            description = "Verifica se um usuário é o proprietário de um calendário",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Verificação realizada"),
                    @ApiResponse(responseCode = "404", description = "Calendário não encontrado")})
    public ResponseEntity<Boolean> verificarProprietario(
            @Parameter(description = "ID do calendário", required = true)
            @PathVariable UUID calendarioId,
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable UUID usuarioId) {
        try {
            boolean ehProprietario = calendarioService.verificarProprietario(calendarioId, usuarioId);
            return ResponseEntity.ok(ehProprietario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/usuario/{usuarioId}/contar")
    @Operation(summary = "Contar calendários do usuário",
            description = "Retorna a quantidade total de calendários de um usuário",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Contagem realizada")})
    public ResponseEntity<Long> contarPorUsuario(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable UUID usuarioId) {
        long quantidade = calendarioService.contarPorUsuario(usuarioId);
        return ResponseEntity.ok(quantidade);
    }

    private CalendarioDTO toDTO(Calendario calendario) {
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