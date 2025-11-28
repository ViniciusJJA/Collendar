package projeto.collendar.controller;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import projeto.collendar.dtos.request.CompartilhamentoRequestDTO;
import projeto.collendar.dtos.response.CalendarioResponseDTO;
import projeto.collendar.dtos.response.CompartilhamentoResponseDTO;
import projeto.collendar.dtos.response.PermissaoResponseDTO;
import projeto.collendar.enums.TipoPermissao;
import projeto.collendar.exception.AccessDeniedException;
import projeto.collendar.service.CalendarioService;
import projeto.collendar.service.CompartilhamentoService;
import projeto.collendar.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CompartilhamentoControllerTest {

    @Mock
    private CompartilhamentoService compartilhamentoService;

    @Mock
    private CalendarioService calendarioService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private CompartilhamentoController compartilhamentoController;

    @Nested
    class Dado_uma_requisicao_para_compartilhar_calendario {

        CompartilhamentoRequestDTO dto;
        UUID calendarioId;
        UUID usuarioId;

        @BeforeEach
        void setup() {
            calendarioId = UUID.randomUUID();
            usuarioId = UUID.randomUUID();

            dto = new CompartilhamentoRequestDTO(
                    calendarioId,
                    "maria@email.com",
                    TipoPermissao.VISUALIZAR
            );
        }

        @Nested
        class Quando_usuario_eh_proprietario {

            @Test
            void deve_compartilhar_calendario_com_sucesso() {
                CompartilhamentoResponseDTO compartilhamentoResponse = new CompartilhamentoResponseDTO(
                        UUID.randomUUID(),
                        calendarioId,
                        "Trabalho",
                        UUID.randomUUID(),
                        "Maria Santos",
                        "maria@email.com",
                        TipoPermissao.VISUALIZAR,
                        LocalDateTime.now()
                );

                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(calendarioService.isOwner(calendarioId, usuarioId)).thenReturn(true);
                when(compartilhamentoService.create(any(CompartilhamentoRequestDTO.class)))
                        .thenReturn(compartilhamentoResponse);

                ResponseEntity<CompartilhamentoResponseDTO> resposta =
                        compartilhamentoController.create(dto);

                assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
                assertNotNull(resposta.getBody());
                assertEquals("maria@email.com", resposta.getBody().usuarioEmail());
                assertEquals(TipoPermissao.VISUALIZAR, resposta.getBody().permissao());
                verify(compartilhamentoService).create(any(CompartilhamentoRequestDTO.class));
            }
        }

        @Nested
        class Quando_usuario_nao_eh_proprietario {

            @Test
            void deve_lancar_access_denied_exception() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(calendarioService.isOwner(calendarioId, usuarioId)).thenReturn(false);

                assertThrows(AccessDeniedException.class,
                        () -> compartilhamentoController.create(dto));
                verify(compartilhamentoService, never()).create(any());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_listar_compartilhamentos_do_calendario {

        UUID calendarioId;
        UUID usuarioId;

        @BeforeEach
        void setup() {
            calendarioId = UUID.randomUUID();
            usuarioId = UUID.randomUUID();
        }

        @Nested
        class Quando_usuario_eh_proprietario {

            @Test
            void deve_retornar_lista_de_compartilhamentos() {
                List<CompartilhamentoResponseDTO> compartilhamentos = Arrays.asList(
                        new CompartilhamentoResponseDTO(
                                UUID.randomUUID(),
                                calendarioId,
                                "Trabalho",
                                UUID.randomUUID(),
                                "Maria Santos",
                                "maria@email.com",
                                TipoPermissao.VISUALIZAR,
                                LocalDateTime.now()
                        ),
                        new CompartilhamentoResponseDTO(
                                UUID.randomUUID(),
                                calendarioId,
                                "Trabalho",
                                UUID.randomUUID(),
                                "Pedro Oliveira",
                                "pedro@email.com",
                                TipoPermissao.EDITAR,
                                LocalDateTime.now()
                        )
                );

                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(calendarioService.isOwner(calendarioId, usuarioId)).thenReturn(true);
                when(compartilhamentoService.listByCalendario(calendarioId))
                        .thenReturn(compartilhamentos);

                ResponseEntity<List<CompartilhamentoResponseDTO>> resposta =
                        compartilhamentoController.listByCalendario(calendarioId);

                assertEquals(HttpStatus.OK, resposta.getStatusCode());
                assertNotNull(resposta.getBody());
                assertEquals(2, resposta.getBody().size());
            }
        }

        @Nested
        class Quando_usuario_nao_eh_proprietario {

            @Test
            void deve_lancar_access_denied_exception() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(calendarioService.isOwner(calendarioId, usuarioId)).thenReturn(false);

                assertThrows(AccessDeniedException.class,
                        () -> compartilhamentoController.listByCalendario(calendarioId));
                verify(compartilhamentoService, never()).listByCalendario(calendarioId);
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_listar_calendarios_compartilhados_comigo {

        UUID usuarioId;

        @BeforeEach
        void setup() {
            usuarioId = UUID.randomUUID();
        }

        @Nested
        class Quando_listar_calendarios_recebidos {

            @Test
            void deve_retornar_calendarios_compartilhados() {
                List<CalendarioResponseDTO> calendarios = Arrays.asList(
                        new CalendarioResponseDTO(
                                UUID.randomUUID(),
                                "Trabalho Compartilhado",
                                "Descrição",
                                "#FF5733",
                                UUID.randomUUID(),
                                "João Silva",
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                false,
                                TipoPermissao.VISUALIZAR
                        )
                );

                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(compartilhamentoService.listSharedWithUsuario(usuarioId))
                        .thenReturn(calendarios);

                ResponseEntity<List<CalendarioResponseDTO>> resposta =
                        compartilhamentoController.listRecebidos();

                assertEquals(HttpStatus.OK, resposta.getStatusCode());
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().size());
                assertFalse(resposta.getBody().get(0).proprietario());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_listar_detalhes_compartilhamentos_recebidos {

        UUID usuarioId;

        @BeforeEach
        void setup() {
            usuarioId = UUID.randomUUID();
        }

        @Nested
        class Quando_listar_detalhes {

            @Test
            void deve_retornar_detalhes_dos_compartilhamentos() {
                List<CompartilhamentoResponseDTO> compartilhamentos = Arrays.asList(
                        new CompartilhamentoResponseDTO(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                "Trabalho",
                                usuarioId,
                                "Maria Santos",
                                "maria@email.com",
                                TipoPermissao.VISUALIZAR,
                                LocalDateTime.now()
                        )
                );

                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(compartilhamentoService.listReceivedByUsuario(usuarioId))
                        .thenReturn(compartilhamentos);

                ResponseEntity<List<CompartilhamentoResponseDTO>> resposta =
                        compartilhamentoController.listRecebidosDetalhes();

                assertEquals(HttpStatus.OK, resposta.getStatusCode());
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().size());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_obter_minha_permissao {

        UUID calendarioId;
        UUID usuarioId;

        @BeforeEach
        void setup() {
            calendarioId = UUID.randomUUID();
            usuarioId = UUID.randomUUID();
        }

        @Nested
        class Quando_obter_permissao {

            @Test
            void deve_retornar_permissao_do_usuario() {
                PermissaoResponseDTO permissao = new PermissaoResponseDTO(
                        false,
                        true,
                        false,
                        TipoPermissao.VISUALIZAR
                );

                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(compartilhamentoService.getMyPermission(calendarioId, usuarioId))
                        .thenReturn(permissao);

                ResponseEntity<PermissaoResponseDTO> resposta =
                        compartilhamentoController.getMyPermission(calendarioId);

                assertEquals(HttpStatus.OK, resposta.getStatusCode());
                assertNotNull(resposta.getBody());
                assertFalse(resposta.getBody().proprietario());
                assertTrue(resposta.getBody().podeVisualizar());
                assertFalse(resposta.getBody().podeEditar());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_atualizar_permissao {

        UUID compartilhamentoId;
        UUID calendarioId;
        UUID usuarioId;

        @BeforeEach
        void setup() {
            compartilhamentoId = UUID.randomUUID();
            calendarioId = UUID.randomUUID();
            usuarioId = UUID.randomUUID();
        }

        @Nested
        class Quando_usuario_eh_proprietario {

            @Test
            void deve_atualizar_permissao_com_sucesso() {
                CompartilhamentoResponseDTO compartilhamentoAtualizado = new CompartilhamentoResponseDTO(
                        compartilhamentoId,
                        calendarioId,
                        "Trabalho",
                        UUID.randomUUID(),
                        "Maria Santos",
                        "maria@email.com",
                        TipoPermissao.EDITAR,
                        LocalDateTime.now()
                );

                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(compartilhamentoService.getCalendarioIdByCompartilhamento(compartilhamentoId))
                        .thenReturn(calendarioId);
                when(calendarioService.isOwner(calendarioId, usuarioId)).thenReturn(true);
                when(compartilhamentoService.updatePermissao(compartilhamentoId, TipoPermissao.EDITAR))
                        .thenReturn(compartilhamentoAtualizado);

                ResponseEntity<CompartilhamentoResponseDTO> resposta =
                        compartilhamentoController.updatePermissao(compartilhamentoId, TipoPermissao.EDITAR);

                assertEquals(HttpStatus.OK, resposta.getStatusCode());
                assertNotNull(resposta.getBody());
                assertEquals(TipoPermissao.EDITAR, resposta.getBody().permissao());
            }
        }

        @Nested
        class Quando_usuario_nao_eh_proprietario {

            @Test
            void deve_lancar_access_denied_exception() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(compartilhamentoService.getCalendarioIdByCompartilhamento(compartilhamentoId))
                        .thenReturn(calendarioId);
                when(calendarioService.isOwner(calendarioId, usuarioId)).thenReturn(false);

                assertThrows(AccessDeniedException.class,
                        () -> compartilhamentoController.updatePermissao(compartilhamentoId, TipoPermissao.EDITAR));
                verify(compartilhamentoService, never()).updatePermissao(any(), any());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_remover_compartilhamento {

        UUID compartilhamentoId;
        UUID calendarioId;
        UUID usuarioId;
        UUID destinatarioId;

        @BeforeEach
        void setup() {
            compartilhamentoId = UUID.randomUUID();
            calendarioId = UUID.randomUUID();
            usuarioId = UUID.randomUUID();
            destinatarioId = UUID.randomUUID();
        }

        @Nested
        class Quando_usuario_eh_proprietario {

            @Test
            void deve_remover_compartilhamento_com_sucesso() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(compartilhamentoService.getCalendarioIdByCompartilhamento(compartilhamentoId))
                        .thenReturn(calendarioId);
                when(compartilhamentoService.getDestinatarioIdByCompartilhamento(compartilhamentoId))
                        .thenReturn(destinatarioId);
                when(calendarioService.isOwner(calendarioId, usuarioId)).thenReturn(true);
                doNothing().when(compartilhamentoService).delete(compartilhamentoId);

                ResponseEntity<Void> resposta = compartilhamentoController.delete(compartilhamentoId);

                assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
                verify(compartilhamentoService).delete(compartilhamentoId);
            }
        }

        @Nested
        class Quando_usuario_eh_destinatario {

            @Test
            void deve_remover_compartilhamento_com_sucesso() {
                when(securityUtils.getLoggedUserId()).thenReturn(destinatarioId);
                when(compartilhamentoService.getCalendarioIdByCompartilhamento(compartilhamentoId))
                        .thenReturn(calendarioId);
                when(compartilhamentoService.getDestinatarioIdByCompartilhamento(compartilhamentoId))
                        .thenReturn(destinatarioId);
                when(calendarioService.isOwner(calendarioId, destinatarioId)).thenReturn(false);
                doNothing().when(compartilhamentoService).delete(compartilhamentoId);

                ResponseEntity<Void> resposta = compartilhamentoController.delete(compartilhamentoId);

                assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
                verify(compartilhamentoService).delete(compartilhamentoId);
            }
        }

        @Nested
        class Quando_usuario_nao_tem_permissao {

            @Test
            void deve_lancar_access_denied_exception() {
                UUID outroUsuarioId = UUID.randomUUID();

                when(securityUtils.getLoggedUserId()).thenReturn(outroUsuarioId);
                when(compartilhamentoService.getCalendarioIdByCompartilhamento(compartilhamentoId))
                        .thenReturn(calendarioId);
                when(compartilhamentoService.getDestinatarioIdByCompartilhamento(compartilhamentoId))
                        .thenReturn(destinatarioId);
                when(calendarioService.isOwner(calendarioId, outroUsuarioId)).thenReturn(false);

                assertThrows(AccessDeniedException.class,
                        () -> compartilhamentoController.delete(compartilhamentoId));
                verify(compartilhamentoService, never()).delete(compartilhamentoId);
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_contar_compartilhamentos {

        UUID calendarioId;
        UUID usuarioId;

        @BeforeEach
        void setup() {
            calendarioId = UUID.randomUUID();
            usuarioId = UUID.randomUUID();
        }

        @Nested
        class Quando_usuario_eh_proprietario {

            @Test
            void deve_retornar_quantidade_de_compartilhamentos() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(calendarioService.isOwner(calendarioId, usuarioId)).thenReturn(true);
                when(compartilhamentoService.countByCalendario(calendarioId)).thenReturn(3L);

                ResponseEntity<Long> resposta =
                        compartilhamentoController.countByCalendario(calendarioId);

                assertEquals(HttpStatus.OK, resposta.getStatusCode());
                assertEquals(3L, resposta.getBody());
            }
        }

        @Nested
        class Quando_usuario_nao_eh_proprietario {

            @Test
            void deve_lancar_access_denied_exception() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(calendarioService.isOwner(calendarioId, usuarioId)).thenReturn(false);

                assertThrows(AccessDeniedException.class,
                        () -> compartilhamentoController.countByCalendario(calendarioId));
            }
        }
    }
}