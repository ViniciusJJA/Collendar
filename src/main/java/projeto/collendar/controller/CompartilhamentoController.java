package projeto.collendar.controller;

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
@RequestMapping("/api/compartilhamentos")
@RequiredArgsConstructor
public class CompartilhamentoController {

    private final CompartilhamentoService compartilhamentoService;

    @PostMapping
    public ResponseEntity<CompartilhamentoDTO> compartilhar(
            @RequestParam UUID calendarioId,
            @RequestParam UUID usuarioId,
            @RequestParam TipoPermissao permissao) {
        try {
            Compartilhamento compartilhamento = compartilhamentoService.compartilhar(calendarioId, usuarioId, permissao);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(compartilhamento));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompartilhamentoDTO> buscarPorId(@PathVariable UUID id) {
        return compartilhamentoService.buscarPorId(id)
                .map(compartilhamento -> ResponseEntity.ok(toDTO(compartilhamento)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/calendario/{calendarioId}")
    public ResponseEntity<List<CompartilhamentoDTO>> listarPorCalendario(@PathVariable UUID calendarioId) {
        List<CompartilhamentoDTO> compartilhamentos = compartilhamentoService.listarPorCalendario(calendarioId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(compartilhamentos);
    }

    @GetMapping("/usuario/{usuarioId}/calendarios")
    public ResponseEntity<List<CalendarioDTO>> listarCalendariosCompartilhados(@PathVariable UUID usuarioId) {
        List<CalendarioDTO> calendarios = compartilhamentoService.listarCalendariosCompartilhados(usuarioId)
                .stream()
                .map(this::toCalendarioDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(calendarios);
    }

    @GetMapping("/usuario/{usuarioId}/recebidos")
    public ResponseEntity<List<CompartilhamentoDTO>> listarCompartilhamentosRecebidos(@PathVariable UUID usuarioId) {
        List<CompartilhamentoDTO> compartilhamentos = compartilhamentoService.listarCompartilhamentosRecebidos(usuarioId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(compartilhamentos);
    }

    @GetMapping("/calendario/{calendarioId}/usuario/{usuarioId}")
    public ResponseEntity<CompartilhamentoDTO> buscarCompartilhamento(
            @PathVariable UUID calendarioId,
            @PathVariable UUID usuarioId) {
        try {
            return compartilhamentoService.buscarCompartilhamento(calendarioId, usuarioId)
                    .map(compartilhamento -> ResponseEntity.ok(toDTO(compartilhamento)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/permissao")
    public ResponseEntity<CompartilhamentoDTO> atualizarPermissao(
            @PathVariable UUID id,
            @RequestParam TipoPermissao permissao) {
        try {
            Compartilhamento compartilhamento = compartilhamentoService.atualizarPermissao(id, permissao);
            return ResponseEntity.ok(toDTO(compartilhamento));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/calendario/{calendarioId}/usuario/{usuarioId}")
    public ResponseEntity<Void> removerCompartilhamento(
            @PathVariable UUID calendarioId,
            @PathVariable UUID usuarioId) {
        compartilhamentoService.removerCompartilhamento(calendarioId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        try {
            compartilhamentoService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/calendario/{calendarioId}/usuario/{usuarioId}/tem-acesso")
    public ResponseEntity<Boolean> temAcesso(
            @PathVariable UUID calendarioId,
            @PathVariable UUID usuarioId) {
        try {
            boolean temAcesso = compartilhamentoService.temAcesso(calendarioId, usuarioId);
            return ResponseEntity.ok(temAcesso);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/calendario/{calendarioId}/usuario/{usuarioId}/pode-editar")
    public ResponseEntity<Boolean> podeEditar(
            @PathVariable UUID calendarioId,
            @PathVariable UUID usuarioId) {
        try {
            boolean podeEditar = compartilhamentoService.podeEditar(calendarioId, usuarioId);
            return ResponseEntity.ok(podeEditar);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/calendario/{calendarioId}/contar")
    public ResponseEntity<Long> contarPorCalendario(@PathVariable UUID calendarioId) {
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