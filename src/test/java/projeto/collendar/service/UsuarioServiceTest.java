package projeto.collendar.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import projeto.collendar.dtos.request.UsuarioRequestDTO;
import projeto.collendar.dtos.response.UsuarioResponseDTO;
import projeto.collendar.exception.BusinessException;
import projeto.collendar.exception.ResourceNotFoundException;
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

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Nested
    class Dado_um_usuario_valido_para_criar {

        UsuarioRequestDTO dto;
        Role roleUser;
        Role roleAdmin;

        @BeforeEach
        void setup() {
            dto = new UsuarioRequestDTO(
                    "João Silva",
                    "joao@email.com",
                    "senha123"
            );

            roleUser = new Role();
            roleUser.setId(UUID.randomUUID());
            roleUser.setNome("USER");

            roleAdmin = new Role();
            roleAdmin.setId(UUID.randomUUID());
            roleAdmin.setNome("ADMIN");
        }

        @Nested
        class Quando_criar_usuario {

            UsuarioResponseDTO resultado;

            @BeforeEach
            void setup() {
                when(usuarioRepository.existsByEmail(dto.email())).thenReturn(false);
                when(passwordEncoder.encode(dto.senha())).thenReturn("senhaEncriptada");
                when(roleRepository.findByNome("USER")).thenReturn(Optional.of(roleUser));
                when(roleRepository.findByNome("ADMIN")).thenReturn(Optional.of(roleAdmin));
                when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
                    Usuario usuario = invocation.getArgument(0);
                    usuario.setId(UUID.randomUUID());
                    return usuario;
                });

                resultado = usuarioService.create(dto);
            }

            @Test
            void deve_retornar_usuario_criado_com_sucesso() {
                assertNotNull(resultado);
                assertEquals("João Silva", resultado.nome());
                assertEquals("joao@email.com", resultado.email());
                assertTrue(resultado.ativo());
            }

            @Test
            void deve_adicionar_roles_user_e_admin() {
                assertNotNull(resultado.roles());
                assertEquals(2, resultado.roles().size());
                assertTrue(resultado.roles().contains("USER"));
                assertTrue(resultado.roles().contains("ADMIN"));
            }

            @Test
            void deve_encriptar_senha() {
                verify(passwordEncoder).encode("senha123");
            }

            @Test
            void deve_salvar_usuario_no_repositorio() {
                verify(usuarioRepository).save(any(Usuario.class));
            }
        }

        @Nested
        class Quando_email_ja_existe {

            @BeforeEach
            void setup() {
                when(usuarioRepository.existsByEmail(dto.email())).thenReturn(true);
            }

            @Test
            void deve_lancar_business_exception() {
                BusinessException exception = assertThrows(
                        BusinessException.class,
                        () -> usuarioService.create(dto)
                );

                assertEquals("Email já cadastrado", exception.getMessage());
            }

            @Test
            void nao_deve_salvar_usuario() {
                assertThrows(BusinessException.class, () -> usuarioService.create(dto));
                verify(usuarioRepository, never()).save(any());
            }
        }

        @Nested
        class Quando_role_user_nao_existe {

            @BeforeEach
            void setup() {
                when(usuarioRepository.existsByEmail(dto.email())).thenReturn(false);
                when(roleRepository.findByNome("USER")).thenReturn(Optional.empty());
            }

            @Test
            void deve_lancar_resource_not_found_exception() {
                ResourceNotFoundException exception = assertThrows(
                        ResourceNotFoundException.class,
                        () -> usuarioService.create(dto)
                );

                assertEquals("Role não encontrado: USER", exception.getMessage());
            }
        }

        @Nested
        class Quando_role_admin_nao_existe {

            @BeforeEach
            void setup() {
                when(usuarioRepository.existsByEmail(dto.email())).thenReturn(false);
                when(roleRepository.findByNome("USER")).thenReturn(Optional.of(roleUser));
                when(roleRepository.findByNome("ADMIN")).thenReturn(Optional.empty());
            }

            @Test
            void deve_lancar_resource_not_found_exception() {
                ResourceNotFoundException exception = assertThrows(
                        ResourceNotFoundException.class,
                        () -> usuarioService.create(dto)
                );

                assertEquals("Role não encontrado: ADMIN", exception.getMessage());
            }
        }
    }

    @Nested
    class Dado_um_usuario_existente {

        Usuario usuario;
        UUID usuarioId;

        @BeforeEach
        void setup() {
            usuarioId = UUID.randomUUID();

            usuario = new Usuario();
            usuario.setId(usuarioId);
            usuario.setNome("João Silva");
            usuario.setEmail("joao@email.com");
            usuario.setSenha("senhaEncriptada");
            usuario.setAtivo(true);
            usuario.setRoles(new HashSet<>());
        }

        @Nested
        class Quando_buscar_por_id {

            UsuarioResponseDTO resultado;

            @BeforeEach
            void setup() {
                when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
                resultado = usuarioService.findById(usuarioId);
            }

            @Test
            void deve_retornar_usuario_encontrado() {
                assertNotNull(resultado);
                assertEquals("João Silva", resultado.nome());
                assertEquals("joao@email.com", resultado.email());
            }
        }

        @Nested
        class Quando_buscar_por_id_inexistente {

            @BeforeEach
            void setup() {
                when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());
            }

            @Test
            void deve_lancar_resource_not_found_exception() {
                ResourceNotFoundException exception = assertThrows(
                        ResourceNotFoundException.class,
                        () -> usuarioService.findById(usuarioId)
                );

                assertTrue(exception.getMessage().contains("Usuário não encontrado"));
            }
        }

        @Nested
        class Quando_buscar_por_email {

            UsuarioResponseDTO resultado;

            @BeforeEach
            void setup() {
                when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
                resultado = usuarioService.findByEmail(usuario.getEmail());
            }

            @Test
            void deve_retornar_usuario_encontrado() {
                assertNotNull(resultado);
                assertEquals("joao@email.com", resultado.email());
            }
        }

        @Nested
        class Quando_listar_todos_usuarios {

            List<UsuarioResponseDTO> resultado;

            @BeforeEach
            void setup() {
                Usuario usuario2 = new Usuario();
                usuario2.setId(UUID.randomUUID());
                usuario2.setNome("Maria Santos");
                usuario2.setEmail("maria@email.com");
                usuario2.setRoles(new HashSet<>());

                when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuario, usuario2));
                resultado = usuarioService.listAll();
            }

            @Test
            void deve_retornar_lista_com_todos_usuarios() {
                assertNotNull(resultado);
                assertEquals(2, resultado.size());
            }
        }

        @Nested
        class Quando_listar_usuarios_ativos {

            List<UsuarioResponseDTO> resultado;

            @BeforeEach
            void setup() {
                when(usuarioRepository.findByAtivo(true)).thenReturn(Arrays.asList(usuario));
                resultado = usuarioService.listAtivos();
            }

            @Test
            void deve_retornar_apenas_usuarios_ativos() {
                assertNotNull(resultado);
                assertEquals(1, resultado.size());
                assertTrue(resultado.get(0).ativo());
            }
        }

        @Nested
        class Quando_atualizar_usuario {

            UsuarioRequestDTO dtoAtualizado;
            UsuarioResponseDTO resultado;

            @BeforeEach
            void setup() {
                dtoAtualizado = new UsuarioRequestDTO(
                        "João Silva Atualizado",
                        "joao@email.com",
                        "novaSenha123"
                );

                when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
                when(usuarioRepository.existsByEmail(dtoAtualizado.email())).thenReturn(false);
                when(passwordEncoder.encode(dtoAtualizado.senha())).thenReturn("novaSenhaEncriptada");
                when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

                resultado = usuarioService.update(usuarioId, dtoAtualizado);
            }

            @Test
            void deve_atualizar_usuario_com_sucesso() {
                assertNotNull(resultado);
                verify(usuarioRepository).save(any(Usuario.class));
            }

            @Test
            void deve_encriptar_nova_senha() {
                verify(passwordEncoder).encode("novaSenha123");
            }
        }

        @Nested
        class Quando_atualizar_com_email_duplicado {

            UsuarioRequestDTO dtoAtualizado;

            @BeforeEach
            void setup() {
                dtoAtualizado = new UsuarioRequestDTO(
                        "João Silva",
                        "outro@email.com",
                        "senha123"
                );

                when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
                when(usuarioRepository.existsByEmail("outro@email.com")).thenReturn(true);
            }

            @Test
            void deve_lancar_business_exception() {
                BusinessException exception = assertThrows(
                        BusinessException.class,
                        () -> usuarioService.update(usuarioId, dtoAtualizado)
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

                usuarioService.deactivate(usuarioId);
            }

            @Test
            void deve_desativar_usuario() {
                assertFalse(usuario.getAtivo());
            }

            @Test
            void deve_salvar_usuario_desativado() {
                verify(usuarioRepository).save(usuario);
            }
        }

        @Nested
        class Quando_deletar_usuario {

            @BeforeEach
            void setup() {
                when(usuarioRepository.existsById(usuarioId)).thenReturn(true);
                doNothing().when(usuarioRepository).deleteById(usuarioId);

                usuarioService.delete(usuarioId);
            }

            @Test
            void deve_deletar_usuario() {
                verify(usuarioRepository).deleteById(usuarioId);
            }
        }

        @Nested
        class Quando_deletar_usuario_inexistente {

            @BeforeEach
            void setup() {
                when(usuarioRepository.existsById(usuarioId)).thenReturn(false);
            }

            @Test
            void deve_lancar_resource_not_found_exception() {
                ResourceNotFoundException exception = assertThrows(
                        ResourceNotFoundException.class,
                        () -> usuarioService.delete(usuarioId)
                );

                assertTrue(exception.getMessage().contains("Usuário não encontrado"));
            }
        }

        @Nested
        class Quando_adicionar_role {

            Role roleAdmin;
            UsuarioResponseDTO resultado;

            @BeforeEach
            void setup() {
                roleAdmin = new Role();
                roleAdmin.setId(UUID.randomUUID());
                roleAdmin.setNome("ADMIN");

                when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
                when(roleRepository.findByNome("ADMIN")).thenReturn(Optional.of(roleAdmin));
                when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

                resultado = usuarioService.addRole(usuarioId, "ADMIN");
            }

            @Test
            void deve_adicionar_role_ao_usuario() {
                assertNotNull(resultado);
                verify(usuarioRepository).save(usuario);
            }
        }

        @Nested
        class Quando_adicionar_role_inexistente {

            @BeforeEach
            void setup() {
                when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
                when(roleRepository.findByNome("ROLE_INVALIDA")).thenReturn(Optional.empty());
            }

            @Test
            void deve_lancar_resource_not_found_exception() {
                ResourceNotFoundException exception = assertThrows(
                        ResourceNotFoundException.class,
                        () -> usuarioService.addRole(usuarioId, "ROLE_INVALIDA")
                );

                assertTrue(exception.getMessage().contains("Role não encontrado"));
            }
        }
    }
}