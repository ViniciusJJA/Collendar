package projeto.collendar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import projeto.collendar.model.Role;
import projeto.collendar.model.Usuario;
import projeto.collendar.repository.RoleRepository;
import projeto.collendar.repository.UsuarioRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private Role roleUser;
    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();

        roleUser = new Role();
        roleUser.setId(UUID.randomUUID());
        roleUser.setNome("ROLE_USER");

        usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setNome("João Silva");
        usuario.setEmail("joao@email.com");
        usuario.setSenha("senha123");
        usuario.setAtivo(true);
        usuario.setRoles(new HashSet<>());
    }

    @Test
    void deveCriarUsuarioComSucesso() {
        when(usuarioRepository.existsByEmail(usuario.getEmail())).thenReturn(false);
        when(roleRepository.findByNome("ROLE_USER")).thenReturn(Optional.of(roleUser));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario resultado = usuarioService.criar(usuario);

        assertNotNull(resultado);
        assertEquals("João Silva", resultado.getNome());
        verify(usuarioRepository).existsByEmail(usuario.getEmail());
        verify(roleRepository).findByNome("ROLE_USER");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void deveLancarExcecaoAoCriarUsuarioComEmailDuplicado() {
        when(usuarioRepository.existsByEmail(usuario.getEmail())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.criar(usuario)
        );
        assertEquals("Email já cadastrado", exception.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoRoleNaoEncontrada() {
        when(usuarioRepository.existsByEmail(usuario.getEmail())).thenReturn(false);
        when(roleRepository.findByNome("ROLE_USER")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> usuarioService.criar(usuario)
        );
        assertEquals("Role ROLE_USER não encontrada", exception.getMessage());
    }

    @Test
    void deveBuscarUsuarioPorIdComSucesso() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.buscarPorId(usuarioId);

        assertTrue(resultado.isPresent());
        assertEquals("João Silva", resultado.get().getNome());
        verify(usuarioRepository).findById(usuarioId);
    }

    @Test
    void deveRetornarVazioQuandoUsuarioNaoEncontrado() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.buscarPorId(usuarioId);

        assertFalse(resultado.isPresent());
    }

    @Test
    void deveBuscarUsuarioPorEmail() {
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.buscarPorEmail(usuario.getEmail());

        assertTrue(resultado.isPresent());
        assertEquals("joao@email.com", resultado.get().getEmail());
    }

    @Test
    void deveListarTodosUsuarios() {
        List<Usuario> usuarios = Arrays.asList(usuario, new Usuario());
        when(usuarioRepository.findAll()).thenReturn(usuarios);

        List<Usuario> resultado = usuarioService.listarTodos();

        assertEquals(2, resultado.size());
        verify(usuarioRepository).findAll();
    }

    @Test
    void deveListarUsuariosAtivos() {
        List<Usuario> usuarios = Arrays.asList(usuario);
        when(usuarioRepository.findByAtivo(true)).thenReturn(usuarios);

        List<Usuario> resultado = usuarioService.listarAtivos();

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getAtivo());
    }

    @Test
    void deveAtualizarUsuarioComSucesso() {
        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setNome("João Updated");
        usuarioAtualizado.setEmail("joao@email.com");
        usuarioAtualizado.setSenha("novaSenha");

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario resultado = usuarioService.atualizar(usuarioId, usuarioAtualizado);

        assertNotNull(resultado);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarUsuarioInexistente() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.atualizar(usuarioId, usuario)
        );
        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoAoAtualizarComEmailDuplicado() {
        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setEmail("outro@email.com");

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmail("outro@email.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.atualizar(usuarioId, usuarioAtualizado)
        );
        assertEquals("Email já cadastrado", exception.getMessage());
    }

    @Test
    void deveDesativarUsuario() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        usuarioService.desativar(usuarioId);

        verify(usuarioRepository).findById(usuarioId);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void deveDeletarUsuario() {
        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);
        doNothing().when(usuarioRepository).deleteById(usuarioId);

        usuarioService.deletar(usuarioId);

        verify(usuarioRepository).existsById(usuarioId);
        verify(usuarioRepository).deleteById(usuarioId);
    }

    @Test
    void deveLancarExcecaoAoDeletarUsuarioInexistente() {
        when(usuarioRepository.existsById(usuarioId)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.deletar(usuarioId)
        );
        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    @Test
    void deveAdicionarRoleAoUsuario() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(roleRepository.findByNome("ROLE_ADMIN")).thenReturn(Optional.of(roleUser));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario resultado = usuarioService.adicionarRole(usuarioId, "ROLE_ADMIN");

        assertNotNull(resultado);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void deveRemoverRoleDoUsuario() {
        usuario.getRoles().add(roleUser);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(roleRepository.findByNome("ROLE_USER")).thenReturn(Optional.of(roleUser));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario resultado = usuarioService.removerRole(usuarioId, "ROLE_USER");

        assertNotNull(resultado);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void deveVerificarSeExistePorEmail() {
        when(usuarioRepository.existsByEmail("joao@email.com")).thenReturn(true);

        boolean resultado = usuarioService.existePorEmail("joao@email.com");

        assertTrue(resultado);
    }

    @Test
    void deveAtualizarUsuarioSemAlterarSenhaQuandoNaoFornecida() {
        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setNome("João Updated");
        usuarioAtualizado.setEmail("joao@email.com");
        usuarioAtualizado.setSenha("");

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario resultado = usuarioService.atualizar(usuarioId, usuarioAtualizado);

        assertNotNull(resultado);
        assertEquals("senha123", usuario.getSenha());
    }
}
