package projeto.collendar.controller;

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
@RequestMapping("/api/calendarios")
@RequiredArgsConstructor
public class CalendarioController {

    private final CalendarioService calendarioService;

    @PostMapping
    public ResponseEntity<CalendarioDTO> criar(@RequestBody Calendario calendario, @RequestParam UUID usuarioId) {
        try {
            Calendario novoCalendario = calendarioService.criar(calendario, usuarioId);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(novoCalendario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CalendarioDTO> buscarPorId(@PathVariable UUID id) {
        return calendarioService.buscarPorId(id)
                .map(calendario -> ResponseEntity.ok(toDTO(calendario)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CalendarioDTO>> listarTodos() {
        List<CalendarioDTO> calendarios = calendarioService.listarTodos()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(calendarios);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<CalendarioDTO>> listarPorUsuario(@PathVariable UUID usuarioId) {
        List<CalendarioDTO> calendarios = calendarioService.listarPorUsuario(usuarioId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(calendarios);
    }

    @GetMapping("/usuario/{usuarioId}/paginado")
    public ResponseEntity<Page<CalendarioDTO>> listarPorUsuarioPaginado(
            @PathVariable UUID usuarioId,
            Pageable pageable) {
        try {
            Page<CalendarioDTO> calendarios = calendarioService.listarPorUsuarioPaginado(usuarioId, pageable)
                    .map(this::toDTO);
            return ResponseEntity.ok(calendarios);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<CalendarioDTO>> buscarPorNome(
            @RequestParam String nome,
            Pageable pageable) {
        Page<CalendarioDTO> calendarios = calendarioService.buscarPorNome(nome, pageable)
                .map(this::toDTO);
        return ResponseEntity.ok(calendarios);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CalendarioDTO> atualizar(@PathVariable UUID id, @RequestBody Calendario calendario) {
        try {
            Calendario calendarioAtualizado = calendarioService.atualizar(id, calendario);
            return ResponseEntity.ok(toDTO(calendarioAtualizado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        try {
            calendarioService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{calendarioId}/verificar-proprietario/{usuarioId}")
    public ResponseEntity<Boolean> verificarProprietario(
            @PathVariable UUID calendarioId,
            @PathVariable UUID usuarioId) {
        try {
            boolean ehProprietario = calendarioService.verificarProprietario(calendarioId, usuarioId);
            return ResponseEntity.ok(ehProprietario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/usuario/{usuarioId}/contar")
    public ResponseEntity<Long> contarPorUsuario(@PathVariable UUID usuarioId) {
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