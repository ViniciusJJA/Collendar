package projeto.collendar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projeto.collendar.model.Role;
import projeto.collendar.repository.RoleRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleRepository roleRepository;

    @PostMapping
    public ResponseEntity<Role> criar(@RequestBody Role role) {
        try {
            if (roleRepository.existsByNome(role.getNome())) {
                return ResponseEntity.badRequest().build();
            }
            Role novaRole = roleRepository.save(role);
            return ResponseEntity.status(HttpStatus.CREATED).body(novaRole);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> buscarPorId(@PathVariable UUID id) {
        return roleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nome/{nome}")
    public ResponseEntity<Role> buscarPorNome(@PathVariable String nome) {
        return roleRepository.findByNome(nome)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Role>> listarTodas() {
        List<Role> roles = roleRepository.findAll();
        return ResponseEntity.ok(roles);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        if (!roleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        roleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/verificar/{nome}")
    public ResponseEntity<Boolean> verificarExistencia(@PathVariable String nome) {
        boolean existe = roleRepository.existsByNome(nome);
        return ResponseEntity.ok(existe);
    }
}