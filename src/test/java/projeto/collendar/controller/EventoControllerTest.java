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
import projeto.collendar.dto.EventoDTO;
import projeto.collendar.enums.TipoRecorrencia;
import projeto.collendar.model.Calendario;
import projeto.collendar.model.Evento;
import projeto.collendar.service.EventoService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class EventoControllerTest {

    @Mock
    private EventoService eventoService;

    @InjectMocks
    private EventoController eventoController;

    @Nested
    class Dado_uma_requisicao_para_criar_evento {

        Evento eventoParaCriar;
        Calendario calendario;
        UUID calendarioId;
        LocalDateTime dataInicio;
        LocalDateTime dataFim;

        @BeforeEach
        void setup() {
            calendarioId = UUID.randomUUID();
            dataInicio = LocalDateTime.of(2025, 1, 15, 10, 0);
            dataFim = LocalDateTime.of(2025, 1, 15, 12, 0);

            calendario = new Calendario();
            calendario.setId(calendarioId);
            calendario.setNome("Trabalho");

            eventoParaCriar = new Evento();
            eventoParaCriar.setTitulo("Reunião");
            eventoParaCriar.setDescricao("Reunião importante");
            eventoParaCriar.setDataInicio(dataInicio);
            eventoParaCriar.setDataFim(dataFim);
            eventoParaCriar.setDiaInteiro(false);
            eventoParaCriar.setRecorrente(false);
        }

        @Nested
        class Quando_dados_validos {

            ResponseEntity<EventoDTO> resposta;
            Evento eventoSalvo;

            @BeforeEach
            void setup() {
                eventoSalvo = new Evento();
                eventoSalvo.setId(UUID.randomUUID());
                eventoSalvo.setTitulo("Reunião");
                eventoSalvo.setDescricao("Reunião importante");
                eventoSalvo.setDataInicio(dataInicio);
                eventoSalvo.setDataFim(dataFim);
                eventoSalvo.setDiaInteiro(false);
                eventoSalvo.setRecorrente(false);
                eventoSalvo.setCalendario(calendario);

                when(eventoService.criar(any(Evento.class), eq(calendarioId))).thenReturn(eventoSalvo);
                resposta = eventoController.criar(eventoParaCriar, calendarioId);
            }

            @Test
            void deve_retornar_status_created() {
                assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_evento_criado() {
                assertNotNull(resposta.getBody());
                assertEquals("Reunião", resposta.getBody().getTitulo());
                assertEquals(calendarioId, resposta.getBody().getCalendarioId());
            }

            @Test
            void deve_chamar_service_para_criar() {
                verify(eventoService, times(1)).criar(any(Evento.class), eq(calendarioId));
            }
        }

        @Nested
        class Quando_calendario_nao_existe {

            ResponseEntity<EventoDTO> resposta;

            @BeforeEach
            void setup() {
                when(eventoService.criar(any(Evento.class), eq(calendarioId)))
                        .thenThrow(new IllegalArgumentException("Calendário não encontrado"));
                resposta = eventoController.criar(eventoParaCriar, calendarioId);
            }

            @Test
            void deve_retornar_status_bad_request() {
                assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
            }
        }

        @Nested
        class Quando_datas_invalidas {

            ResponseEntity<EventoDTO> resposta;

            @BeforeEach
            void setup() {
                when(eventoService.criar(any(Evento.class), eq(calendarioId)))
                        .thenThrow(new IllegalArgumentException("Data de fim deve ser posterior à data de início"));
                resposta = eventoController.criar(eventoParaCriar, calendarioId);
            }

            @Test
            void deve_retornar_status_bad_request() {
                assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_buscar_evento_por_id {

        UUID eventoId;
        Evento evento;
        Calendario calendario;

        @BeforeEach
        void setup() {
            eventoId = UUID.randomUUID();
            UUID calendarioId = UUID.randomUUID();

            calendario = new Calendario();
            calendario.setId(calendarioId);
            calendario.setNome("Trabalho");

            evento = new Evento();
            evento.setId(eventoId);
            evento.setTitulo("Reunião");
            evento.setDataInicio(LocalDateTime.now());
            evento.setDataFim(LocalDateTime.now().plusHours(2));
            evento.setDiaInteiro(false);
            evento.setRecorrente(false);
            evento.setCalendario(calendario);
        }

        @Nested
        class Quando_evento_existe {

            ResponseEntity<EventoDTO> resposta;

            @BeforeEach
            void setup() {
                when(eventoService.buscarPorId(eventoId)).thenReturn(Optional.of(evento));
                resposta = eventoController.buscarPorId(eventoId);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_evento_encontrado() {
                assertNotNull(resposta.getBody());
                assertEquals("Reunião", resposta.getBody().getTitulo());
            }
        }

        @Nested
        class Quando_evento_nao_existe {

            ResponseEntity<EventoDTO> resposta;

            @BeforeEach
            void setup() {
                when(eventoService.buscarPorId(eventoId)).thenReturn(Optional.empty());
                resposta = eventoController.buscarPorId(eventoId);
            }

            @Test
            void deve_retornar_status_not_found() {
                assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_listar_eventos {

        Evento evento1;
        Evento evento2;
        Calendario calendario;

        @BeforeEach
        void setup() {
            UUID calendarioId = UUID.randomUUID();

            calendario = new Calendario();
            calendario.setId(calendarioId);
            calendario.setNome("Trabalho");

            evento1 = new Evento();
            evento1.setId(UUID.randomUUID());
            evento1.setTitulo("Reunião 1");
            evento1.setDataInicio(LocalDateTime.now());
            evento1.setDataFim(LocalDateTime.now().plusHours(1));
            evento1.setDiaInteiro(false);
            evento1.setRecorrente(false);
            evento1.setCalendario(calendario);

            evento2 = new Evento();
            evento2.setId(UUID.randomUUID());
            evento2.setTitulo("Reunião 2");
            evento2.setDataInicio(LocalDateTime.now());
            evento2.setDataFim(LocalDateTime.now().plusHours(2));
            evento2.setDiaInteiro(false);
            evento2.setRecorrente(false);
            evento2.setCalendario(calendario);
        }

        @Nested
        class Quando_listar_todos_eventos {

            ResponseEntity<List<EventoDTO>> resposta;

            @BeforeEach
            void setup() {
                when(eventoService.listarTodos()).thenReturn(List.of(evento1, evento2));
                resposta = eventoController.listarTodos();
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_todos_eventos() {
                assertNotNull(resposta.getBody());
                assertEquals(2, resposta.getBody().size());
            }
        }

        @Nested
        class Quando_nao_existem_eventos {

            ResponseEntity<List<EventoDTO>> resposta;

            @BeforeEach
            void setup() {
                when(eventoService.listarTodos()).thenReturn(List.of());
                resposta = eventoController.listarTodos();
            }

            @Test
            void deve_retornar_lista_vazia() {
                assertNotNull(resposta.getBody());
                assertTrue(resposta.getBody().isEmpty());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_listar_eventos_por_calendario {

        UUID calendarioId;
        Evento evento;
        Calendario calendario;

        @BeforeEach
        void setup() {
            calendarioId = UUID.randomUUID();

            calendario = new Calendario();
            calendario.setId(calendarioId);
            calendario.setNome("Trabalho");

            evento = new Evento();
            evento.setId(UUID.randomUUID());
            evento.setTitulo("Reunião");
            evento.setDataInicio(LocalDateTime.now());
            evento.setDataFim(LocalDateTime.now().plusHours(2));
            evento.setDiaInteiro(false);
            evento.setRecorrente(false);
            evento.setCalendario(calendario);
        }

        @Nested
        class Quando_calendario_possui_eventos {

            ResponseEntity<List<EventoDTO>> resposta;

            @BeforeEach
            void setup() {
                when(eventoService.listarPorCalendario(calendarioId)).thenReturn(List.of(evento));
                resposta = eventoController.listarPorCalendario(calendarioId);
            }

            @Test
            void deve_retornar_eventos_do_calendario() {
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().size());
                assertEquals(calendarioId, resposta.getBody().get(0).getCalendarioId());
            }
        }

        @Nested
        class Quando_listar_paginado {

            ResponseEntity<Page<EventoDTO>> resposta;
            Pageable pageable;

            @BeforeEach
            void setup() {
                pageable = PageRequest.of(0, 10);
                Page<Evento> page = new PageImpl<>(List.of(evento));
                when(eventoService.listarPorCalendarioPaginado(calendarioId, pageable)).thenReturn(page);
                resposta = eventoController.listarPorCalendarioPaginado(calendarioId, pageable);
            }

            @Test
            void deve_retornar_page_de_eventos() {
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().getTotalElements());
            }
        }

        @Nested
        class Quando_calendario_nao_existe_para_paginacao {

            ResponseEntity<Page<EventoDTO>> resposta;
            Pageable pageable;

            @BeforeEach
            void setup() {
                pageable = PageRequest.of(0, 10);
                when(eventoService.listarPorCalendarioPaginado(calendarioId, pageable))
                        .thenThrow(new IllegalArgumentException("Calendário não encontrado"));
                resposta = eventoController.listarPorCalendarioPaginado(calendarioId, pageable);
            }

            @Test
            void deve_retornar_status_bad_request() {
                assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_buscar_eventos_por_periodo {

        LocalDateTime dataInicio;
        LocalDateTime dataFim;
        Evento evento;
        Calendario calendario;

        @BeforeEach
        void setup() {
            dataInicio = LocalDateTime.of(2025, 1, 1, 0, 0);
            dataFim = LocalDateTime.of(2025, 1, 31, 23, 59);

            UUID calendarioId = UUID.randomUUID();
            calendario = new Calendario();
            calendario.setId(calendarioId);
            calendario.setNome("Trabalho");

            evento = new Evento();
            evento.setId(UUID.randomUUID());
            evento.setTitulo("Reunião");
            evento.setDataInicio(LocalDateTime.of(2025, 1, 15, 10, 0));
            evento.setDataFim(LocalDateTime.of(2025, 1, 15, 12, 0));
            evento.setDiaInteiro(false);
            evento.setRecorrente(false);
            evento.setCalendario(calendario);
        }

        @Nested
        class Quando_existem_eventos_no_periodo {

            ResponseEntity<List<EventoDTO>> resposta;

            @BeforeEach
            void setup() {
                when(eventoService.buscarPorPeriodo(dataInicio, dataFim)).thenReturn(List.of(evento));
                resposta = eventoController.buscarPorPeriodo(dataInicio, dataFim);
            }

            @Test
            void deve_retornar_eventos_encontrados() {
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().size());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_buscar_por_calendario_e_periodo {

        UUID calendarioId;
        LocalDateTime dataInicio;
        LocalDateTime dataFim;
        Evento evento;
        Calendario calendario;

        @BeforeEach
        void setup() {
            calendarioId = UUID.randomUUID();
            dataInicio = LocalDateTime.of(2025, 1, 1, 0, 0);
            dataFim = LocalDateTime.of(2025, 1, 31, 23, 59);

            calendario = new Calendario();
            calendario.setId(calendarioId);
            calendario.setNome("Trabalho");

            evento = new Evento();
            evento.setId(UUID.randomUUID());
            evento.setTitulo("Reunião");
            evento.setDataInicio(LocalDateTime.of(2025, 1, 15, 10, 0));
            evento.setDataFim(LocalDateTime.of(2025, 1, 15, 12, 0));
            evento.setDiaInteiro(false);
            evento.setRecorrente(false);
            evento.setCalendario(calendario);
        }

        @Nested
        class Quando_existem_eventos_no_periodo_para_calendario {

            ResponseEntity<List<EventoDTO>> resposta;

            @BeforeEach
            void setup() {
                when(eventoService.buscarPorCalendarioEPeriodo(calendarioId, dataInicio, dataFim))
                        .thenReturn(List.of(evento));
                resposta = eventoController.buscarPorCalendarioEPeriodo(calendarioId, dataInicio, dataFim);
            }

            @Test
            void deve_retornar_eventos_do_calendario_no_periodo() {
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().size());
                assertEquals(calendarioId, resposta.getBody().get(0).getCalendarioId());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_buscar_por_titulo {

        Evento evento;
        Calendario calendario;
        Pageable pageable;

        @BeforeEach
        void setup() {
            pageable = PageRequest.of(0, 10);

            UUID calendarioId = UUID.randomUUID();
            calendario = new Calendario();
            calendario.setId(calendarioId);
            calendario.setNome("Trabalho");

            evento = new Evento();
            evento.setId(UUID.randomUUID());
            evento.setTitulo("Reunião Importante");
            evento.setDataInicio(LocalDateTime.now());
            evento.setDataFim(LocalDateTime.now().plusHours(2));
            evento.setDiaInteiro(false);
            evento.setRecorrente(false);
            evento.setCalendario(calendario);
        }

        @Nested
        class Quando_eventos_encontrados {

            ResponseEntity<Page<EventoDTO>> resposta;

            @BeforeEach
            void setup() {
                Page<Evento> page = new PageImpl<>(List.of(evento));
                when(eventoService.buscarPorTitulo("Reunião", pageable)).thenReturn(page);
                resposta = eventoController.buscarPorTitulo("Reunião", pageable);
            }

            @Test
            void deve_retornar_eventos_com_titulo() {
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().getTotalElements());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_listar_eventos_recorrentes {

        Evento eventoRecorrente;
        Calendario calendario;

        @BeforeEach
        void setup() {
            UUID calendarioId = UUID.randomUUID();
            calendario = new Calendario();
            calendario.setId(calendarioId);
            calendario.setNome("Trabalho");

            eventoRecorrente = new Evento();
            eventoRecorrente.setId(UUID.randomUUID());
            eventoRecorrente.setTitulo("Reunião Semanal");
            eventoRecorrente.setDataInicio(LocalDateTime.now());
            eventoRecorrente.setDataFim(LocalDateTime.now().plusHours(1));
            eventoRecorrente.setDiaInteiro(false);
            eventoRecorrente.setRecorrente(true);
            eventoRecorrente.setTipoRecorrencia(TipoRecorrencia.SEMANAL);
            eventoRecorrente.setCalendario(calendario);
        }

        @Nested
        class Quando_existem_eventos_recorrentes {

            ResponseEntity<List<EventoDTO>> resposta;

            @BeforeEach
            void setup() {
                when(eventoService.listarRecorrentes()).thenReturn(List.of(eventoRecorrente));
                resposta = eventoController.listarRecorrentes();
            }

            @Test
            void deve_retornar_apenas_eventos_recorrentes() {
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().size());
                assertTrue(resposta.getBody().get(0).getRecorrente());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_atualizar_evento {

        UUID eventoId;
        Evento eventoAtualizado;
        Calendario calendario;

        @BeforeEach
        void setup() {
            eventoId = UUID.randomUUID();
            UUID calendarioId = UUID.randomUUID();

            calendario = new Calendario();
            calendario.setId(calendarioId);
            calendario.setNome("Trabalho");

            eventoAtualizado = new Evento();
            eventoAtualizado.setId(eventoId);
            eventoAtualizado.setTitulo("Reunião Atualizada");
            eventoAtualizado.setDataInicio(LocalDateTime.now());
            eventoAtualizado.setDataFim(LocalDateTime.now().plusHours(2));
            eventoAtualizado.setDiaInteiro(false);
            eventoAtualizado.setRecorrente(false);
            eventoAtualizado.setCalendario(calendario);
        }

        @Nested
        class Quando_evento_existe {

            ResponseEntity<EventoDTO> resposta;

            @BeforeEach
            void setup() {
                when(eventoService.atualizar(eq(eventoId), any(Evento.class))).thenReturn(eventoAtualizado);
                resposta = eventoController.atualizar(eventoId, eventoAtualizado);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_evento_atualizado() {
                assertNotNull(resposta.getBody());
                assertEquals("Reunião Atualizada", resposta.getBody().getTitulo());
            }
        }

        @Nested
        class Quando_evento_nao_existe {

            ResponseEntity<EventoDTO> resposta;

            @BeforeEach
            void setup() {
                when(eventoService.atualizar(eq(eventoId), any(Evento.class)))
                        .thenThrow(new IllegalArgumentException("Evento não encontrado"));
                resposta = eventoController.atualizar(eventoId, eventoAtualizado);
            }

            @Test
            void deve_retornar_status_bad_request() {
                assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_deletar_evento {

        UUID eventoId;

        @BeforeEach
        void setup() {
            eventoId = UUID.randomUUID();
        }

        @Nested
        class Quando_evento_existe {

            ResponseEntity<Void> resposta;

            @BeforeEach
            void setup() {
                doNothing().when(eventoService).deletar(eventoId);
                resposta = eventoController.deletar(eventoId);
            }

            @Test
            void deve_retornar_status_no_content() {
                assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
            }

            @Test
            void deve_chamar_service_para_deletar() {
                verify(eventoService, times(1)).deletar(eventoId);
            }
        }

        @Nested
        class Quando_evento_nao_existe {

            ResponseEntity<Void> resposta;

            @BeforeEach
            void setup() {
                doThrow(new IllegalArgumentException("Evento não encontrado"))
                        .when(eventoService).deletar(eventoId);
                resposta = eventoController.deletar(eventoId);
            }

            @Test
            void deve_retornar_status_not_found() {
                assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_contar_eventos_por_calendario {

        UUID calendarioId;

        @BeforeEach
        void setup() {
            calendarioId = UUID.randomUUID();
        }

        @Nested
        class Quando_calendario_possui_eventos {

            ResponseEntity<Long> resposta;

            @BeforeEach
            void setup() {
                when(eventoService.contarPorCalendario(calendarioId)).thenReturn(5L);
                resposta = eventoController.contarPorCalendario(calendarioId);
            }

            @Test
            void deve_retornar_quantidade_de_eventos() {
                assertNotNull(resposta.getBody());
                assertEquals(5L, resposta.getBody());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_verificar_pertencimento {

        UUID eventoId;
        UUID calendarioId;

        @BeforeEach
        void setup() {
            eventoId = UUID.randomUUID();
            calendarioId = UUID.randomUUID();
        }

        @Nested
        class Quando_evento_pertence_ao_calendario {

            ResponseEntity<Boolean> resposta;

            @BeforeEach
            void setup() {
                when(eventoService.pertenceAoCalendario(eventoId, calendarioId)).thenReturn(true);
                resposta = eventoController.pertenceAoCalendario(eventoId, calendarioId);
            }

            @Test
            void deve_retornar_true() {
                assertNotNull(resposta.getBody());
                assertTrue(resposta.getBody());
            }
        }

        @Nested
        class Quando_evento_nao_pertence_ao_calendario {

            ResponseEntity<Boolean> resposta;

            @BeforeEach
            void setup() {
                when(eventoService.pertenceAoCalendario(eventoId, calendarioId)).thenReturn(false);
                resposta = eventoController.pertenceAoCalendario(eventoId, calendarioId);
            }

            @Test
            void deve_retornar_false() {
                assertNotNull(resposta.getBody());
                assertFalse(resposta.getBody());
            }
        }

        @Nested
        class Quando_evento_nao_existe {

            ResponseEntity<Boolean> resposta;

            @BeforeEach
            void setup() {
                when(eventoService.pertenceAoCalendario(eventoId, calendarioId))
                        .thenThrow(new IllegalArgumentException("Evento não encontrado"));
                resposta = eventoController.pertenceAoCalendario(eventoId, calendarioId);
            }

            @Test
            void deve_retornar_status_bad_request() {
                assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
            }
        }
    }
}