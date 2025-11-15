package projeto.collendar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projeto.collendar.model.Role;
import projeto.collendar.model.Usuario;
import projeto.collendar.repository.RoleRepository;
import projeto.collendar.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public Usuario criar(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
        Role roleUser = roleRepository.findByNome("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role ROLE_USER não encontrada"));
        usuario.getRoles().add(roleUser);

        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorId(UUID id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> listarAtivos() {
        return usuarioRepository.findByAtivo(true);
    }

    @Transactional
    public Usuario atualizar(UUID id, Usuario usuarioAtualizado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (!usuario.getEmail().equals(usuarioAtualizado.getEmail()) &&
                usuarioRepository.existsByEmail(usuarioAtualizado.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        usuario.setNome(usuarioAtualizado.getNome());
        usuario.setEmail(usuarioAtualizado.getEmail());

        if (usuarioAtualizado.getSenha() != null && !usuarioAtualizado.getSenha().isEmpty()) {
            usuario.setSenha(usuarioAtualizado.getSenha());
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void desativar(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void deletar(UUID id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        usuarioRepository.deleteById(id);
    }

    @Transactional
    public Usuario adicionarRole(UUID usuarioId, String nomeRole) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Role role = roleRepository.findByNome(nomeRole)
                .orElseThrow(() -> new IllegalArgumentException("Role não encontrada"));

        usuario.getRoles().add(role);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario removerRole(UUID usuarioId, String nomeRole) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Role role = roleRepository.findByNome(nomeRole)
                .orElseThrow(() -> new IllegalArgumentException("Role não encontrada"));

        usuario.getRoles().remove(role);
        return usuarioRepository.save(usuario);
    }

    public boolean existePorEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }
}
//comentario