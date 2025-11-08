package projeto.collendar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import projeto.collendar.enums.TipoPermissao;
import projeto.collendar.model.Calendario;
import projeto.collendar.model.Compartilhamento;
import projeto.collendar.model.Usuario;
import projeto.collendar.repository.CalendarioRepository;
import projeto.collendar.repository.CompartilhamentoRepository;
import projeto.collendar.repository.UsuarioRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompartilhamentoServiceTest {

    @Mock
    private CompartilhamentoRepository compartilhamentoRepository;

    @Mock
    private CalendarioRepository calendarioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CompartilhamentoService compartilhamentoService;

    private Compartilhamento compartilhamento;
    private Calendario calendario;
    private Usuario dono;
    private Usuario usuarioCompartilhado;
    private UUID compartilhamentoId;
    private UUID calendarioId;
    private UUID donoId;
    private UUID usuarioCompartilhadoId;

    @BeforeEach
    void setUp() {
        compartilhamentoId = UUID.randomUUID();
        calendarioId = UUID.randomUUID();
        donoId = UUID.randomUUID();
        usuarioCompartilhadoId = UUID.randomUUID();

        dono = new Usuario();
        dono.setId(donoId);
        dono.setNome("João Silva");
        dono.setEmail("joao@email.com");

        usuarioCompartilhado = new Usuario();
        usuarioCompartilhado.setId(usuarioCompartilhadoId);
        usuarioCompartilhado.setNome("Maria Santos");
        usuarioCompartilhado.setEmail("maria@email.com");

        calendario = new Calendario();
        calendario.setId(calendarioId);
        calendario.setNome("Trabalho");
        calendario.setUsuario(dono);

        compartilhamento = new Compartilhamento();
        compartilhamento.setId(compartilhamentoId);
        compartilhamento.setCalendario(calendario);
        compartilhamento.setUsuario(usuarioCompartilhado);
        compartilhamento.setPermissao(TipoPermissao.VISUALIZAR);
    }

    @Test
    void deveCompartilharCalendarioComSucesso() {
        when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
        when(usuarioRepository.findById(usuarioCompartilhadoId)).thenReturn(Optional.of(usuarioCompartilhado));
        when(compartilhamentoRepository.existsByCalendarioAndUsuario(calendario, usuarioCompartilhado)).thenReturn(false);
        when(compartilhamentoRepository.save(any(Compartilhamento.class))).thenReturn(compartilhamento);

        Compartilhamento resultado = compartilhamentoService.compartilhar(
                calendarioId,
                usuarioCompartilhadoId,
                TipoPermissao.VISUALIZAR
        );

        assertNotNull(resultado);
        assertEquals(TipoPermissao.VISUALIZAR, resultado.getPermissao());
        verify(compartilhamentoRepository).save(any(Compartilhamento.class));
    }

    @Test
    void deveLancarExcecaoAoCompartilharCalendarioInexistente() {
        when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> compartilhamentoService.compartilhar(calendarioId, usuarioCompartilhadoId, TipoPermissao.VISUALIZAR)
        );
        assertEquals("Calendário não encontrado", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoAoCompartilharComUsuarioInexistente() {
        when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
        when(usuarioRepository.findById(usuarioCompartilhadoId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> compartilhamentoService.compartilhar(calendarioId, usuarioCompartilhadoId, TipoPermissao.VISUALIZAR)
        );
        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoAoCompartilharCalendarioJaCompartilhado() {
        when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
        when(usuarioRepository.findById(usuarioCompartilhadoId)).thenReturn(Optional.of(usuarioCompartilhado));
        when(compartilhamentoRepository.existsByCalendarioAndUsuario(calendario, usuarioCompartilhado)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> compartilhamentoService.compartilhar(calendarioId, usuarioCompartilhadoId, TipoPermissao.VISUALIZAR)
        );
        assertEquals("Calendário já compartilhado com este usuário", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoAoCompartilharComProprietario() {
        when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
        when(usuarioRepository.findById(donoId)).thenReturn(Optional.of(dono));
        when(compartilhamentoRepository.existsByCalendarioAndUsuario(calendario, dono)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> compartilhamentoService.compartilhar(calendarioId, donoId, TipoPermissao.VISUALIZAR)
        );
        assertEquals("Não é possível compartilhar o calendário consigo mesmo", exception.getMessage());
    }

    @Test
    void deveBuscarCompartilhamentoPorId() {
        when(compartilhamentoRepository.findById(compartilhamentoId)).thenReturn(Optional.of(compartilhamento));

        Optional<Compartilhamento> resultado = compartilhamentoService.buscarPorId(compartilhamentoId);

        assertTrue(resultado.isPresent());
        assertEquals(TipoPermissao.VISUALIZAR, resultado.get().getPermissao());
    }

    @Test
    void deveListarCompartilhamentosPorCalendario() {
        List<Compartilhamento> compartilhamentos = Arrays.asList(compartilhamento);
        when(compartilhamentoRepository.findByCalendarioId(calendarioId)).thenReturn(compartilhamentos);

        List<Compartilhamento> resultado = compartilhamentoService.listarPorCalendario(calendarioId);

        assertEquals(1, resultado.size());
    }

    @Test
    void deveListarCalendariosCompartilhados() {
        List<Calendario> calendarios = Arrays.asList(calendario);
        when(compartilhamentoRepository.findCalendariosCompartilhadosComUsuario(usuarioCompartilhadoId))
                .thenReturn(calendarios);

        List<Calendario> resultado = compartilhamentoService.listarCalendariosCompartilhados(usuarioCompartilhadoId);

        assertEquals(1, resultado.size());
        assertEquals("Trabalho", resultado.get(0).getNome());
    }

    @Test
    void deveListarCompartilhamentosRecebidos() {
        List<Compartilhamento> compartilhamentos = Arrays.asList(compartilhamento);
        when(compartilhamentoRepository.findByUsuarioId(usuarioCompartilhadoId)).thenReturn(compartilhamentos);

        List<Compartilhamento> resultado = compartilhamentoService.listarCompartilhamentosRecebidos(usuarioCompartilhadoId);

        assertEquals(1, resultado.size());
    }

    @Test
    void deveBuscarCompartilhamentoEspecifico() {
        when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
        when(usuarioRepository.findById(usuarioCompartilhadoId)).thenReturn(Optional.of(usuarioCompartilhado));
        when(compartilhamentoRepository.findByCalendarioAndUsuario(calendario, usuarioCompartilhado))
                .thenReturn(Optional.of(compartilhamento));

        Optional<Compartilhamento> resultado = compartilhamentoService.buscarCompartilhamento(calendarioId, usuarioCompartilhadoId);

        assertTrue(resultado.isPresent());
    }

    @Test
    void deveAtualizarPermissao() {
        when(compartilhamentoRepository.findById(compartilhamentoId)).thenReturn(Optional.of(compartilhamento));
        when(compartilhamentoRepository.save(any(Compartilhamento.class))).thenReturn(compartilhamento);

        Compartilhamento resultado = compartilhamentoService.atualizarPermissao(compartilhamentoId, TipoPermissao.EDITAR);

        assertNotNull(resultado);
        verify(compartilhamentoRepository).save(any(Compartilhamento.class));
    }

    @Test
    void deveRemoverCompartilhamento() {
        doNothing().when(compartilhamentoRepository).deleteByCalendarioIdAndUsuarioId(calendarioId, usuarioCompartilhadoId);

        compartilhamentoService.removerCompartilhamento(calendarioId, usuarioCompartilhadoId);

        verify(compartilhamentoRepository).deleteByCalendarioIdAndUsuarioId(calendarioId, usuarioCompartilhadoId);
    }

    @Test
    void deveDeletarCompartilhamento() {
        when(compartilhamentoRepository.existsById(compartilhamentoId)).thenReturn(true);
        doNothing().when(compartilhamentoRepository).deleteById(compartilhamentoId);

        compartilhamentoService.deletar(compartilhamentoId);

        verify(compartilhamentoRepository).deleteById(compartilhamentoId);
    }

    @Test
    void deveVerificarSeUsuarioTemAcesso() {
        when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
        when(usuarioRepository.findById(usuarioCompartilhadoId)).thenReturn(Optional.of(usuarioCompartilhado));
        when(compartilhamentoRepository.existsByCalendarioAndUsuario(calendario, usuarioCompartilhado)).thenReturn(true);

        boolean resultado = compartilhamentoService.temAcesso(calendarioId, usuarioCompartilhadoId);

        assertTrue(resultado);
    }

    @Test
    void deveRetornarTrueQuandoUsuarioEhProprietario() {
        when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));

        boolean resultado = compartilhamentoService.temAcesso(calendarioId, donoId);

        assertTrue(resultado);
    }

    @Test
    void deveVerificarSeUsuarioPodeEditar() {
        compartilhamento.setPermissao(TipoPermissao.EDITAR);
        when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
        when(usuarioRepository.findById(usuarioCompartilhadoId)).thenReturn(Optional.of(usuarioCompartilhado));
        when(compartilhamentoRepository.findByCalendarioAndUsuario(calendario, usuarioCompartilhado))
                .thenReturn(Optional.of(compartilhamento));

        boolean resultado = compartilhamentoService.podeEditar(calendarioId, usuarioCompartilhadoId);

        assertTrue(resultado);
    }

    @Test
    void deveRetornarTrueQuandoProprietarioPodeEditar() {
        when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));

        boolean resultado = compartilhamentoService.podeEditar(calendarioId, donoId);

        assertTrue(resultado);
    }

    @Test
    void deveRetornarFalsoQuandoUsuarioSoTemPermissaoVisualizar() {
        when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
        when(usuarioRepository.findById(usuarioCompartilhadoId)).thenReturn(Optional.of(usuarioCompartilhado));
        when(compartilhamentoRepository.findByCalendarioAndUsuario(calendario, usuarioCompartilhado))
                .thenReturn(Optional.of(compartilhamento));

        boolean resultado = compartilhamentoService.podeEditar(calendarioId, usuarioCompartilhadoId);

        assertFalse(resultado);
    }

    @Test
    void deveContarCompartilhamentosPorCalendario() {
        List<Compartilhamento> compartilhamentos = Arrays.asList(
                compartilhamento,
                new Compartilhamento(),
                new Compartilhamento()
        );
        when(compartilhamentoRepository.findByCalendarioId(calendarioId)).thenReturn(compartilhamentos);

        long resultado = compartilhamentoService.contarPorCalendario(calendarioId);

        assertEquals(3, resultado);
    }
}
