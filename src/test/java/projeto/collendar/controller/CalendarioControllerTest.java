package projeto.collendar.controller;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import projeto.collendar.dtos.request.CalendarioRequestDTO;
import projeto.collendar.dtos.response.CalendarioResponseDTO;
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
class CalendarioControllerTest {

    @Mock
    private CalendarioService calendarioService;

    @Mock
    private CompartilhamentoService compartilhamentoService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private CalendarioController calendarioController;

    @Nested
    class Dado_uma_requisicao_para_criar_calendario {

        CalendarioRequestDTO dto;
        UUID usuarioId;

        @BeforeEach
        void setup() {
            usuarioId = UUID.randomUUID();
            dto = new CalendarioRequestDTO(
                    "Trabalho",
                    "Calendário de trabalho",
                    "#FF5733"
            );
        }

        @Nested
        class Quando_dados_validos {

            @Test
            void deve_criar_calendario_com_sucesso() {
                CalendarioResponseDTO calendarioResponse = new CalendarioResponseDTO(
                        UUID.randomUUID(),
                        "Trabalho",
                        "Calendário de trabalho",
                        "#FF5733",
                        usuarioId,
                        "João Silva",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        true,
                        null
                );

                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(calendarioService.create(any(CalendarioRequestDTO.class), eq(usuarioId)))
                        .thenReturn(calendarioResponse);

                ResponseEntity<CalendarioResponseDTO> resposta = calendarioController.create(dto);

                assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
                assertNotNull(resposta.getBody());
                assertEquals("Trabalho", resposta.getBody().nome());
                assertTrue(resposta.getBody().proprietario());
                verify(calendarioService).create(any(CalendarioRequestDTO.class), eq(usuarioId));
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_buscar_calendario_por_id {

        UUID calendarioId;
        UUID usuarioId;

        @BeforeEach
        void setup() {
            calendarioId = UUID.randomUUID();
            usuarioId = UUID.randomUUID();
        }

        @Nested
        class Quando_usuario_tem_acesso {

            @Test
            void deve_retornar_calendario_encontrado() {
                CalendarioResponseDTO calendarioResponse = new CalendarioResponseDTO(
                        calendarioId,
                        "Trabalho",
                        "Calendário de trabalho",
                        "#FF5733",
                        usuarioId,
                        "João Silva",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        true,
                        null
                );

                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(compartilhamentoService.hasAccess(calendarioId, usuarioId)).thenReturn(true);
                when(calendarioService.findById(calendarioId)).thenReturn(calendarioResponse);

                ResponseEntity<CalendarioResponseDTO> resposta = calendarioController.findById(calendarioId);

                assertEquals(HttpStatus.OK, resposta.getStatusCode());
                assertNotNull(resposta.getBody());
                assertEquals("Trabalho", resposta.getBody().nome());
            }
        }

        @Nested
        class Quando_usuario_nao_tem_acesso {

            @Test
            void deve_lancar_access_denied_exception() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(compartilhamentoService.hasAccess(calendarioId, usuarioId)).thenReturn(false);

                assertThrows(AccessDeniedException.class,
                        () -> calendarioController.findById(calendarioId));
                verify(calendarioService, never()).findById(calendarioId);
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_listar_meus_calendarios {

        UUID usuarioId;

        @BeforeEach
        void setup() {
            usuarioId = UUID.randomUUID();
        }

        @Nested
        class Quando_listar_calendarios {

            @Test
            void deve_retornar_calendarios_do_usuario() {
                List<CalendarioResponseDTO> calendarios = Arrays.asList(
                        new CalendarioResponseDTO(
                                UUID.randomUUID(),
                                "Trabalho",
                                "Calendário de trabalho",
                                "#FF5733",
                                usuarioId,
                                "João Silva",
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                true,
                                null
                        ),
                        new CalendarioResponseDTO(
                                UUID.randomUUID(),
                                "Pessoal",
                                "Calendário pessoal",
                                "#00FF00",
                                usuarioId,
                                "João Silva",
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                true,
                                null
                        )
                );

                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(calendarioService.listByUsuario(usuarioId)).thenReturn(calendarios);

                ResponseEntity<List<CalendarioResponseDTO>> resposta = calendarioController.listMeus();

                assertEquals(HttpStatus.OK, resposta.getStatusCode());
                assertNotNull(resposta.getBody());
                assertEquals(2, resposta.getBody().size());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_listar_calendarios_acessiveis {

        UUID usuarioId;

        @BeforeEach
        void setup() {
            usuarioId = UUID.randomUUID();
        }

        @Nested
        class Quando_listar_calendarios_acessiveis {

            @Test
            void deve_retornar_proprios_e_compartilhados() {
                List<CalendarioResponseDTO> proprios = Arrays.asList(
                        new CalendarioResponseDTO(
                                UUID.randomUUID(),
                                "Meu Trabalho",
                                "Descrição",
                                "#FF5733",
                                usuarioId,
                                "João Silva",
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                true,
                                null
                        )
                );

                List<CalendarioResponseDTO> compartilhados = Arrays.asList(
                        new CalendarioResponseDTO(
                                UUID.randomUUID(),
                                "Trabalho Compartilhado",
                                "Descrição",
                                "#00FF00",
                                UUID.randomUUID(),
                                "Maria Santos",
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                false,
                                null
                        )
                );

                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(calendarioService.listByUsuario(usuarioId)).thenReturn(proprios);
                when(compartilhamentoService.listSharedWithUsuario(usuarioId)).thenReturn(compartilhados);

                ResponseEntity<List<CalendarioResponseDTO>> resposta = calendarioController.listAcessiveis();

                assertEquals(HttpStatus.OK, resposta.getStatusCode());
                assertNotNull(resposta.getBody());
                assertEquals(2, resposta.getBody().size());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_listar_calendarios_paginado {

        UUID usuarioId;
        Pageable pageable;

        @BeforeEach
        void setup() {
            usuarioId = UUID.randomUUID();
            pageable = PageRequest.of(0, 10);
        }

        @Nested
        class Quando_listar_paginado {

            @Test
            void deve_retornar_pagina_de_calendarios() {
                CalendarioResponseDTO calendario = new CalendarioResponseDTO(
                        UUID.randomUUID(),
                        "Trabalho",
                        "Descrição",
                        "#FF5733",
                        usuarioId,
                        "João Silva",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        true,
                        null
                );

                Page<CalendarioResponseDTO> page = new PageImpl<>(Arrays.asList(calendario));

                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(calendarioService.listByUsuarioPaginated(usuarioId, pageable)).thenReturn(page);

                ResponseEntity<Page<CalendarioResponseDTO>> resposta =
                        calendarioController.listMeusPaginado(pageable);

                assertEquals(HttpStatus.OK, resposta.getStatusCode());
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().getTotalElements());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_buscar_por_nome {

        Pageable pageable;

        @BeforeEach
        void setup() {
            pageable = PageRequest.of(0, 10);
        }

        @Nested
        class Quando_buscar_por_nome {

            @Test
            void deve_retornar_calendarios_encontrados() {
                CalendarioResponseDTO calendario = new CalendarioResponseDTO(
                        UUID.randomUUID(),
                        "Trabalho",
                        "Descrição",
                        "#FF5733",
                        UUID.randomUUID(),
                        "João Silva",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        true,
                        null
                );

                Page<CalendarioResponseDTO> page = new PageImpl<>(Arrays.asList(calendario));

                when(calendarioService.searchByNome("Trabalho", pageable)).thenReturn(page);

                ResponseEntity<Page<CalendarioResponseDTO>> resposta =
                        calendarioController.searchByNome("Trabalho", pageable);

                assertEquals(HttpStatus.OK, resposta.getStatusCode());
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().getTotalElements());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_atualizar_calendario {

        UUID calendarioId;
        UUID usuarioId;
        CalendarioRequestDTO dto;

        @BeforeEach
        void setup() {
            calendarioId = UUID.randomUUID();
            usuarioId = UUID.randomUUID();
            dto = new CalendarioRequestDTO(
                    "Trabalho Atualizado",
                    "Nova descrição",
                    "#00FF00"
            );
        }

        @Nested
        class Quando_usuario_eh_proprietario {

            @Test
            void deve_atualizar_calendario_com_sucesso() {
                CalendarioResponseDTO calendarioAtualizado = new CalendarioResponseDTO(
                        calendarioId,
                        "Trabalho Atualizado",
                        "Nova descrição",
                        "#00FF00",
                        usuarioId,
                        "João Silva",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        true,
                        null
                );

                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(calendarioService.isOwner(calendarioId, usuarioId)).thenReturn(true);
                when(calendarioService.update(eq(calendarioId), any(CalendarioRequestDTO.class)))
                        .thenReturn(calendarioAtualizado);

                ResponseEntity<CalendarioResponseDTO> resposta =
                        calendarioController.update(calendarioId, dto);

                assertEquals(HttpStatus.OK, resposta.getStatusCode());
                assertNotNull(resposta.getBody());
                assertEquals("Trabalho Atualizado", resposta.getBody().nome());
            }
        }

        @Nested
        class Quando_usuario_nao_eh_proprietario {

            @Test
            void deve_lancar_access_denied_exception() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(calendarioService.isOwner(calendarioId, usuarioId)).thenReturn(false);

                assertThrows(AccessDeniedException.class,
                        () -> calendarioController.update(calendarioId, dto));
                verify(calendarioService, never()).update(any(), any());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_deletar_calendario {

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
            void deve_deletar_calendario_com_sucesso() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(calendarioService.isOwner(calendarioId, usuarioId)).thenReturn(true);
                doNothing().when(calendarioService).delete(calendarioId);

                ResponseEntity<Void> resposta = calendarioController.delete(calendarioId);

                assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
                verify(calendarioService).delete(calendarioId);
            }
        }

        @Nested
        class Quando_usuario_nao_eh_proprietario {

            @Test
            void deve_lancar_access_denied_exception() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(calendarioService.isOwner(calendarioId, usuarioId)).thenReturn(false);

                assertThrows(AccessDeniedException.class,
                        () -> calendarioController.delete(calendarioId));
                verify(calendarioService, never()).delete(calendarioId);
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_verificar_se_pode_editar {

        UUID calendarioId;
        UUID usuarioId;

        @BeforeEach
        void setup() {
            calendarioId = UUID.randomUUID();
            usuarioId = UUID.randomUUID();
        }

        @Nested
        class Quando_verificar_permissao_edicao {

            @Test
            void deve_retornar_true_quando_pode_editar() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(compartilhamentoService.canEdit(calendarioId, usuarioId)).thenReturn(true);

                ResponseEntity<Boolean> resposta = calendarioController.canEdit(calendarioId);

                assertEquals(HttpStatus.OK, resposta.getStatusCode());
                assertTrue(resposta.getBody());
            }

            @Test
            void deve_retornar_false_quando_nao_pode_editar() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(compartilhamentoService.canEdit(calendarioId, usuarioId)).thenReturn(false);

                ResponseEntity<Boolean> resposta = calendarioController.canEdit(calendarioId);

                assertEquals(HttpStatus.OK, resposta.getStatusCode());
                assertFalse(resposta.getBody());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_contar_eventos {

        UUID calendarioId;
        UUID usuarioId;

        @BeforeEach
        void setup() {
            calendarioId = UUID.randomUUID();
            usuarioId = UUID.randomUUID();
        }

        @Nested
        class Quando_usuario_tem_acesso {

            @Test
            void deve_retornar_quantidade_de_eventos() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(compartilhamentoService.hasAccess(calendarioId, usuarioId)).thenReturn(true);
                when(calendarioService.countByUsuario(calendarioId)).thenReturn(5L);

                ResponseEntity<Long> resposta = calendarioController.countEventos(calendarioId);

                assertEquals(HttpStatus.OK, resposta.getStatusCode());
                assertEquals(5L, resposta.getBody());
            }
        }

        @Nested
        class Quando_usuario_nao_tem_acesso {

            @Test
            void deve_lancar_access_denied_exception() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(compartilhamentoService.hasAccess(calendarioId, usuarioId)).thenReturn(false);

                assertThrows(AccessDeniedException.class,
                        () -> calendarioController.countEventos(calendarioId));
            }
        }
    }
}