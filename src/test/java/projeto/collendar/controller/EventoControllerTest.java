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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import projeto.collendar.dtos.request.EventoRequestDTO;
import projeto.collendar.dtos.response.EventoResponseDTO;
import projeto.collendar.enums.TipoRecorrencia;
import projeto.collendar.service.CompartilhamentoService;
import projeto.collendar.service.EventoService;
import projeto.collendar.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class EventoControllerTest {

    @Mock
    private EventoService eventoService;

    @Mock
    private CompartilhamentoService compartilhamentoService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private EventoController eventoController;

    @Nested
    class Dado_um_evento_valido {

        UUID eventoId;
        UUID calendarioId;
        UUID usuarioId;
        LocalDateTime dataInicio;
        LocalDateTime dataFim;
        EventoRequestDTO eventoRequest;
        EventoResponseDTO eventoResponse;

        @BeforeEach
        void setup() {
            eventoId = UUID.randomUUID();
            calendarioId = UUID.randomUUID();
            usuarioId = UUID.randomUUID();
            dataInicio = LocalDateTime.of(2025, 1, 15, 10, 0);
            dataFim = LocalDateTime.of(2025, 1, 15, 12, 0);

            eventoRequest = new EventoRequestDTO(
                    "Reunião",
                    "Reunião importante",
                    dataInicio,
                    dataFim,
                    "Sala 101",
                    "#FF5733",
                    false,
                    false,
                    null,
                    calendarioId
            );

            eventoResponse = new EventoResponseDTO(
                    eventoId,
                    "Reunião",
                    "Reunião importante",
                    dataInicio,
                    dataFim,
                    "Sala 101",
                    "#FF5733",
                    false,
                    false,
                    null,
                    calendarioId,
                    "Trabalho"
            );
        }

        @Nested
        class Quando_criar_evento_com_permissao_editar {

            ResponseEntity<EventoResponseDTO> resposta;

            @BeforeEach
            void setup() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(compartilhamentoService.canEdit(calendarioId, usuarioId)).thenReturn(true);
                when(eventoService.create(any(EventoRequestDTO.class))).thenReturn(eventoResponse);

                resposta = eventoController.create(eventoRequest);
            }

            @Test
            void deve_retornar_status_created() {
                assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_evento_criado() {
                assertNotNull(resposta.getBody());
                assertEquals("Reunião", resposta.getBody().titulo());
                assertEquals(calendarioId, resposta.getBody().calendarioId());
            }

            @Test
            void deve_chamar_service_create() {
                verify(eventoService, times(1)).create(any(EventoRequestDTO.class));
            }
        }

        @Nested
        class Quando_criar_evento_sem_permissao_editar {

            @BeforeEach
            void setup() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(compartilhamentoService.canEdit(calendarioId, usuarioId)).thenReturn(false);
            }

            @Test
            void deve_lancar_excecao_acesso_negado() {
                assertThrows(
                        Exception.class,
                        () -> eventoController.create(eventoRequest)
                );
            }

            @Test
            void nao_deve_chamar_service_create() {
                try {
                    eventoController.create(eventoRequest);
                } catch (Exception e) {
                    // Exceção esperada
                }
                verify(eventoService, never()).create(any(EventoRequestDTO.class));
            }
        }

        @Nested
        class Quando_buscar_evento_por_id_com_acesso {

            ResponseEntity<EventoResponseDTO> resposta;

            @BeforeEach
            void setup() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(eventoService.getCalendarioIdByEvento(eventoId)).thenReturn(calendarioId);
                when(compartilhamentoService.hasAccess(calendarioId, usuarioId)).thenReturn(true);
                when(eventoService.findById(eventoId)).thenReturn(eventoResponse);

                resposta = eventoController.findById(eventoId);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_evento_encontrado() {
                assertNotNull(resposta.getBody());
                assertEquals("Reunião", resposta.getBody().titulo());
            }
        }

        @Nested
        class Quando_buscar_evento_por_id_sem_acesso {

            @BeforeEach
            void setup() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(eventoService.getCalendarioIdByEvento(eventoId)).thenReturn(calendarioId);
                when(compartilhamentoService.hasAccess(calendarioId, usuarioId)).thenReturn(false);
            }

            @Test
            void deve_lancar_excecao_acesso_negado() {
                assertThrows(
                        Exception.class,
                        () -> eventoController.findById(eventoId)
                );
            }
        }

        @Nested
        class Quando_listar_eventos_por_calendario {

            ResponseEntity<List<EventoResponseDTO>> resposta;
            List<EventoResponseDTO> eventosLista;

            @BeforeEach
            void setup() {
                eventosLista = List.of(eventoResponse);
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(compartilhamentoService.hasAccess(calendarioId, usuarioId)).thenReturn(true);
                when(eventoService.listByCalendario(calendarioId)).thenReturn(eventosLista);

                resposta = eventoController.listByCalendario(calendarioId);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_eventos_do_calendario() {
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().size());
                assertEquals("Reunião", resposta.getBody().get(0).titulo());
            }
        }

        @Nested
        class Quando_listar_eventos_paginado {

            ResponseEntity<Page<EventoResponseDTO>> resposta;
            Page<EventoResponseDTO> page;
            Pageable pageable;

            @BeforeEach
            void setup() {
                pageable = PageRequest.of(0, 10);
                page = new PageImpl<>(List.of(eventoResponse));

                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(compartilhamentoService.hasAccess(calendarioId, usuarioId)).thenReturn(true);
                when(eventoService.listByCalendarioPaginated(calendarioId, pageable)).thenReturn(page);

                resposta = eventoController.listByCalendarioPaginado(calendarioId, pageable);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_page_com_eventos() {
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().getTotalElements());
            }
        }

        @Nested
        class Quando_buscar_eventos_por_periodo {

            ResponseEntity<List<EventoResponseDTO>> resposta;
            LocalDateTime inicio;
            LocalDateTime fim;

            @BeforeEach
            void setup() {
                inicio = LocalDateTime.of(2025, 1, 1, 0, 0);
                fim = LocalDateTime.of(2025, 1, 31, 23, 59);

                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(compartilhamentoService.hasAccess(calendarioId, usuarioId)).thenReturn(true);
                when(eventoService.findByCalendarioAndPeriod(calendarioId, inicio, fim))
                        .thenReturn(List.of(eventoResponse));

                resposta = eventoController.findByCalendarioAndPeriodo(calendarioId, inicio, fim);
            }

            @Test
            void deve_retornar_eventos_no_periodo() {
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().size());
            }
        }

        @Nested
        class Quando_buscar_eventos_por_titulo {

            ResponseEntity<Page<EventoResponseDTO>> resposta;
            Pageable pageable;

            @BeforeEach
            void setup() {
                pageable = PageRequest.of(0, 10);
                Page<EventoResponseDTO> page = new PageImpl<>(List.of(eventoResponse));

                when(eventoService.searchByTitulo("Reunião", pageable)).thenReturn(page);

                resposta = eventoController.searchByTitulo("Reunião", pageable);
            }

            @Test
            void deve_retornar_eventos_encontrados() {
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().getTotalElements());
            }
        }

        @Nested
        class Quando_listar_eventos_recorrentes {

            ResponseEntity<List<EventoResponseDTO>> resposta;
            EventoResponseDTO eventoRecorrente;

            @BeforeEach
            void setup() {
                eventoRecorrente = new EventoResponseDTO(
                        UUID.randomUUID(),
                        "Reunião Semanal",
                        "Reunião recorrente",
                        dataInicio,
                        dataFim,
                        "Sala 101",
                        "#FF5733",
                        false,
                        true,
                        TipoRecorrencia.SEMANAL,
                        calendarioId,
                        "Trabalho"
                );

                when(eventoService.listRecorrentes()).thenReturn(List.of(eventoRecorrente));

                resposta = eventoController.listRecorrentes();
            }

            @Test
            void deve_retornar_apenas_eventos_recorrentes() {
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().size());
                assertTrue(resposta.getBody().get(0).recorrente());
            }
        }

        @Nested
        class Quando_atualizar_evento_com_permissao {

            ResponseEntity<EventoResponseDTO> resposta;
            EventoResponseDTO eventoAtualizado;

            @BeforeEach
            void setup() {
                eventoAtualizado = new EventoResponseDTO(
                        eventoId,
                        "Reunião Atualizada",
                        "Nova descrição",
                        dataInicio,
                        dataFim,
                        "Sala 102",
                        "#00FF00",
                        false,
                        false,
                        null,
                        calendarioId,
                        "Trabalho"
                );

                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(eventoService.getCalendarioIdByEvento(eventoId)).thenReturn(calendarioId);
                when(compartilhamentoService.canEdit(calendarioId, usuarioId)).thenReturn(true);
                when(eventoService.update(eq(eventoId), any(EventoRequestDTO.class)))
                        .thenReturn(eventoAtualizado);

                resposta = eventoController.update(eventoId, eventoRequest);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_evento_atualizado() {
                assertNotNull(resposta.getBody());
                assertEquals("Reunião Atualizada", resposta.getBody().titulo());
            }
        }

        @Nested
        class Quando_atualizar_evento_sem_permissao {

            @BeforeEach
            void setup() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(eventoService.getCalendarioIdByEvento(eventoId)).thenReturn(calendarioId);
                when(compartilhamentoService.canEdit(calendarioId, usuarioId)).thenReturn(false);
            }

            @Test
            void deve_lancar_excecao_acesso_negado() {
                assertThrows(
                        Exception.class,
                        () -> eventoController.update(eventoId, eventoRequest)
                );
            }
        }

        @Nested
        class Quando_deletar_evento_com_permissao {

            ResponseEntity<Void> resposta;

            @BeforeEach
            void setup() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(eventoService.getCalendarioIdByEvento(eventoId)).thenReturn(calendarioId);
                when(compartilhamentoService.canEdit(calendarioId, usuarioId)).thenReturn(true);
                doNothing().when(eventoService).delete(eventoId);

                resposta = eventoController.delete(eventoId);
            }

            @Test
            void deve_retornar_status_no_content() {
                assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
            }

            @Test
            void deve_chamar_service_delete() {
                verify(eventoService, times(1)).delete(eventoId);
            }
        }

        @Nested
        class Quando_deletar_evento_sem_permissao {

            @BeforeEach
            void setup() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(eventoService.getCalendarioIdByEvento(eventoId)).thenReturn(calendarioId);
                when(compartilhamentoService.canEdit(calendarioId, usuarioId)).thenReturn(false);
            }

            @Test
            void deve_lancar_excecao_acesso_negado() {
                assertThrows(
                        Exception.class,
                        () -> eventoController.delete(eventoId)
                );
            }

            @Test
            void nao_deve_chamar_service_delete() {
                try {
                    eventoController.delete(eventoId);
                } catch (Exception e) {
                    // Exceção esperada
                }
                verify(eventoService, never()).delete(eventoId);
            }
        }

        @Nested
        class Quando_contar_eventos_por_calendario {

            ResponseEntity<Long> resposta;

            @BeforeEach
            void setup() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(compartilhamentoService.hasAccess(calendarioId, usuarioId)).thenReturn(true);
                when(eventoService.countByCalendario(calendarioId)).thenReturn(5L);

                resposta = eventoController.countByCalendario(calendarioId);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_quantidade_correta() {
                assertEquals(5L, resposta.getBody());
            }
        }
    }

    @Nested
    class Dado_um_calendario_sem_acesso {

        UUID eventoId;
        UUID calendarioId;
        UUID usuarioId;

        @BeforeEach
        void setup() {
            eventoId = UUID.randomUUID();
            calendarioId = UUID.randomUUID();
            usuarioId = UUID.randomUUID();
        }

        @Nested
        class Quando_tentar_listar_eventos {

            @BeforeEach
            void setup() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(compartilhamentoService.hasAccess(calendarioId, usuarioId)).thenReturn(false);
            }

            @Test
            void deve_lancar_excecao_acesso_negado() {
                assertThrows(
                        Exception.class,
                        () -> eventoController.listByCalendario(calendarioId)
                );
            }
        }

        @Nested
        class Quando_tentar_contar_eventos {

            @BeforeEach
            void setup() {
                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(compartilhamentoService.hasAccess(calendarioId, usuarioId)).thenReturn(false);
            }

            @Test
            void deve_lancar_excecao_acesso_negado() {
                assertThrows(
                        Exception.class,
                        () -> eventoController.countByCalendario(calendarioId)
                );
            }
        }
    }

    @Nested
    class Dado_eventos_com_diferentes_tipos {

        UUID calendarioId;
        UUID usuarioId;

        @BeforeEach
        void setup() {
            calendarioId = UUID.randomUUID();
            usuarioId = UUID.randomUUID();
        }

        @Nested
        class Quando_buscar_eventos_dia_inteiro {

            ResponseEntity<List<EventoResponseDTO>> resposta;
            EventoResponseDTO eventoDiaInteiro;

            @BeforeEach
            void setup() {
                eventoDiaInteiro = new EventoResponseDTO(
                        UUID.randomUUID(),
                        "Feriado",
                        "Feriado nacional",
                        LocalDateTime.of(2025, 1, 1, 0, 0),
                        LocalDateTime.of(2025, 1, 1, 23, 59),
                        null,
                        "#FF0000",
                        true,
                        false,
                        null,
                        calendarioId,
                        "Pessoal"
                );

                when(securityUtils.getLoggedUserId()).thenReturn(usuarioId);
                when(compartilhamentoService.hasAccess(calendarioId, usuarioId)).thenReturn(true);
                when(eventoService.listByCalendario(calendarioId)).thenReturn(List.of(eventoDiaInteiro));

                resposta = eventoController.listByCalendario(calendarioId);
            }

            @Test
            void deve_retornar_evento_marcado_como_dia_inteiro() {
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().size());
                assertTrue(resposta.getBody().get(0).diaInteiro());
            }
        }

        @Nested
        class Quando_buscar_eventos_com_recorrencia_mensal {

            ResponseEntity<List<EventoResponseDTO>> resposta;
            EventoResponseDTO eventoMensal;

            @BeforeEach
            void setup() {
                eventoMensal = new EventoResponseDTO(
                        UUID.randomUUID(),
                        "Reunião Mensal",
                        "Reunião do mês",
                        LocalDateTime.now(),
                        LocalDateTime.now().plusHours(1),
                        "Sala 201",
                        "#0000FF",
                        false,
                        true,
                        TipoRecorrencia.MENSAL,
                        calendarioId,
                        "Trabalho"
                );

                when(eventoService.listRecorrentes()).thenReturn(List.of(eventoMensal));

                resposta = eventoController.listRecorrentes();
            }

            @Test
            void deve_retornar_evento_com_recorrencia_mensal() {
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().size());
                assertEquals(TipoRecorrencia.MENSAL, resposta.getBody().get(0).tipoRecorrencia());
            }
        }
    }
}