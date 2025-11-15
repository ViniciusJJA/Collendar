package projeto.collendar.service;

import org.junit.jupiter.api.*;
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

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
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

    @Nested
    class Dado_um_compartilhamento_valido {

        Compartilhamento compartilhamento;
        Calendario calendario;
        Usuario dono;
        Usuario usuarioCompartilhado;
        UUID compartilhamentoId;
        UUID calendarioId;
        UUID donoId;
        UUID usuarioCompartilhadoId;

        @BeforeEach
        void setup() {
            compartilhamentoId = UUID.randomUUID();
            calendarioId = UUID.randomUUID();
            donoId = UUID.randomUUID();
            usuarioCompartilhadoId = UUID.randomUUID();

            dono = new Usuario();
            dono.setId(donoId);
            dono.setNome("João Silva");

            usuarioCompartilhado = new Usuario();
            usuarioCompartilhado.setId(usuarioCompartilhadoId);
            usuarioCompartilhado.setNome("Maria Santos");

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

        @Nested
        class Quando_compartilhar_calendario {

            @BeforeEach
            void setup() {
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
                when(usuarioRepository.findById(usuarioCompartilhadoId)).thenReturn(Optional.of(usuarioCompartilhado));
                when(compartilhamentoRepository.existsByCalendarioAndUsuario(calendario, usuarioCompartilhado))
                        .thenReturn(false);
                when(compartilhamentoRepository.save(any(Compartilhamento.class))).thenReturn(compartilhamento);
            }

            @Test
            void deve_compartilhar_calendario_com_sucesso() {
                Compartilhamento resultado = compartilhamentoService.compartilhar(
                        calendarioId,
                        usuarioCompartilhadoId,
                        TipoPermissao.VISUALIZAR
                );

                assertNotNull(resultado);
                assertEquals(TipoPermissao.VISUALIZAR, resultado.getPermissao());
                verify(compartilhamentoRepository).save(any(Compartilhamento.class));
            }
        }

        @Nested
        class Quando_compartilhar_com_calendario_inexistente {

            @BeforeEach
            void setup() {
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.empty());
            }

            @Test
            void deve_lancar_excecao() {
                IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> compartilhamentoService.compartilhar(calendarioId, usuarioCompartilhadoId, TipoPermissao.VISUALIZAR)
                );

                assertEquals("Calendário não encontrado", exception.getMessage());
            }
        }

        @Nested
        class Quando_compartilhar_com_usuario_inexistente {

            @BeforeEach
            void setup() {
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
                when(usuarioRepository.findById(usuarioCompartilhadoId)).thenReturn(Optional.empty());
            }

            @Test
            void deve_lancar_excecao() {
                IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> compartilhamentoService.compartilhar(calendarioId, usuarioCompartilhadoId, TipoPermissao.VISUALIZAR)
                );

                assertEquals("Usuário não encontrado", exception.getMessage());
            }
        }

        @Nested
        class Quando_calendario_ja_compartilhado {

            @BeforeEach
            void setup() {
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
                when(usuarioRepository.findById(usuarioCompartilhadoId)).thenReturn(Optional.of(usuarioCompartilhado));
                when(compartilhamentoRepository.existsByCalendarioAndUsuario(calendario, usuarioCompartilhado))
                        .thenReturn(true);
            }

            @Test
            void deve_lancar_excecao() {
                IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> compartilhamentoService.compartilhar(calendarioId, usuarioCompartilhadoId, TipoPermissao.VISUALIZAR)
                );

                assertEquals("Calendário já compartilhado com este usuário", exception.getMessage());
            }
        }

        @Nested
        class Quando_compartilhar_com_proprietario {

            @BeforeEach
            void setup() {
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
                when(usuarioRepository.findById(donoId)).thenReturn(Optional.of(dono));
                when(compartilhamentoRepository.existsByCalendarioAndUsuario(calendario, dono)).thenReturn(false);
            }

            @Test
            void deve_lancar_excecao() {
                IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> compartilhamentoService.compartilhar(calendarioId, donoId, TipoPermissao.VISUALIZAR)
                );

                assertEquals("Não é possível compartilhar o calendário consigo mesmo", exception.getMessage());
            }
        }

        @Nested
        class Quando_buscar_compartilhamento_por_id {

            @Test
            void deve_retornar_compartilhamento_quando_existir() {
                when(compartilhamentoRepository.findById(compartilhamentoId)).thenReturn(Optional.of(compartilhamento));

                Optional<Compartilhamento> resultado = compartilhamentoService.buscarPorId(compartilhamentoId);

                assertTrue(resultado.isPresent());
                assertEquals(TipoPermissao.VISUALIZAR, resultado.get().getPermissao());
            }

            @Test
            void deve_retornar_vazio_quando_nao_existir() {
                when(compartilhamentoRepository.findById(compartilhamentoId)).thenReturn(Optional.empty());

                Optional<Compartilhamento> resultado = compartilhamentoService.buscarPorId(compartilhamentoId);

                assertFalse(resultado.isPresent());
            }
        }

        @Nested
        class Quando_listar_compartilhamentos {

            @Test
            void deve_listar_compartilhamentos_por_calendario() {
                List<Compartilhamento> compartilhamentos = Arrays.asList(compartilhamento);
                when(compartilhamentoRepository.findByCalendarioId(calendarioId)).thenReturn(compartilhamentos);

                List<Compartilhamento> resultado = compartilhamentoService.listarPorCalendario(calendarioId);

                assertEquals(1, resultado.size());
            }

            @Test
            void deve_listar_calendarios_compartilhados_com_usuario() {
                List<Calendario> calendarios = Arrays.asList(calendario);
                when(compartilhamentoRepository.findCalendariosCompartilhadosComUsuario(usuarioCompartilhadoId))
                        .thenReturn(calendarios);

                List<Calendario> resultado = compartilhamentoService.listarCalendariosCompartilhados(usuarioCompartilhadoId);

                assertEquals(1, resultado.size());
                assertEquals("Trabalho", resultado.get(0).getNome());
            }

            @Test
            void deve_listar_compartilhamentos_recebidos_por_usuario() {
                List<Compartilhamento> compartilhamentos = Arrays.asList(compartilhamento);
                when(compartilhamentoRepository.findByUsuarioId(usuarioCompartilhadoId)).thenReturn(compartilhamentos);

                List<Compartilhamento> resultado = compartilhamentoService.listarCompartilhamentosRecebidos(usuarioCompartilhadoId);

                assertEquals(1, resultado.size());
            }
        }

        @Nested
        class Quando_buscar_compartilhamento_especifico {

            @BeforeEach
            void setup() {
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
                when(usuarioRepository.findById(usuarioCompartilhadoId)).thenReturn(Optional.of(usuarioCompartilhado));
                when(compartilhamentoRepository.findByCalendarioAndUsuario(calendario, usuarioCompartilhado))
                        .thenReturn(Optional.of(compartilhamento));
            }

            @Test
            void deve_retornar_compartilhamento_encontrado() {
                Optional<Compartilhamento> resultado = compartilhamentoService.buscarCompartilhamento(
                        calendarioId,
                        usuarioCompartilhadoId
                );

                assertTrue(resultado.isPresent());
            }
        }

        @Nested
        class Quando_atualizar_permissao {

            @BeforeEach
            void setup() {
                when(compartilhamentoRepository.findById(compartilhamentoId)).thenReturn(Optional.of(compartilhamento));
                when(compartilhamentoRepository.save(any(Compartilhamento.class))).thenReturn(compartilhamento);
            }

            @Test
            void deve_atualizar_permissao_com_sucesso() {
                Compartilhamento resultado = compartilhamentoService.atualizarPermissao(
                        compartilhamentoId,
                        TipoPermissao.EDITAR
                );

                assertNotNull(resultado);
                verify(compartilhamentoRepository).save(any(Compartilhamento.class));
            }
        }

        @Nested
        class Quando_remover_compartilhamento {

            @Test
            void deve_remover_compartilhamento_por_ids() {
                doNothing().when(compartilhamentoRepository).deleteByCalendarioIdAndUsuarioId(calendarioId, usuarioCompartilhadoId);

                compartilhamentoService.removerCompartilhamento(calendarioId, usuarioCompartilhadoId);

                verify(compartilhamentoRepository).deleteByCalendarioIdAndUsuarioId(calendarioId, usuarioCompartilhadoId);
            }
        }

        @Nested
        class Quando_deletar_compartilhamento {

            @Test
            void deve_deletar_compartilhamento_existente() {
                when(compartilhamentoRepository.existsById(compartilhamentoId)).thenReturn(true);
                doNothing().when(compartilhamentoRepository).deleteById(compartilhamentoId);

                compartilhamentoService.deletar(compartilhamentoId);

                verify(compartilhamentoRepository).deleteById(compartilhamentoId);
            }

            @Test
            void deve_lancar_excecao_quando_compartilhamento_nao_existe() {
                when(compartilhamentoRepository.existsById(compartilhamentoId)).thenReturn(false);

                IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> compartilhamentoService.deletar(compartilhamentoId)
                );

                assertEquals("Compartilhamento não encontrado", exception.getMessage());
            }
        }

        @Nested
        class Quando_verificar_acesso_ao_calendario {

            @Test
            void deve_retornar_true_quando_usuario_tem_acesso_por_compartilhamento() {
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
                when(usuarioRepository.findById(usuarioCompartilhadoId)).thenReturn(Optional.of(usuarioCompartilhado));
                when(compartilhamentoRepository.existsByCalendarioAndUsuario(calendario, usuarioCompartilhado))
                        .thenReturn(true);

                boolean resultado = compartilhamentoService.temAcesso(calendarioId, usuarioCompartilhadoId);

                assertTrue(resultado);
            }

            @Test
            void deve_retornar_true_quando_usuario_eh_proprietario() {
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));

                boolean resultado = compartilhamentoService.temAcesso(calendarioId, donoId);

                assertTrue(resultado);
            }
        }

        @Nested
        class Quando_verificar_permissao_de_edicao {

            @Test
            void deve_retornar_true_quando_usuario_tem_permissao_editar() {
                compartilhamento.setPermissao(TipoPermissao.EDITAR);
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
                when(usuarioRepository.findById(usuarioCompartilhadoId)).thenReturn(Optional.of(usuarioCompartilhado));
                when(compartilhamentoRepository.findByCalendarioAndUsuario(calendario, usuarioCompartilhado))
                        .thenReturn(Optional.of(compartilhamento));

                boolean resultado = compartilhamentoService.podeEditar(calendarioId, usuarioCompartilhadoId);

                assertTrue(resultado);
            }

            @Test
            void deve_retornar_true_quando_usuario_eh_proprietario() {
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));

                boolean resultado = compartilhamentoService.podeEditar(calendarioId, donoId);

                assertTrue(resultado);
            }

            @Test
            void deve_retornar_false_quando_usuario_so_tem_permissao_visualizar() {
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
                when(usuarioRepository.findById(usuarioCompartilhadoId)).thenReturn(Optional.of(usuarioCompartilhado));
                when(compartilhamentoRepository.findByCalendarioAndUsuario(calendario, usuarioCompartilhado))
                        .thenReturn(Optional.of(compartilhamento));

                boolean resultado = compartilhamentoService.podeEditar(calendarioId, usuarioCompartilhadoId);

                assertFalse(resultado);
            }
        }

        @Nested
        class Quando_contar_compartilhamentos_por_calendario {

            @BeforeEach
            void setup() {
                List<Compartilhamento> compartilhamentos = Arrays.asList(
                        compartilhamento,
                        new Compartilhamento(),
                        new Compartilhamento()
                );
                when(compartilhamentoRepository.findByCalendarioId(calendarioId)).thenReturn(compartilhamentos);
            }

            @Test
            void deve_retornar_quantidade_correta() {
                long resultado = compartilhamentoService.contarPorCalendario(calendarioId);

                assertEquals(3, resultado);
            }
        }
    }
}
//comentario