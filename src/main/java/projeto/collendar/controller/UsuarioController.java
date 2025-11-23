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
import projeto.collendar.dtos.request.UsuarioRequestDTO;
import projeto.collendar.dtos.response.UsuarioResponseDTO;
import projeto.collendar.service.UsuarioService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gerenciamento de usuários do sistema")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @Operation(
            summary = "Criar novo usuário",
            description = "Registra um novo usuário no sistema",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso",
                            content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos ou email já cadastrado")
            }
    )
    public ResponseEntity<UsuarioResponseDTO> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do usuário",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UsuarioRequestDTO.class))
            )
            @RequestBody @Valid UsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.create(dto));
    }

    @GetMapping
    @Operation(summary = "Listar todos os usuários")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<List<UsuarioResponseDTO>> listAll() {
        return ResponseEntity.ok(usuarioService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar usuário por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
            }
    )
    public ResponseEntity<UsuarioResponseDTO> findById(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(usuarioService.findById(id));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Buscar usuário por email")
    public ResponseEntity<UsuarioResponseDTO> findByEmail(
            @Parameter(description = "Email do usuário", required = true)
            @PathVariable String email) {
        return ResponseEntity.ok(usuarioService.findByEmail(email));
    }

    @GetMapping("/ativos")
    @Operation(summary = "Listar usuários ativos")
    public ResponseEntity<List<UsuarioResponseDTO>> listAtivos() {
        return ResponseEntity.ok(usuarioService.listAtivos());
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar usuário",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuário atualizado"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
            }
    )
    public ResponseEntity<UsuarioResponseDTO> update(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable UUID id,
            @RequestBody @Valid UsuarioRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.update(id, dto));
    }

    @PatchMapping("/{id}/desativar")
    @Operation(summary = "Desativar usuário")
    @ApiResponse(responseCode = "204", description = "Usuário desativado")
    public ResponseEntity<Void> deactivate(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable UUID id) {
        usuarioService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar usuário")
    @ApiResponse(responseCode = "204", description = "Usuário deletado")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable UUID id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/roles")
    @Operation(summary = "Adicionar role ao usuário")
    public ResponseEntity<UsuarioResponseDTO> addRole(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Nome da role", required = true)
            @RequestParam String role) {
        return ResponseEntity.ok(usuarioService.addRole(id, role));
    }
}