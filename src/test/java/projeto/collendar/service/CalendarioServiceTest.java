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
import projeto.collendar.model.Usuario;
import projeto.collendar.repository.CalendarioRepository;
import projeto.collendar.repository.UsuarioRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarioServiceTest {

    @Mock
    private CalendarioRepository calendarioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CalendarioService calendarioService;

    private Calendario calendario;
    private Usuario usuario;
    private UUID calendarioId;
    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        calendarioId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();

        usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setNome("João Silva");
        usuario.setEmail("joao@email.com");

        calendario = new Calendario();
        calendario.setId(calendarioId);
        calendario.setNome("Trabalho");
        calendario.setDescricao("Calendário de trabalho");
        calendario.setCor("#FF5733");
        calendario.setUsuario(usuario);
    }

    @Test
    void deveCriarCalendarioComSucesso() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(calendarioRepository.save(any(Calendario.class))).thenReturn(calendario);

        Calendario resultado = calendarioService.criar(calendario, usuarioId);

        assertNotNull(resultado);
        assertEquals("Trabalho", resultado.getNome());
        assertEquals(usuario, resultado.getUsuario());
        verify(usuarioRepository).findById(usuarioId);
        verify(calendarioRepository).save(any(Calendario.class));
    }

    @Test
    void deveLancarExcecaoAoCriarCalendarioComUsuarioInexistente() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calendarioService.criar(calendario, usuarioId)
        );
        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(calendarioRepository, never()).save(any());
    }

    @Test
    void deveBuscarCalendarioPorId() {
        when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));

        Optional<Calendario> resultado = calendarioService.buscarPorId(calendarioId);

        assertTrue(resultado.isPresent());
        assertEquals("Trabalho", resultado.get().getNome());
    }

    @Test
    void deveListarTodosCalendarios() {
        List<Calendario> calendarios = Arrays.asList(calendario, new Calendario());
        when(calendarioRepository.findAll()).thenReturn(calendarios);

        List<Calendario> resultado = calendarioService.listarTodos();

        assertEquals(2, resultado.size());
        verify(calendarioRepository).findAll();
    }

    @Test
    void deveListarCalendariosPorUsuario() {
        List<Calendario> calendarios = Arrays.asList(calendario);
        when(calendarioRepository.findByUsuarioId(usuarioId)).thenReturn(calendarios);

        List<Calendario> resultado = calendarioService.listarPorUsuario(usuarioId);

        assertEquals(1, resultado.size());
        assertEquals("Trabalho", resultado.get(0).getNome());
    }

    @Test
    void deveListarCalendariosPorUsuarioPaginado() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Calendario> page = new PageImpl<>(Arrays.asList(calendario));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(calendarioRepository.findByUsuario(usuario, pageable)).thenReturn(page);

        Page<Calendario> resultado = calendarioService.listarPorUsuarioPaginado(usuarioId, pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Trabalho", resultado.getContent().get(0).getNome());
    }

    @Test
    void deveBuscarCalendariosPorNome() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Calendario> page = new PageImpl<>(Arrays.asList(calendario));
        when(calendarioRepository.findByNomeContainingIgnoreCase("Trabalho", pageable)).thenReturn(page);

        Page<Calendario> resultado = calendarioService.buscarPorNome("Trabalho", pageable);

        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    void deveAtualizarCalendario() {
        Calendario calendarioAtualizado = new Calendario();
        calendarioAtualizado.setNome("Trabalho Updated");
        calendarioAtualizado.setDescricao("Nova descrição");
        calendarioAtualizado.setCor("#00FF00");

        when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
        when(calendarioRepository.save(any(Calendario.class))).thenReturn(calendario);

        Calendario resultado = calendarioService.atualizar(calendarioId, calendarioAtualizado);

        assertNotNull(resultado);
        verify(calendarioRepository).save(any(Calendario.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarCalendarioInexistente() {
        when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calendarioService.atualizar(calendarioId, calendario)
        );
        assertEquals("Calendário não encontrado", exception.getMessage());
    }

    @Test
    void deveDeletarCalendario() {
        when(calendarioRepository.existsById(calendarioId)).thenReturn(true);
        doNothing().when(calendarioRepository).deleteById(calendarioId);

        calendarioService.deletar(calendarioId);

        verify(calendarioRepository).existsById(calendarioId);
        verify(calendarioRepository).deleteById(calendarioId);
    }

    @Test
    void deveLancarExcecaoAoDeletarCalendarioInexistente() {
        when(calendarioRepository.existsById(calendarioId)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calendarioService.deletar(calendarioId)
        );
        assertEquals("Calendário não encontrado", exception.getMessage());
    }

    @Test
    void deveVerificarSeUsuarioEhProprietario() {
        when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));

        boolean resultado = calendarioService.verificarProprietario(calendarioId, usuarioId);

        assertTrue(resultado);
    }

    @Test
    void deveRetornarFalsoQuandoUsuarioNaoEhProprietario() {
        UUID outroUsuarioId = UUID.randomUUID();
        when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));

        boolean resultado = calendarioService.verificarProprietario(calendarioId, outroUsuarioId);

        assertFalse(resultado);
    }

    @Test
    void deveContarCalendariosPorUsuario() {
        List<Calendario> calendarios = Arrays.asList(calendario, new Calendario());
        when(calendarioRepository.findByUsuarioId(usuarioId)).thenReturn(calendarios);

        long resultado = calendarioService.contarPorUsuario(usuarioId);

        assertEquals(2, resultado);
    }
}
