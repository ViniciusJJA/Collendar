package projeto.collendar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projeto.collendar.dto.UsuarioDTO;
import projeto.collendar.model.Usuario;
import projeto.collendar.service.UsuarioService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gerenciamento de usuários do sistema")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @Operation(summary = "Criar novo usuário",
            description = "Registra um novo usuário no sistema",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Email já cadastrado ou dados inválidos")})
    public ResponseEntity<UsuarioDTO> criar(@RequestBody Usuario usuario) {
        try {
            Usuario novoUsuario = usuarioService.criar(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(novoUsuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID",
            description = "Retorna os dados de um usuário específico",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")})
    public ResponseEntity<UsuarioDTO> buscarPorId(@PathVariable UUID id) {
        return usuarioService.buscarPorId(id)
                .map(usuario -> ResponseEntity.ok(toDTO(usuario)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Buscar usuário por email",
            description = "Retorna os dados de um usuário pelo email",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")})
    public ResponseEntity<UsuarioDTO> buscarPorEmail(@PathVariable String email) {
        return usuarioService.buscarPorEmail(email)
                .map(usuario -> ResponseEntity.ok(toDTO(usuario)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Listar todos os usuários",
            description = "Retorna lista completa de usuários cadastrados",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de usuários retornada")})
    public ResponseEntity<List<UsuarioDTO>> listarTodos() {
        List<UsuarioDTO> usuarios = usuarioService.listarTodos()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/ativos")
    @Operation(summary = "Listar usuários ativos",
            description = "Retorna apenas usuários com status ativo",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de usuários ativos")})
    public ResponseEntity<List<UsuarioDTO>> listarAtivos() {
        List<UsuarioDTO> usuarios = usuarioService.listarAtivos()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usuarios);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário",
            description = "Atualiza os dados de um usuário existente",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos ou email já cadastrado"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")})
    public ResponseEntity<UsuarioDTO> atualizar(@PathVariable UUID id, @RequestBody Usuario usuario) {
        try {
            Usuario usuarioAtualizado = usuarioService.atualizar(id, usuario);
            return ResponseEntity.ok(toDTO(usuarioAtualizado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/desativar")
    @Operation(summary = "Desativar usuário",
            description = "Desativa um usuário sem removê-lo do sistema",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Usuário desativado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")})
    public ResponseEntity<Void> desativar(@PathVariable UUID id) {
        try {
            usuarioService.desativar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar usuário",
            description = "Remove permanentemente um usuário do sistema",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")})
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        try {
            usuarioService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/roles")
    @Operation(summary = "Adicionar role ao usuário")
    public ResponseEntity<UsuarioDTO> adicionarRole(
            @PathVariable UUID id,
            @RequestParam String role) {
        try {
            Usuario usuario = usuarioService.adicionarRole(id, role);
            return ResponseEntity.ok(toDTO(usuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private UsuarioDTO toDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setEmail(usuario.getEmail());
        dto.setAtivo(usuario.getAtivo());
        dto.setRoles(usuario.getRoles().stream()
                .map(role -> role.getNome())
                .collect(Collectors.toSet()));
        return dto;
    }
}