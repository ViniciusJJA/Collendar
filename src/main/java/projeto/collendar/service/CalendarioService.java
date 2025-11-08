package projeto.collendar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projeto.collendar.model.Calendario;
import projeto.collendar.model.Usuario;
import projeto.collendar.repository.CalendarioRepository;
import projeto.collendar.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalendarioService {

    private final CalendarioRepository calendarioRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Calendario criar(Calendario calendario, UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        calendario.setUsuario(usuario);
        return calendarioRepository.save(calendario);
    }

    public Optional<Calendario> buscarPorId(UUID id) {
        return calendarioRepository.findById(id);
    }

    public List<Calendario> listarTodos() {
        return calendarioRepository.findAll();
    }

    public List<Calendario> listarPorUsuario(UUID usuarioId) {
        return calendarioRepository.findByUsuarioId(usuarioId);
    }

    public Page<Calendario> listarPorUsuarioPaginado(UUID usuarioId, Pageable pageable) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return calendarioRepository.findByUsuario(usuario, pageable);
    }

    public Page<Calendario> buscarPorNome(String nome, Pageable pageable) {
        return calendarioRepository.findByNomeContainingIgnoreCase(nome, pageable);
    }

    @Transactional
    public Calendario atualizar(UUID id, Calendario calendarioAtualizado) {
        Calendario calendario = calendarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Calendário não encontrado"));

        calendario.setNome(calendarioAtualizado.getNome());
        calendario.setDescricao(calendarioAtualizado.getDescricao());
        calendario.setCor(calendarioAtualizado.getCor());

        return calendarioRepository.save(calendario);
    }

    @Transactional
    public void deletar(UUID id) {
        if (!calendarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Calendário não encontrado");
        }
        calendarioRepository.deleteById(id);
    }

    public boolean verificarProprietario(UUID calendarioId, UUID usuarioId) {
        Calendario calendario = calendarioRepository.findById(calendarioId)
                .orElseThrow(() -> new IllegalArgumentException("Calendário não encontrado"));
        return calendario.getUsuario().getId().equals(usuarioId);
    }

    public long contarPorUsuario(UUID usuarioId) {
        return calendarioRepository.findByUsuarioId(usuarioId).size();
    }
}