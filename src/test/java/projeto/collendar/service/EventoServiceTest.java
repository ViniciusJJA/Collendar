package projeto.collendar.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import projeto.collendar.model.Calendario;
import projeto.collendar.model.Evento;
import projeto.collendar.repository.CalendarioRepository;
import projeto.collendar.repository.EventoRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private CalendarioRepository calendarioRepository;

    @InjectMocks
    private EventoService eventoService;

    @Nested
    class Dado_um_evento_valido {

        Evento evento;
        Calendario calendario;
        UUID eventoId;
        UUID calendarioId;
        LocalDateTime dataInicio;
        LocalDateTime dataFim;

        @BeforeEach
        void setup() {
            eventoId = UUID.randomUUID();
            calendarioId = UUID.randomUUID();
            dataInicio = LocalDateTime.now();
            dataFim = dataInicio.plusHours(2);

            calendario = new Calendario();
            calendario.setId(calendarioId);
            calendario.setNome("Trabalho");

            evento = new Evento();
            evento.setId(eventoId);
            evento.setTitulo("Reunião");
            evento.setDescricao("Reunião de equipe");
            evento.setDataInicio(dataInicio);
            evento.setDataFim(dataFim);
            evento.setCalendario(calendario);
            evento.setDiaInteiro(false);
            evento.setRecorrente(false);
        }

        @Nested
        class Quando_criar_evento {

            @BeforeEach
            void setup() {
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
                when(eventoRepository.save(any(Evento.class))).thenReturn(evento);
            }

            @Test
            void deve_criar_evento_com_sucesso() {
                Evento resultado = eventoService.criar(evento, calendarioId);

                assertNotNull(resultado);
                assertEquals("Reunião", resultado.getTitulo());
                assertEquals(calendario, resultado.getCalendario());
                verify(eventoRepository).save(any(Evento.class));
            }
        }

        @Nested
        class Quando_criar_evento_com_calendario_inexistente {

            @BeforeEach
            void setup() {
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.empty());
            }

            @Test
            void deve_lancar_excecao() {
                IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> eventoService.criar(evento, calendarioId)
                );

                assertEquals("Calendário não encontrado", exception.getMessage());
            }
        }

        @Nested
        class Quando_criar_evento_com_datas_invalidas {

            @BeforeEach
            void setup() {
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
            }

            @Test
            void deve_lancar_excecao_quando_data_fim_anterior() {
                evento.setDataFim(dataInicio.minusHours(1));

                IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> eventoService.criar(evento, calendarioId)
                );

                assertEquals("Data de fim deve ser posterior à data de início", exception.getMessage());
            }

            @Test
            void deve_lancar_excecao_quando_datas_nulas() {
                evento.setDataInicio(null);
                evento.setDataFim(null);

                IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> eventoService.criar(evento, calendarioId)
                );

                assertEquals("Datas de início e fim são obrigatórias", exception.getMessage());
            }
        }

        @Nested
        class Quando_buscar_evento_por_id {

            @Test
            void deve_retornar_evento_quando_existir() {
                when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));

                Optional<Evento> resultado = eventoService.buscarPorId(eventoId);

                assertTrue(resultado.isPresent());
                assertEquals("Reunião", resultado.get().getTitulo());
            }

            @Test
            void deve_retornar_vazio_quando_nao_existir() {
                when(eventoRepository.findById(eventoId)).thenReturn(Optional.empty());

                Optional<Evento> resultado = eventoService.buscarPorId(eventoId);

                assertFalse(resultado.isPresent());
            }
        }

        @Nested
        class Quando_listar_eventos {

            @Test
            void deve_listar_todos_eventos() {
                List<Evento> eventos = Arrays.asList(evento, new Evento());
                when(eventoRepository.findAll()).thenReturn(eventos);

                List<Evento> resultado = eventoService.listarTodos();

                assertEquals(2, resultado.size());
            }

            @Test
            void deve_listar_eventos_por_calendario() {
                List<Evento> eventos = Arrays.asList(evento);
                when(eventoRepository.findByCalendarioId(calendarioId)).thenReturn(eventos);

                List<Evento> resultado = eventoService.listarPorCalendario(calendarioId);

                assertEquals(1, resultado.size());
                assertEquals("Reunião", resultado.get(0).getTitulo());
            }

            @Test
            void deve_listar_eventos_por_calendario_paginado() {
                Pageable pageable = PageRequest.of(0, 10);
                Page<Evento> page = new PageImpl<>(Arrays.asList(evento));
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
                when(eventoRepository.findByCalendario(calendario, pageable)).thenReturn(page);

                Page<Evento> resultado = eventoService.listarPorCalendarioPaginado(calendarioId, pageable);

                assertEquals(1, resultado.getTotalElements());
            }

            @Test
            void deve_listar_eventos_recorrentes() {
                evento.setRecorrente(true);
                List<Evento> eventos = Arrays.asList(evento);
                when(eventoRepository.findByRecorrente(true)).thenReturn(eventos);

                List<Evento> resultado = eventoService.listarRecorrentes();

                assertEquals(1, resultado.size());
                assertTrue(resultado.get(0).getRecorrente());
            }
        }

        @Nested
        class Quando_buscar_eventos_por_periodo {

            @BeforeEach
            void setup() {
                List<Evento> eventos = Arrays.asList(evento);
                when(eventoRepository.findByDataInicioBetween(dataInicio, dataFim)).thenReturn(eventos);
            }

            @Test
            void deve_retornar_eventos_no_periodo() {
                List<Evento> resultado = eventoService.buscarPorPeriodo(dataInicio, dataFim);

                assertEquals(1, resultado.size());
            }
        }

        @Nested
        class Quando_buscar_eventos_por_calendario_e_periodo {

            @BeforeEach
            void setup() {
                List<Evento> eventos = Arrays.asList(evento);
                when(eventoRepository.findByCalendarioAndDataBetween(calendarioId, dataInicio, dataFim))
                        .thenReturn(eventos);
            }

            @Test
            void deve_retornar_eventos_encontrados() {
                List<Evento> resultado = eventoService.buscarPorCalendarioEPeriodo(calendarioId, dataInicio, dataFim);

                assertEquals(1, resultado.size());
            }
        }

        @Nested
        class Quando_buscar_eventos_por_titulo {

            @BeforeEach
            void setup() {
                Pageable pageable = PageRequest.of(0, 10);
                Page<Evento> page = new PageImpl<>(Arrays.asList(evento));
                when(eventoRepository.findByTituloContainingIgnoreCase("Reunião", pageable)).thenReturn(page);
            }

            @Test
            void deve_retornar_eventos_encontrados() {
                Pageable pageable = PageRequest.of(0, 10);
                Page<Evento> resultado = eventoService.buscarPorTitulo("Reunião", pageable);

                assertEquals(1, resultado.getTotalElements());
            }
        }

        @Nested
        class Quando_atualizar_evento {

            Evento eventoAtualizado;

            @BeforeEach
            void setup() {
                eventoAtualizado = new Evento();
                eventoAtualizado.setTitulo("Reunião Updated");
                eventoAtualizado.setDataInicio(dataInicio);
                eventoAtualizado.setDataFim(dataFim);

                when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));
                when(eventoRepository.save(any(Evento.class))).thenReturn(evento);
            }

            @Test
            void deve_atualizar_evento_com_sucesso() {
                Evento resultado = eventoService.atualizar(eventoId, eventoAtualizado);

                assertNotNull(resultado);
                verify(eventoRepository).save(any(Evento.class));
            }
        }

        @Nested
        class Quando_atualizar_evento_inexistente {

            @BeforeEach
            void setup() {
                when(eventoRepository.findById(eventoId)).thenReturn(Optional.empty());
            }

            @Test
            void deve_lancar_excecao() {
                IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> eventoService.atualizar(eventoId, evento)
                );

                assertEquals("Evento não encontrado", exception.getMessage());
            }
        }

        @Nested
        class Quando_deletar_evento {

            @Test
            void deve_deletar_evento_existente() {
                when(eventoRepository.existsById(eventoId)).thenReturn(true);
                doNothing().when(eventoRepository).deleteById(eventoId);

                eventoService.deletar(eventoId);

                verify(eventoRepository).existsById(eventoId);
                verify(eventoRepository).deleteById(eventoId);
            }

            @Test
            void deve_lancar_excecao_quando_evento_nao_existe() {
                when(eventoRepository.existsById(eventoId)).thenReturn(false);

                IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> eventoService.deletar(eventoId)
                );

                assertEquals("Evento não encontrado", exception.getMessage());
            }
        }

        @Nested
        class Quando_contar_eventos_por_calendario {

            @BeforeEach
            void setup() {
                List<Evento> eventos = Arrays.asList(evento, new Evento(), new Evento());
                when(eventoRepository.findByCalendarioId(calendarioId)).thenReturn(eventos);
            }

            @Test
            void deve_retornar_quantidade_correta() {
                long resultado = eventoService.contarPorCalendario(calendarioId);

                assertEquals(3, resultado);
            }
        }

        @Nested
        class Quando_verificar_pertencimento_ao_calendario {

            @BeforeEach
            void setup() {
                when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));
            }

            @Test
            void deve_retornar_true_quando_pertence() {
                boolean resultado = eventoService.pertenceAoCalendario(eventoId, calendarioId);

                assertTrue(resultado);
            }

            @Test
            void deve_retornar_false_quando_nao_pertence() {
                UUID outroCalendarioId = UUID.randomUUID();

                boolean resultado = eventoService.pertenceAoCalendario(eventoId, outroCalendarioId);

                assertFalse(resultado);
            }
        }
    }
}