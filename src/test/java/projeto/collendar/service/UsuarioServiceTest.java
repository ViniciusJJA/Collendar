package projeto.collendar.service;

import org.junit.jupiter.api.*;
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

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Nested
    class Dado_um_usuario_valido {

        Usuario usuario;
        Role roleUser;
        UUID usuarioId;

        @BeforeEach
        void setup() {
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

        @Nested
        class Quando_criar_usuario {

            @BeforeEach
            void setup() {
                when(usuarioRepository.existsByEmail(usuario.getEmail())).thenReturn(false);
                when(roleRepository.findByNome("ROLE_USER")).thenReturn(Optional.of(roleUser));
                when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
            }

            @Test
            void deve_criar_usuario_com_sucesso() {
                Usuario resultado = usuarioService.criar(usuario);

                assertNotNull(resultado);
                assertEquals("João Silva", resultado.getNome());
                assertEquals("joao@email.com", resultado.getEmail());
                verify(usuarioRepository).save(any(Usuario.class));
            }

            @Test
            void deve_adicionar_role_user_automaticamente() {
                Usuario resultado = usuarioService.criar(usuario);

                verify(roleRepository).findByNome("ROLE_USER");
                verify(usuarioRepository).save(any(Usuario.class));
            }
        }

        @Nested
        class Quando_criar_usuario_com_email_duplicado {

            @BeforeEach
            void setup() {
                when(usuarioRepository.existsByEmail(usuario.getEmail())).thenReturn(true);
            }

            @Test
            void deve_lancar_excecao() {
                IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> usuarioService.criar(usuario)
                );

                assertEquals("Email já cadastrado", exception.getMessage());
                verify(usuarioRepository, never()).save(any());
            }
        }

        @Nested
        class Quando_role_nao_encontrada {

            @BeforeEach
            void setup() {
                when(usuarioRepository.existsByEmail(usuario.getEmail())).thenReturn(false);
                when(roleRepository.findByNome("ROLE_USER")).thenReturn(Optional.empty());
            }

            @Test
            void deve_lancar_excecao() {
                RuntimeException exception = assertThrows(
                        RuntimeException.class,
                        () -> usuarioService.criar(usuario)
                );

                assertEquals("Role ROLE_USER não encontrada", exception.getMessage());
            }
        }

        @Nested
        class Quando_buscar_usuario_por_id {

            @Test
            void deve_retornar_usuario_quando_existir() {
                when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

                Optional<Usuario> resultado = usuarioService.buscarPorId(usuarioId);

                assertTrue(resultado.isPresent());
                assertEquals("João Silva", resultado.get().getNome());
            }

            @Test
            void deve_retornar_vazio_quando_nao_existir() {
                when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

                Optional<Usuario> resultado = usuarioService.buscarPorId(usuarioId);

                assertFalse(resultado.isPresent());
            }
        }

        @Nested
        class Quando_buscar_usuario_por_email {

            @Test
            void deve_retornar_usuario_quando_existir() {
                when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

                Optional<Usuario> resultado = usuarioService.buscarPorEmail(usuario.getEmail());

                assertTrue(resultado.isPresent());
                assertEquals("joao@email.com", resultado.get().getEmail());
            }
        }

        @Nested
        class Quando_listar_usuarios {

            @Test
            void deve_listar_todos_usuarios() {
                List<Usuario> usuarios = Arrays.asList(usuario, new Usuario());
                when(usuarioRepository.findAll()).thenReturn(usuarios);

                List<Usuario> resultado = usuarioService.listarTodos();

                assertEquals(2, resultado.size());
            }

            @Test
            void deve_listar_apenas_usuarios_ativos() {
                List<Usuario> usuarios = Arrays.asList(usuario);
                when(usuarioRepository.findByAtivo(true)).thenReturn(usuarios);

                List<Usuario> resultado = usuarioService.listarAtivos();

                assertEquals(1, resultado.size());
                assertTrue(resultado.get(0).getAtivo());
            }
        }

        @Nested
        class Quando_atualizar_usuario {

            Usuario usuarioAtualizado;

            @BeforeEach
            void setup() {
                usuarioAtualizado = new Usuario();
                usuarioAtualizado.setNome("João Updated");
                usuarioAtualizado.setEmail("joao@email.com");
                usuarioAtualizado.setSenha("novaSenha");

                when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
                when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
            }

            @Test
            void deve_atualizar_usuario_com_sucesso() {
                Usuario resultado = usuarioService.atualizar(usuarioId, usuarioAtualizado);

                assertNotNull(resultado);
                verify(usuarioRepository).save(any(Usuario.class));
            }

            @Test
            void nao_deve_alterar_senha_quando_vazia() {
                usuarioAtualizado.setSenha("");

                Usuario resultado = usuarioService.atualizar(usuarioId, usuarioAtualizado);

                assertEquals("senha123", usuario.getSenha());
            }
        }

        @Nested
        class Quando_atualizar_usuario_inexistente {

            @BeforeEach
            void setup() {
                when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());
            }

            @Test
            void deve_lancar_excecao() {
                IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> usuarioService.atualizar(usuarioId, usuario)
                );

                assertEquals("Usuário não encontrado", exception.getMessage());
            }
        }

        @Nested
        class Quando_atualizar_com_email_duplicado {

            @BeforeEach
            void setup() {
                Usuario usuarioAtualizado = new Usuario();
                usuarioAtualizado.setEmail("outro@email.com");

                when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
                when(usuarioRepository.existsByEmail("outro@email.com")).thenReturn(true);
            }

            @Test
            void deve_lancar_excecao() {
                Usuario usuarioAtualizado = new Usuario();
                usuarioAtualizado.setEmail("outro@email.com");

                IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> usuarioService.atualizar(usuarioId, usuarioAtualizado)
                );

                assertEquals("Email já cadastrado", exception.getMessage());
            }
        }

        @Nested
        class Quando_desativar_usuario {

            @BeforeEach
            void setup() {
                when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
                when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

                usuarioService.desativar(usuarioId);
            }

            @Test
            void deve_desativar_usuario() {
                verify(usuarioRepository).findById(usuarioId);
                verify(usuarioRepository).save(any(Usuario.class));
            }
        }

        @Nested
        class Quando_deletar_usuario {

            @Test
            void deve_deletar_usuario_existente() {
                when(usuarioRepository.existsById(usuarioId)).thenReturn(true);
                doNothing().when(usuarioRepository).deleteById(usuarioId);

                usuarioService.deletar(usuarioId);

                verify(usuarioRepository).deleteById(usuarioId);
            }

            @Test
            void deve_lancar_excecao_quando_usuario_nao_existe() {
                when(usuarioRepository.existsById(usuarioId)).thenReturn(false);

                IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> usuarioService.deletar(usuarioId)
                );

                assertEquals("Usuário não encontrado", exception.getMessage());
            }
        }

        @Nested
        class Quando_gerenciar_roles {

            @Test
            void deve_adicionar_role_ao_usuario() {
                when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
                when(roleRepository.findByNome("ROLE_ADMIN")).thenReturn(Optional.of(roleUser));
                when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

                Usuario resultado = usuarioService.adicionarRole(usuarioId, "ROLE_ADMIN");

                assertNotNull(resultado);
                verify(usuarioRepository).save(any(Usuario.class));
            }

            @Test
            void deve_remover_role_do_usuario() {
                usuario.getRoles().add(roleUser);
                when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
                when(roleRepository.findByNome("ROLE_USER")).thenReturn(Optional.of(roleUser));
                when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

                Usuario resultado = usuarioService.removerRole(usuarioId, "ROLE_USER");

                assertNotNull(resultado);
                verify(usuarioRepository).save(any(Usuario.class));
            }
        }

        @Nested
        class Quando_verificar_existencia_por_email {

            @Test
            void deve_retornar_true_quando_email_existe() {
                when(usuarioRepository.existsByEmail("joao@email.com")).thenReturn(true);

                boolean resultado = usuarioService.existePorEmail("joao@email.com");

                assertTrue(resultado);
            }

            @Test
            void deve_retornar_false_quando_email_nao_existe() {
                when(usuarioRepository.existsByEmail("inexistente@email.com")).thenReturn(false);

                boolean resultado = usuarioService.existePorEmail("inexistente@email.com");

                assertFalse(resultado);
            }
        }
    }
}