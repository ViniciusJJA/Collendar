package projeto.collendar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projeto.collendar.enums.TipoPermissao;
import projeto.collendar.model.Calendario;
import projeto.collendar.model.Compartilhamento;
import projeto.collendar.model.Usuario;
import projeto.collendar.repository.CalendarioRepository;
import projeto.collendar.repository.CompartilhamentoRepository;
import projeto.collendar.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompartilhamentoService {

    private final CompartilhamentoRepository compartilhamentoRepository;
    private final CalendarioRepository calendarioRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Compartilhamento compartilhar(UUID calendarioId, UUID usuarioId, TipoPermissao permissao) {
        Calendario calendario = calendarioRepository.findById(calendarioId)
                .orElseThrow(() -> new IllegalArgumentException("Calendário não encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (compartilhamentoRepository.existsByCalendarioAndUsuario(calendario, usuario)) {
            throw new IllegalArgumentException("Calendário já compartilhado com este usuário");
        }

        if (calendario.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("Não é possível compartilhar o calendário consigo mesmo");
        }

        Compartilhamento compartilhamento = new Compartilhamento();
        compartilhamento.setCalendario(calendario);
        compartilhamento.setUsuario(usuario);
        compartilhamento.setPermissao(permissao);

        return compartilhamentoRepository.save(compartilhamento);
    }

    public Optional<Compartilhamento> buscarPorId(UUID id) {
        return compartilhamentoRepository.findById(id);
    }

    public List<Compartilhamento> listarPorCalendario(UUID calendarioId) {
        return compartilhamentoRepository.findByCalendarioId(calendarioId);
    }

    public List<Calendario> listarCalendariosCompartilhados(UUID usuarioId) {
        return compartilhamentoRepository.findCalendariosCompartilhadosComUsuario(usuarioId);
    }

    public List<Compartilhamento> listarCompartilhamentosRecebidos(UUID usuarioId) {
        return compartilhamentoRepository.findByUsuarioId(usuarioId);
    }

    public Optional<Compartilhamento> buscarCompartilhamento(UUID calendarioId, UUID usuarioId) {
        Calendario calendario = calendarioRepository.findById(calendarioId)
                .orElseThrow(() -> new IllegalArgumentException("Calendário não encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return compartilhamentoRepository.findByCalendarioAndUsuario(calendario, usuario);
    }

    @Transactional
    public Compartilhamento atualizarPermissao(UUID id, TipoPermissao novaPermissao) {
        Compartilhamento compartilhamento = compartilhamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Compartilhamento não encontrado"));

        compartilhamento.setPermissao(novaPermissao);
        return compartilhamentoRepository.save(compartilhamento);
    }

    @Transactional
    public void removerCompartilhamento(UUID calendarioId, UUID usuarioId) {
        compartilhamentoRepository.deleteByCalendarioIdAndUsuarioId(calendarioId, usuarioId);
    }

    @Transactional
    public void deletar(UUID id) {
        if (!compartilhamentoRepository.existsById(id)) {
            throw new IllegalArgumentException("Compartilhamento não encontrado");
        }
        compartilhamentoRepository.deleteById(id);
    }

    public boolean temAcesso(UUID calendarioId, UUID usuarioId) {
        Calendario calendario = calendarioRepository.findById(calendarioId)
                .orElseThrow(() -> new IllegalArgumentException("Calendário não encontrado"));

        if (calendario.getUsuario().getId().equals(usuarioId)) {
            return true;
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return compartilhamentoRepository.existsByCalendarioAndUsuario(calendario, usuario);
    }

    public boolean podeEditar(UUID calendarioId, UUID usuarioId) {
        Calendario calendario = calendarioRepository.findById(calendarioId)
                .orElseThrow(() -> new IllegalArgumentException("Calendário não encontrado"));

        if (calendario.getUsuario().getId().equals(usuarioId)) {
            return true;
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Optional<Compartilhamento> compartilhamento =
                compartilhamentoRepository.findByCalendarioAndUsuario(calendario, usuario);

        return compartilhamento.isPresent() &&
                compartilhamento.get().getPermissao() == TipoPermissao.EDITAR;
    }

    public long contarPorCalendario(UUID calendarioId) {
        return compartilhamentoRepository.findByCalendarioId(calendarioId).size();
    }
}