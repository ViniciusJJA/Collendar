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
import projeto.collendar.dtos.request.EventoRequestDTO;
import projeto.collendar.dtos.response.EventoResponseDTO;
import projeto.collendar.enums.TipoRecorrencia;
import projeto.collendar.exception.BusinessException;
import projeto.collendar.exception.ResourceNotFoundException;
import projeto.collendar.model.Calendario;
import projeto.collendar.model.Evento;
import projeto.collendar.model.Usuario;
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
    private CalendarioService calendarioService;

    @InjectMocks
    private EventoService eventoService;

    @Nested
    class Dado_um_evento_valido_para_criar {

        EventoRequestDTO dto;
        Calendario calendario;
        UUID calendarioId;
        LocalDateTime dataInicio;
        LocalDateTime dataFim;

        @BeforeEach
        void setup() {
            calendarioId = UUID.randomUUID();
            dataInicio = LocalDateTime.of(2025, 1, 15, 10, 0);
            dataFim = LocalDateTime.of(2025, 1, 15, 12, 0);

            Usuario usuario = new Usuario();
            usuario.setId(UUID.randomUUID());
            usuario.setNome("João Silva");

            calendario = new Calendario();
            calendario.setId(calendarioId);
            calendario.setNome("Trabalho");
            calendario.setUsuario(usuario);

            dto = new EventoRequestDTO(
                    "Reunião",
                    "Reunião importante",
                    dataInicio,
                    dataFim,
                    "Sala 1",
                    "#FF5733",
                    false,
                    false,
                    null,
                    calendarioId
            );
        }

        @Nested
        class Quando_criar_evento {

            EventoResponseDTO resultado;

            @BeforeEach
            void setup() {
                when(calendarioService.findEntityById(calendarioId)).thenReturn(calendario);
                when(eventoRepository.save(any(Evento.class))).thenAnswer(invocation -> {
                    Evento evento = invocation.getArgument(0);
                    evento.setId(UUID.randomUUID());
                    return evento;
                });

                resultado = eventoService.create(dto);
            }

            @Test
            void deve_criar_evento_com_sucesso() {
                assertNotNull(resultado);
                assertEquals("Reunião", resultado.titulo());
                assertEquals("Reunião importante", resultado.descricao());
            }

            @Test
            void deve_associar_calendario_ao_evento() {
                assertEquals(calendarioId, resultado.calendarioId());
                assertEquals("Trabalho", resultado.calendarioNome());
            }

            @Test
            void deve_configurar_datas_corretamente() {
                assertEquals(dataInicio, resultado.dataInicio());
                assertEquals(dataFim, resultado.dataFim());
            }

            @Test
            void deve_salvar_evento_no_repositorio() {
                verify(eventoRepository).save(any(Evento.class));
            }
        }

        @Nested
        class Quando_criar_evento_com_calendario_inexistente {

            @BeforeEach
            void setup() {
                when(calendarioService.findEntityById(calendarioId))
                        .thenThrow(new ResourceNotFoundException("Calendário", calendarioId.toString()));
            }

            @Test
            void deve_lancar_resource_not_found_exception() {
                ResourceNotFoundException exception = assertThrows(
                        ResourceNotFoundException.class,
                        () -> eventoService.create(dto)
                );

                assertTrue(exception.getMessage().contains("Calendário não encontrado"));
            }

            @Test
            void nao_deve_salvar_evento() {
                assertThrows(ResourceNotFoundException.class, () -> eventoService.create(dto));
                verify(eventoRepository, never()).save(any());
            }
        }

        @Nested
        class Quando_criar_evento_com_data_fim_anterior_a_inicio {

            EventoRequestDTO dtoInvalido;

            @BeforeEach
            void setup() {
                dtoInvalido = new EventoRequestDTO(
                        "Reunião",
                        "Reunião importante",
                        dataFim,
                        dataInicio,
                        "Sala 1",
                        "#FF5733",
                        false,
                        false,
                        null,
                        calendarioId
                );
            }

            @Test
            void deve_lancar_business_exception() {
                BusinessException exception = assertThrows(
                        BusinessException.class,
                        () -> eventoService.create(dtoInvalido)
                );

                assertEquals("Data de fim deve ser posterior à data de início", exception.getMessage());
            }
        }

        @Nested
        class Quando_criar_evento_com_datas_nulas {

            EventoRequestDTO dtoInvalido;

            @BeforeEach
            void setup() {
                dtoInvalido = new EventoRequestDTO(
                        "Reunião",
                        "Reunião importante",
                        null,
                        null,
                        "Sala 1",
                        "#FF5733",
                        false,
                        false,
                        null,
                        calendarioId
                );
            }

            @Test
            void deve_lancar_business_exception() {
                BusinessException exception = assertThrows(
                        BusinessException.class,
                        () -> eventoService.create(dtoInvalido)
                );

                assertEquals("Datas de início e fim são obrigatórias", exception.getMessage());
            }
        }
    }

    @Nested
    class Dado_um_evento_existente {

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
            dataInicio = LocalDateTime.of(2025, 1, 15, 10, 0);
            dataFim = LocalDateTime.of(2025, 1, 15, 12, 0);

            Usuario usuario = new Usuario();
            usuario.setId(UUID.randomUUID());

            calendario = new Calendario();
            calendario.setId(calendarioId);
            calendario.setNome("Trabalho");
            calendario.setUsuario(usuario);

            evento = new Evento();
            evento.setId(eventoId);
            evento.setTitulo("Reunião");
            evento.setDescricao("Reunião importante");
            evento.setDataInicio(dataInicio);
            evento.setDataFim(dataFim);
            evento.setLocal("Sala 1");
            evento.setCor("#FF5733");
            evento.setDiaInteiro(false);
            evento.setRecorrente(false);
            evento.setCalendario(calendario);
        }

        @Nested
        class Quando_buscar_por_id {

            EventoResponseDTO resultado;

            @BeforeEach
            void setup() {
                when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));
                resultado = eventoService.findById(eventoId);
            }

            @Test
            void deve_retornar_evento_encontrado() {
                assertNotNull(resultado);
                assertEquals("Reunião", resultado.titulo());
                assertEquals(eventoId, resultado.id());
            }
        }

        @Nested
        class Quando_buscar_por_id_inexistente {

            @BeforeEach
            void setup() {
                when(eventoRepository.findById(eventoId)).thenReturn(Optional.empty());
            }

            @Test
            void deve_lancar_resource_not_found_exception() {
                ResourceNotFoundException exception = assertThrows(
                        ResourceNotFoundException.class,
                        () -> eventoService.findById(eventoId)
                );

                assertTrue(exception.getMessage().contains("Evento não encontrado"));
            }
        }

        @Nested
        class Quando_listar_todos_eventos {

            List<EventoResponseDTO> resultado;

            @BeforeEach
            void setup() {
                Evento evento2 = new Evento();
                evento2.setId(UUID.randomUUID());
                evento2.setTitulo("Apresentação");
                evento2.setCalendario(calendario);
                evento2.setDataInicio(dataInicio);
                evento2.setDataFim(dataFim);

                when(eventoRepository.findAll()).thenReturn(Arrays.asList(evento, evento2));
                resultado = eventoService.listAll();
            }

            @Test
            void deve_retornar_todos_eventos() {
                assertNotNull(resultado);
                assertEquals(2, resultado.size());
            }
        }

        @Nested
        class Quando_listar_por_calendario {

            List<EventoResponseDTO> resultado;

            @BeforeEach
            void setup() {
                when(eventoRepository.findByCalendarioId(calendarioId))
                        .thenReturn(Arrays.asList(evento));
                resultado = eventoService.listByCalendario(calendarioId);
            }

            @Test
            void deve_retornar_eventos_do_calendario() {
                assertNotNull(resultado);
                assertEquals(1, resultado.size());
                assertEquals(calendarioId, resultado.get(0).calendarioId());
            }
        }

        @Nested
        class Quando_listar_por_calendario_paginado {

            Page<EventoResponseDTO> resultado;
            Pageable pageable;

            @BeforeEach
            void setup() {
                pageable = PageRequest.of(0, 10);
                Page<Evento> page = new PageImpl<>(Arrays.asList(evento));

                when(calendarioService.findEntityById(calendarioId)).thenReturn(calendario);
                when(eventoRepository.findByCalendario(calendario, pageable)).thenReturn(page);

                resultado = eventoService.listByCalendarioPaginated(calendarioId, pageable);
            }

            @Test
            void deve_retornar_pagina_de_eventos() {
                assertNotNull(resultado);
                assertEquals(1, resultado.getTotalElements());
            }
        }

        @Nested
        class Quando_buscar_por_periodo {

            List<EventoResponseDTO> resultado;
            LocalDateTime inicio;
            LocalDateTime fim;

            @BeforeEach
            void setup() {
                inicio = LocalDateTime.of(2025, 1, 1, 0, 0);
                fim = LocalDateTime.of(2025, 1, 31, 23, 59);

                when(eventoRepository.findByDataInicioBetween(inicio, fim))
                        .thenReturn(Arrays.asList(evento));

                resultado = eventoService.findByPeriod(inicio, fim);
            }

            @Test
            void deve_retornar_eventos_no_periodo() {
                assertNotNull(resultado);
                assertEquals(1, resultado.size());
            }
        }

        @Nested
        class Quando_buscar_por_calendario_e_periodo {

            List<EventoResponseDTO> resultado;
            LocalDateTime inicio;
            LocalDateTime fim;

            @BeforeEach
            void setup() {
                inicio = LocalDateTime.of(2025, 1, 1, 0, 0);
                fim = LocalDateTime.of(2025, 1, 31, 23, 59);

                when(eventoRepository.findByCalendarioAndDataBetween(calendarioId, inicio, fim))
                        .thenReturn(Arrays.asList(evento));

                resultado = eventoService.findByCalendarioAndPeriod(calendarioId, inicio, fim);
            }

            @Test
            void deve_retornar_eventos_encontrados() {
                assertNotNull(resultado);
                assertEquals(1, resultado.size());
                assertEquals(calendarioId, resultado.get(0).calendarioId());
            }
        }

        @Nested
        class Quando_buscar_por_titulo {

            Page<EventoResponseDTO> resultado;
            Pageable pageable;

            @BeforeEach
            void setup() {
                pageable = PageRequest.of(0, 10);
                Page<Evento> page = new PageImpl<>(Arrays.asList(evento));

                when(eventoRepository.findByTituloContainingIgnoreCase("Reunião", pageable))
                        .thenReturn(page);

                resultado = eventoService.searchByTitulo("Reunião", pageable);
            }

            @Test
            void deve_retornar_eventos_encontrados() {
                assertNotNull(resultado);
                assertEquals(1, resultado.getTotalElements());
                assertEquals("Reunião", resultado.getContent().get(0).titulo());
            }
        }

        @Nested
        class Quando_listar_eventos_recorrentes {

            List<EventoResponseDTO> resultado;

            @BeforeEach
            void setup() {
                evento.setRecorrente(true);
                evento.setTipoRecorrencia(TipoRecorrencia.SEMANAL);

                when(eventoRepository.findByRecorrente(true)).thenReturn(Arrays.asList(evento));
                resultado = eventoService.listRecorrentes();
            }

            @Test
            void deve_retornar_apenas_eventos_recorrentes() {
                assertNotNull(resultado);
                assertEquals(1, resultado.size());
                assertTrue(resultado.get(0).recorrente());
                assertEquals(TipoRecorrencia.SEMANAL, resultado.get(0).tipoRecorrencia());
            }
        }

        @Nested
        class Quando_atualizar_evento {

            EventoRequestDTO dtoAtualizado;
            EventoResponseDTO resultado;

            @BeforeEach
            void setup() {
                dtoAtualizado = new EventoRequestDTO(
                        "Reunião Atualizada",
                        "Nova descrição",
                        dataInicio,
                        dataFim,
                        "Sala 2",
                        "#00FF00",
                        false,
                        false,
                        null,
                        calendarioId
                );

                when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));
                when(eventoRepository.save(any(Evento.class))).thenReturn(evento);

                resultado = eventoService.update(eventoId, dtoAtualizado);
            }

            @Test
            void deve_atualizar_evento_com_sucesso() {
                assertNotNull(resultado);
                verify(eventoRepository).save(any(Evento.class));
            }
        }

        @Nested
        class Quando_atualizar_evento_inexistente {

            EventoRequestDTO dtoAtualizado;

            @BeforeEach
            void setup() {
                dtoAtualizado = new EventoRequestDTO(
                        "Reunião Atualizada",
                        "Nova descrição",
                        dataInicio,
                        dataFim,
                        "Sala 2",
                        "#00FF00",
                        false,
                        false,
                        null,
                        calendarioId
                );

                when(eventoRepository.findById(eventoId)).thenReturn(Optional.empty());
            }

            @Test
            void deve_lancar_resource_not_found_exception() {
                ResourceNotFoundException exception = assertThrows(
                        ResourceNotFoundException.class,
                        () -> eventoService.update(eventoId, dtoAtualizado)
                );

                assertTrue(exception.getMessage().contains("Evento não encontrado"));
            }
        }

        @Nested
        class Quando_deletar_evento {

            @BeforeEach
            void setup() {
                when(eventoRepository.existsById(eventoId)).thenReturn(true);
                doNothing().when(eventoRepository).deleteById(eventoId);

                eventoService.delete(eventoId);
            }

            @Test
            void deve_deletar_evento() {
                verify(eventoRepository).deleteById(eventoId);
            }
        }

        @Nested
        class Quando_deletar_evento_inexistente {

            @BeforeEach
            void setup() {
                when(eventoRepository.existsById(eventoId)).thenReturn(false);
            }

            @Test
            void deve_lancar_resource_not_found_exception() {
                ResourceNotFoundException exception = assertThrows(
                        ResourceNotFoundException.class,
                        () -> eventoService.delete(eventoId)
                );

                assertTrue(exception.getMessage().contains("Evento não encontrado"));
            }
        }

        @Nested
        class Quando_contar_eventos_por_calendario {

            long resultado;

            @BeforeEach
            void setup() {
                Evento evento2 = new Evento();
                evento2.setId(UUID.randomUUID());

                when(eventoRepository.findByCalendarioId(calendarioId))
                        .thenReturn(Arrays.asList(evento, evento2));

                resultado = eventoService.countByCalendario(calendarioId);
            }

            @Test
            void deve_retornar_quantidade_correta() {
                assertEquals(2, resultado);
            }
        }

        @Nested
        class Quando_obter_calendario_id_do_evento {

            UUID resultado;

            @BeforeEach
            void setup() {
                when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));
                resultado = eventoService.getCalendarioIdByEvento(eventoId);
            }

            @Test
            void deve_retornar_id_do_calendario() {
                assertEquals(calendarioId, resultado);
            }
        }
    }
}