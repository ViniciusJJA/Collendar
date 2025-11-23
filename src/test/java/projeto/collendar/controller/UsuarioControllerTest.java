package projeto.collendar.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import projeto.collendar.model.Role;
import projeto.collendar.model.Usuario;
import projeto.collendar.service.UsuarioService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    @Nested
    class Dado_um_usuario_valido {

        Usuario usuario;
        UUID usuarioId;

        @BeforeEach
        void setup() {
            usuarioId = UUID.randomUUID();
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

            ResponseEntity<UsuarioDTO> resposta;

            @BeforeEach
            void setup() {
                when(usuarioService.criar(any(Usuario.class))).thenReturn(usuario);
                resposta = usuarioController.criar(usuario);
            }

            @Test
            void deve_retornar_status_created() {
                assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_usuario_criado() {
                assertNotNull(resposta.getBody());
                assertEquals("João Silva", resposta.getBody().getNome());
                assertEquals("joao@email.com", resposta.getBody().getEmail());
            }

            @Test
            void deve_chamar_service_criar() {
                verify(usuarioService, times(1)).criar(any(Usuario.class));
            }
        }

        @Nested
        class Quando_buscar_por_id {

            ResponseEntity<UsuarioDTO> resposta;

            @BeforeEach
            void setup() {
                when(usuarioService.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
                resposta = usuarioController.buscarPorId(usuarioId);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_usuario_encontrado() {
                assertNotNull(resposta.getBody());
                assertEquals("João Silva", resposta.getBody().getNome());
            }
        }

        @Nested
        class Quando_buscar_por_email {

            ResponseEntity<UsuarioDTO> resposta;

            @BeforeEach
            void setup() {
                when(usuarioService.buscarPorEmail("joao@email.com")).thenReturn(Optional.of(usuario));
                resposta = usuarioController.buscarPorEmail("joao@email.com");
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_usuario_com_email_correto() {
                assertNotNull(resposta.getBody());
                assertEquals("joao@email.com", resposta.getBody().getEmail());
            }
        }

        @Nested
        class Quando_atualizar_usuario {

            ResponseEntity<UsuarioDTO> resposta;
            Usuario usuarioAtualizado;

            @BeforeEach
            void setup() {
                usuarioAtualizado = new Usuario();
                usuarioAtualizado.setNome("João Silva Atualizado");
                usuarioAtualizado.setEmail("joao@email.com");

                when(usuarioService.atualizar(eq(usuarioId), any(Usuario.class))).thenReturn(usuario);
                resposta = usuarioController.atualizar(usuarioId, usuarioAtualizado);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_chamar_service_atualizar() {
                verify(usuarioService, times(1)).atualizar(eq(usuarioId), any(Usuario.class));
            }
        }

        @Nested
        class Quando_desativar_usuario {

            ResponseEntity<Void> resposta;

            @BeforeEach
            void setup() {
                doNothing().when(usuarioService).desativar(usuarioId);
                resposta = usuarioController.desativar(usuarioId);
            }

            @Test
            void deve_retornar_status_no_content() {
                assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
            }

            @Test
            void deve_chamar_service_desativar() {
                verify(usuarioService, times(1)).desativar(usuarioId);
            }
        }

        @Nested
        class Quando_deletar_usuario {

            ResponseEntity<Void> resposta;

            @BeforeEach
            void setup() {
                doNothing().when(usuarioService).deletar(usuarioId);
                resposta = usuarioController.deletar(usuarioId);
            }

            @Test
            void deve_retornar_status_no_content() {
                assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
            }

            @Test
            void deve_chamar_service_deletar() {
                verify(usuarioService, times(1)).deletar(usuarioId);
            }
        }

        @Nested
        class Quando_adicionar_role {

            ResponseEntity<UsuarioDTO> resposta;

            @BeforeEach
            void setup() {
                Role role = new Role();
                role.setNome("ROLE_ADMIN");
                usuario.getRoles().add(role);

                when(usuarioService.adicionarRole(usuarioId, "ROLE_ADMIN")).thenReturn(usuario);
                resposta = usuarioController.adicionarRole(usuarioId, "ROLE_ADMIN");
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_chamar_service_adicionar_role() {
                verify(usuarioService, times(1)).adicionarRole(usuarioId, "ROLE_ADMIN");
            }
        }
    }

    @Nested
    class Dado_um_usuario_inexistente {

        UUID usuarioId;

        @BeforeEach
        void setup() {
            usuarioId = UUID.randomUUID();
        }

        @Nested
        class Quando_buscar_por_id {

            ResponseEntity<UsuarioDTO> resposta;

            @BeforeEach
            void setup() {
                when(usuarioService.buscarPorId(usuarioId)).thenReturn(Optional.empty());
                resposta = usuarioController.buscarPorId(usuarioId);
            }

            @Test
            void deve_retornar_status_not_found() {
                assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
            }

            @Test
            void nao_deve_retornar_corpo() {
                assertNull(resposta.getBody());
            }
        }

        @Nested
        class Quando_deletar_usuario {

            ResponseEntity<Void> resposta;

            @BeforeEach
            void setup() {
                doThrow(new IllegalArgumentException("Usuário não encontrado"))
                        .when(usuarioService).deletar(usuarioId);
                resposta = usuarioController.deletar(usuarioId);
            }

            @Test
            void deve_retornar_status_not_found() {
                assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
            }
        }
    }

    @Nested
    class Dado_um_email_duplicado {

        Usuario usuario;

        @BeforeEach
        void setup() {
            usuario = new Usuario();
            usuario.setNome("Maria Santos");
            usuario.setEmail("maria@email.com");
            usuario.setSenha("senha456");
        }

        @Nested
        class Quando_criar_usuario {

            ResponseEntity<UsuarioDTO> resposta;

            @BeforeEach
            void setup() {
                when(usuarioService.criar(any(Usuario.class)))
                        .thenThrow(new IllegalArgumentException("Email já cadastrado"));
                resposta = usuarioController.criar(usuario);
            }

            @Test
            void deve_retornar_status_bad_request() {
                assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
            }
        }
    }

    @Nested
    class Quando_listar_todos_usuarios {

        List<Usuario> usuarios;
        ResponseEntity<List<UsuarioDTO>> resposta;

        @BeforeEach
        void setup() {
            Usuario usuario1 = new Usuario();
            usuario1.setId(UUID.randomUUID());
            usuario1.setNome("João");
            usuario1.setEmail("joao@email.com");
            usuario1.setRoles(new HashSet<>());

            Usuario usuario2 = new Usuario();
            usuario2.setId(UUID.randomUUID());
            usuario2.setNome("Maria");
            usuario2.setEmail("maria@email.com");
            usuario2.setRoles(new HashSet<>());

            usuarios = Arrays.asList(usuario1, usuario2);

            when(usuarioService.listarTodos()).thenReturn(usuarios);
            resposta = usuarioController.listarTodos();
        }

        @Test
        void deve_retornar_status_ok() {
            assertEquals(HttpStatus.OK, resposta.getStatusCode());
        }

        @Test
        void deve_retornar_lista_com_dois_usuarios() {
            assertNotNull(resposta.getBody());
            assertEquals(2, resposta.getBody().size());
        }
    }

    @Nested
    class Quando_listar_usuarios_ativos {

        List<Usuario> usuarios;
        ResponseEntity<List<UsuarioDTO>> resposta;

        @BeforeEach
        void setup() {
            Usuario usuario1 = new Usuario();
            usuario1.setId(UUID.randomUUID());
            usuario1.setNome("João");
            usuario1.setAtivo(true);
            usuario1.setRoles(new HashSet<>());

            usuarios = Arrays.asList(usuario1);

            when(usuarioService.listarAtivos()).thenReturn(usuarios);
            resposta = usuarioController.listarAtivos();
        }

        @Test
        void deve_retornar_status_ok() {
            assertEquals(HttpStatus.OK, resposta.getStatusCode());
        }

        @Test
        void deve_retornar_apenas_usuarios_ativos() {
            assertNotNull(resposta.getBody());
            assertEquals(1, resposta.getBody().size());
            assertTrue(resposta.getBody().get(0).getAtivo());
        }
    }
}