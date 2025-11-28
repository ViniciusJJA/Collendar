package projeto.collendar.controller;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import projeto.collendar.dtos.request.UsuarioRequestDTO;
import projeto.collendar.dtos.response.UsuarioResponseDTO;
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
    class Dado_uma_requisicao_para_criar_usuario {

        UsuarioRequestDTO dto;

        @BeforeEach
        void setup() {
            dto = new UsuarioRequestDTO(
                    "João Silva",
                    "joao@email.com",
                    "senha123"
            );
        }

        @Nested
        class Quando_dados_validos {

            @Test
            void deve_criar_usuario_com_sucesso() {
                UsuarioResponseDTO usuarioResponse = new UsuarioResponseDTO(
                        UUID.randomUUID(),
                        "João Silva",
                        "joao@email.com",
                        true,
                        Set.of("USER", "ADMIN")
                );

                when(usuarioService.create(any(UsuarioRequestDTO.class))).thenReturn(usuarioResponse);

                ResponseEntity<UsuarioResponseDTO> resposta = usuarioController.create(dto);

                assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
                assertNotNull(resposta.getBody());
                assertEquals("João Silva", resposta.getBody().nome());
                assertEquals("joao@email.com", resposta.getBody().email());
                verify(usuarioService).create(any(UsuarioRequestDTO.class));
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_listar_usuarios {

        @Nested
        class Quando_listar_todos_usuarios {

            @Test
            void deve_retornar_lista_de_usuarios() {
                List<UsuarioResponseDTO> usuarios = Arrays.asList(
                        new UsuarioResponseDTO(
                                UUID.randomUUID(),
                                "João Silva",
                                "joao@email.com",
                                true,
                                Set.of("USER")
                        ),
                        new UsuarioResponseDTO(
                                UUID.randomUUID(),
                                "Maria Santos",
                                "maria@email.com",
                                true,
                                Set.of("USER")
                        )
                );

                when(usuarioService.listAll()).thenReturn(usuarios);

                ResponseEntity<List<UsuarioResponseDTO>> resposta = usuarioController.listAll();

                assertEquals(HttpStatus.OK, resposta.getStatusCode());
                assertNotNull(resposta.getBody());
                assertEquals(2, resposta.getBody().size());
            }
        }

        @Nested
        class Quando_listar_usuarios_ativos {

            @Test
            void deve_retornar_apenas_usuarios_ativos() {
                List<UsuarioResponseDTO> usuarios = Arrays.asList(
                        new UsuarioResponseDTO(
                                UUID.randomUUID(),
                                "João Silva",
                                "joao@email.com",
                                true,
                                Set.of("USER")
                        )
                );

                when(usuarioService.listAtivos()).thenReturn(usuarios);

                ResponseEntity<List<UsuarioResponseDTO>> resposta = usuarioController.listAtivos();

                assertEquals(HttpStatus.OK, resposta.getStatusCode());
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().size());
                assertTrue(resposta.getBody().get(0).ativo());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_buscar_usuario_por_id {

        UUID usuarioId;

        @BeforeEach
        void setup() {
            usuarioId = UUID.randomUUID();
        }

        @Nested
        class Quando_usuario_existe {

            @Test
            void deve_retornar_usuario_encontrado() {
                UsuarioResponseDTO usuarioResponse = new UsuarioResponseDTO(
                        usuarioId,
                        "João Silva",
                        "joao@email.com",
                        true,
                        Set.of("USER")
                );

                when(usuarioService.findById(usuarioId)).thenReturn(usuarioResponse);

                ResponseEntity<UsuarioResponseDTO> resposta = usuarioController.findById(usuarioId);

                assertEquals(HttpStatus.OK, resposta.getStatusCode());
                assertNotNull(resposta.getBody());
                assertEquals("João Silva", resposta.getBody().nome());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_buscar_usuario_por_email {

        String email;

        @BeforeEach
        void setup() {
            email = "joao@email.com";
        }

        @Nested
        class Quando_usuario_existe {

            @Test
            void deve_retornar_usuario_encontrado() {
                UsuarioResponseDTO usuarioResponse = new UsuarioResponseDTO(
                        UUID.randomUUID(),
                        "João Silva",
                        email,
                        true,
                        Set.of("USER")
                );

                when(usuarioService.findByEmail(email)).thenReturn(usuarioResponse);

                ResponseEntity<UsuarioResponseDTO> resposta = usuarioController.findByEmail(email);

                assertEquals(HttpStatus.OK, resposta.getStatusCode());
                assertNotNull(resposta.getBody());
                assertEquals(email, resposta.getBody().email());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_atualizar_usuario {

        UUID usuarioId;
        UsuarioRequestDTO dto;

        @BeforeEach
        void setup() {
            usuarioId = UUID.randomUUID();
            dto = new UsuarioRequestDTO(
                    "João Silva Atualizado",
                    "joao@email.com",
                    "novaSenha123"
            );
        }

        @Nested
        class Quando_usuario_existe {

            @Test
            void deve_atualizar_usuario_com_sucesso() {
                UsuarioResponseDTO usuarioAtualizado = new UsuarioResponseDTO(
                        usuarioId,
                        "João Silva Atualizado",
                        "joao@email.com",
                        true,
                        Set.of("USER")
                );

                when(usuarioService.update(eq(usuarioId), any(UsuarioRequestDTO.class)))
                        .thenReturn(usuarioAtualizado);

                ResponseEntity<UsuarioResponseDTO> resposta = usuarioController.update(usuarioId, dto);

                assertEquals(HttpStatus.OK, resposta.getStatusCode());
                assertNotNull(resposta.getBody());
                verify(usuarioService).update(eq(usuarioId), any(UsuarioRequestDTO.class));
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_desativar_usuario {

        UUID usuarioId;

        @BeforeEach
        void setup() {
            usuarioId = UUID.randomUUID();
        }

        @Nested
        class Quando_usuario_existe {

            @Test
            void deve_desativar_usuario_com_sucesso() {
                doNothing().when(usuarioService).deactivate(usuarioId);

                ResponseEntity<Void> resposta = usuarioController.deactivate(usuarioId);

                assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
                verify(usuarioService).deactivate(usuarioId);
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_deletar_usuario {

        UUID usuarioId;

        @BeforeEach
        void setup() {
            usuarioId = UUID.randomUUID();
        }

        @Nested
        class Quando_usuario_existe {

            @Test
            void deve_deletar_usuario_com_sucesso() {
                doNothing().when(usuarioService).delete(usuarioId);

                ResponseEntity<Void> resposta = usuarioController.delete(usuarioId);

                assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
                verify(usuarioService).delete(usuarioId);
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_adicionar_role {

        UUID usuarioId;
        String role;

        @BeforeEach
        void setup() {
            usuarioId = UUID.randomUUID();
            role = "ADMIN";
        }

        @Nested
        class Quando_role_valida {

            @Test
            void deve_adicionar_role_com_sucesso() {
                UsuarioResponseDTO usuarioComNovaRole = new UsuarioResponseDTO(
                        usuarioId,
                        "João Silva",
                        "joao@email.com",
                        true,
                        Set.of("USER", "ADMIN")
                );

                when(usuarioService.addRole(usuarioId, role)).thenReturn(usuarioComNovaRole);

                ResponseEntity<UsuarioResponseDTO> resposta = usuarioController.addRole(usuarioId, role);

                assertEquals(HttpStatus.OK, resposta.getStatusCode());
                assertNotNull(resposta.getBody());
                assertTrue(resposta.getBody().roles().contains("ADMIN"));
                verify(usuarioService).addRole(usuarioId, role);
            }
        }
    }
}