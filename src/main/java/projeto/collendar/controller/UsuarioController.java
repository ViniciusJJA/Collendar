package projeto.collendar.controller;

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
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioDTO> criar(@RequestBody Usuario usuario) {
        try {
            Usuario novoUsuario = usuarioService.criar(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(novoUsuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> buscarPorId(@PathVariable UUID id) {
        return usuarioService.buscarPorId(id)
                .map(usuario -> ResponseEntity.ok(toDTO(usuario)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UsuarioDTO> buscarPorEmail(@PathVariable String email) {
        return usuarioService.buscarPorEmail(email)
                .map(usuario -> ResponseEntity.ok(toDTO(usuario)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listarTodos() {
        List<UsuarioDTO> usuarios = usuarioService.listarTodos()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<UsuarioDTO>> listarAtivos() {
        List<UsuarioDTO> usuarios = usuarioService.listarAtivos()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usuarios);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> atualizar(@PathVariable UUID id, @RequestBody Usuario usuario) {
        try {
            Usuario usuarioAtualizado = usuarioService.atualizar(id, usuario);
            return ResponseEntity.ok(toDTO(usuarioAtualizado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable UUID id) {
        try {
            usuarioService.desativar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        try {
            usuarioService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/roles/{nomeRole}")
    public ResponseEntity<UsuarioDTO> adicionarRole(@PathVariable UUID id, @PathVariable String nomeRole) {
        try {
            Usuario usuario = usuarioService.adicionarRole(id, nomeRole);
            return ResponseEntity.ok(toDTO(usuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}/roles/{nomeRole}")
    public ResponseEntity<UsuarioDTO> removerRole(@PathVariable UUID id, @PathVariable String nomeRole) {
        try {
            Usuario usuario = usuarioService.removerRole(id, nomeRole);
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