package projeto.collendar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private CalendarioRepository calendarioRepository;

    @InjectMocks
    private EventoService eventoService;

    private Evento evento;
    private Calendario calendario;
    private UUID eventoId;
    private UUID calendarioId;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;

    @BeforeEach
    void setUp() {
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

    @Test
    void deveCriarEventoComSucesso() {
        when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
        when(eventoRepository.save(any(Evento.class))).thenReturn(evento);

        Evento resultado = eventoService.criar(evento, calendarioId);

        assertNotNull(resultado);
        assertEquals("Reunião", resultado.getTitulo());
        assertEquals(calendario, resultado.getCalendario());
        verify(calendarioRepository).findById(calendarioId);
        verify(eventoRepository).save(any(Evento.class));
    }

    @Test
    void deveLancarExcecaoAoCriarEventoComCalendarioInexistente() {
        when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> eventoService.criar(evento, calendarioId)
        );
        assertEquals("Calendário não encontrado", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoAoCriarEventoComDataFimAnteriorADataInicio() {
        evento.setDataFim(dataInicio.minusHours(1));
        when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> eventoService.criar(evento, calendarioId)
        );
        assertEquals("Data de fim deve ser posterior à data de início", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoAoCriarEventoComDatasNulas() {
        evento.setDataInicio(null);
        evento.setDataFim(null);
        when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> eventoService.criar(evento, calendarioId)
        );
        assertEquals("Datas de início e fim são obrigatórias", exception.getMessage());
    }

    @Test
    void deveBuscarEventoPorId() {
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));

        Optional<Evento> resultado = eventoService.buscarPorId(eventoId);

        assertTrue(resultado.isPresent());
        assertEquals("Reunião", resultado.get().getTitulo());
    }

    @Test
    void deveListarTodosEventos() {
        List<Evento> eventos = Arrays.asList(evento, new Evento());
        when(eventoRepository.findAll()).thenReturn(eventos);

        List<Evento> resultado = eventoService.listarTodos();

        assertEquals(2, resultado.size());
    }

    @Test
    void deveListarEventosPorCalendario() {
        List<Evento> eventos = Arrays.asList(evento);
        when(eventoRepository.findByCalendarioId(calendarioId)).thenReturn(eventos);

        List<Evento> resultado = eventoService.listarPorCalendario(calendarioId);

        assertEquals(1, resultado.size());
        assertEquals("Reunião", resultado.get(0).getTitulo());
    }

    @Test
    void deveListarEventosPorCalendarioPaginado() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Evento> page = new PageImpl<>(Arrays.asList(evento));
        when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
        when(eventoRepository.findByCalendario(calendario, pageable)).thenReturn(page);

        Page<Evento> resultado = eventoService.listarPorCalendarioPaginado(calendarioId, pageable);

        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    void deveBuscarEventosPorPeriodo() {
        List<Evento> eventos = Arrays.asList(evento);
        when(eventoRepository.findByDataInicioBetween(dataInicio, dataFim)).thenReturn(eventos);

        List<Evento> resultado = eventoService.buscarPorPeriodo(dataInicio, dataFim);

        assertEquals(1, resultado.size());
    }

    @Test
    void deveBuscarEventosPorCalendarioEPeriodo() {
        List<Evento> eventos = Arrays.asList(evento);
        when(eventoRepository.findByCalendarioAndDataBetween(calendarioId, dataInicio, dataFim))
                .thenReturn(eventos);

        List<Evento> resultado = eventoService.buscarPorCalendarioEPeriodo(calendarioId, dataInicio, dataFim);

        assertEquals(1, resultado.size());
    }

    @Test
    void deveBuscarEventosPorTitulo() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Evento> page = new PageImpl<>(Arrays.asList(evento));
        when(eventoRepository.findByTituloContainingIgnoreCase("Reunião", pageable)).thenReturn(page);

        Page<Evento> resultado = eventoService.buscarPorTitulo("Reunião", pageable);

        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    void deveListarEventosRecorrentes() {
        evento.setRecorrente(true);
        List<Evento> eventos = Arrays.asList(evento);
        when(eventoRepository.findByRecorrente(true)).thenReturn(eventos);

        List<Evento> resultado = eventoService.listarRecorrentes();

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getRecorrente());
    }

    @Test
    void deveAtualizarEvento() {
        Evento eventoAtualizado = new Evento();
        eventoAtualizado.setTitulo("Reunião Updated");
        eventoAtualizado.setDataInicio(dataInicio);
        eventoAtualizado.setDataFim(dataFim);

        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(any(Evento.class))).thenReturn(evento);

        Evento resultado = eventoService.atualizar(eventoId, eventoAtualizado);

        assertNotNull(resultado);
        verify(eventoRepository).save(any(Evento.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarEventoInexistente() {
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> eventoService.atualizar(eventoId, evento)
        );
        assertEquals("Evento não encontrado", exception.getMessage());
    }

    @Test
    void deveDeletarEvento() {
        when(eventoRepository.existsById(eventoId)).thenReturn(true);
        doNothing().when(eventoRepository).deleteById(eventoId);

        eventoService.deletar(eventoId);

        verify(eventoRepository).existsById(eventoId);
        verify(eventoRepository).deleteById(eventoId);
    }

    @Test
    void deveLancarExcecaoAoDeletarEventoInexistente() {
        when(eventoRepository.existsById(eventoId)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> eventoService.deletar(eventoId)
        );
        assertEquals("Evento não encontrado", exception.getMessage());
    }

    @Test
    void deveContarEventosPorCalendario() {
        List<Evento> eventos = Arrays.asList(evento, new Evento(), new Evento());
        when(eventoRepository.findByCalendarioId(calendarioId)).thenReturn(eventos);

        long resultado = eventoService.contarPorCalendario(calendarioId);

        assertEquals(3, resultado);
    }

    @Test
    void deveVerificarSeEventoPertenceAoCalendario() {
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));

        boolean resultado = eventoService.pertenceAoCalendario(eventoId, calendarioId);

        assertTrue(resultado);
    }

    @Test
    void deveRetornarFalsoQuandoEventoNaoPertenceAoCalendario() {
        UUID outroCalendarioId = UUID.randomUUID();
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));

        boolean resultado = eventoService.pertenceAoCalendario(eventoId, outroCalendarioId);

        assertFalse(resultado);
    }
}
