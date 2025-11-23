package projeto.collendar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projeto.collendar.dtos.request.UsuarioRequestDTO;
import projeto.collendar.dtos.response.UsuarioResponseDTO;
import projeto.collendar.exception.BusinessException;
import projeto.collendar.exception.ResourceNotFoundException;
import projeto.collendar.mappers.UsuarioMapper;
import projeto.collendar.model.Role;
import projeto.collendar.model.Usuario;
import projeto.collendar.repository.RoleRepository;
import projeto.collendar.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponseDTO create(UsuarioRequestDTO dto) {
        log.info("🔵 Iniciando criação do usuário: {}", dto.email());

        if (usuarioRepository.existsByEmail(dto.email())) {
            log.error("❌ Email já cadastrado: {}", dto.email());
            throw new BusinessException("Email já cadastrado");
        }

        Usuario usuario = UsuarioMapper.toEntity(dto);
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        log.info("✅ Senha criptografada");

        // Listar todas as roles para debug
        List<Role> todasRoles = roleRepository.findAll();
        log.info("📋 Roles disponíveis no banco: {}", todasRoles.stream().map(Role::getNome).toList());

        // Buscar role USER
        log.info("🔍 Buscando role USER...");
        Optional<Role> roleUserOpt = roleRepository.findByNome("USER");

        if (roleUserOpt.isEmpty()) {
            log.error("❌ Role USER não encontrada!");
            throw new ResourceNotFoundException("Role", "USER");
        }

        Role roleUser = roleUserOpt.get();
        log.info("✅ Role USER encontrada: ID={}", roleUser.getId());

        // Buscar role ADMIN
        log.info("🔍 Buscando role ADMIN...");
        Optional<Role> roleAdminOpt = roleRepository.findByNome("ADMIN");

        if (roleAdminOpt.isEmpty()) {
            log.error("❌ Role ADMIN não encontrada!");
            throw new ResourceNotFoundException("Role", "ADMIN");
        }

        Role roleAdmin = roleAdminOpt.get();
        log.info("✅ Role ADMIN encontrada: ID={}", roleAdmin.getId());

        usuario.getRoles().add(roleUser);
        usuario.getRoles().add(roleAdmin);
        log.info("✅ Roles adicionadas ao usuário");

        Usuario savedUsuario = usuarioRepository.save(usuario);
        log.info("✅ Usuário salvo com sucesso! ID={}", savedUsuario.getId());

        return UsuarioMapper.toDTO(savedUsuario);
    }

    public List<UsuarioResponseDTO> listAll() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioMapper::toDTO)
                .toList();
    }

    public UsuarioResponseDTO findById(UUID id) {
        return usuarioRepository.findById(id)
                .map(UsuarioMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id.toString()));
    }

    public UsuarioResponseDTO findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .map(UsuarioMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", email));
    }

    public List<UsuarioResponseDTO> listAtivos() {
        return usuarioRepository.findByAtivo(true).stream()
                .map(UsuarioMapper::toDTO)
                .toList();
    }

    @Transactional
    public UsuarioResponseDTO update(UUID id, UsuarioRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id.toString()));

        if (!usuario.getEmail().equals(dto.email()) && usuarioRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Email já cadastrado");
        }

        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());

        if (dto.senha() != null && !dto.senha().isEmpty()) {
            usuario.setSenha(passwordEncoder.encode(dto.senha()));
        }

        return UsuarioMapper.toDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public void deactivate(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id.toString()));
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void delete(UUID id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário", id.toString());
        }
        usuarioRepository.deleteById(id);
    }

    @Transactional
    public UsuarioResponseDTO addRole(UUID usuarioId, String nomeRole) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId.toString()));

        Role role = roleRepository.findByNome(nomeRole)
                .orElseThrow(() -> new ResourceNotFoundException("Role", nomeRole));

        usuario.getRoles().add(role);
        return UsuarioMapper.toDTO(usuarioRepository.save(usuario));
    }

    public Usuario findEntityById(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id.toString()));
    }

    public Usuario findEntityByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", email));
    }
}