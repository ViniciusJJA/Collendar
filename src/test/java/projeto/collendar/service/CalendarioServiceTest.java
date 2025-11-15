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
import projeto.collendar.model.Usuario;
import projeto.collendar.repository.CalendarioRepository;
import projeto.collendar.repository.UsuarioRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class CalendarioServiceTest {

    @Mock
    private CalendarioRepository calendarioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CalendarioService calendarioService;

    @Nested
    class Dado_um_calendario_valido {

        Calendario calendario;
        Usuario usuario;
        UUID calendarioId;
        UUID usuarioId;

        @BeforeEach
        void setup() {
            calendarioId = UUID.randomUUID();
            usuarioId = UUID.randomUUID();

            usuario = new Usuario();
            usuario.setId(usuarioId);
            usuario.setNome("João Silva");

            calendario = new Calendario();
            calendario.setId(calendarioId);
            calendario.setNome("Trabalho");
            calendario.setDescricao("Calendário de trabalho");
            calendario.setCor("#FF5733");
            calendario.setUsuario(usuario);
        }

        @Nested
        class Quando_criar_calendario {

            @BeforeEach
            void setup() {
                when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
                when(calendarioRepository.save(any(Calendario.class))).thenReturn(calendario);
            }

            @Test
            void deve_criar_calendario_com_sucesso() {
                Calendario resultado = calendarioService.criar(calendario, usuarioId);

                assertNotNull(resultado);
                assertEquals("Trabalho", resultado.getNome());
                assertEquals(usuario, resultado.getUsuario());
                verify(calendarioRepository).save(any(Calendario.class));
            }
        }

        @Nested
        class Quando_criar_calendario_com_usuario_inexistente {

            @BeforeEach
            void setup() {
                when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());
            }

            @Test
            void deve_lancar_excecao() {
                IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> calendarioService.criar(calendario, usuarioId)
                );

                assertEquals("Usuário não encontrado", exception.getMessage());
                verify(calendarioRepository, never()).save(any());
            }
        }

        @Nested
        class Quando_buscar_calendario_por_id {

            @Test
            void deve_retornar_calendario_quando_existir() {
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));

                Optional<Calendario> resultado = calendarioService.buscarPorId(calendarioId);

                assertTrue(resultado.isPresent());
                assertEquals("Trabalho", resultado.get().getNome());
            }

            @Test
            void deve_retornar_vazio_quando_nao_existir() {
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.empty());

                Optional<Calendario> resultado = calendarioService.buscarPorId(calendarioId);

                assertFalse(resultado.isPresent());
            }
        }

        @Nested
        class Quando_listar_calendarios {

            @Test
            void deve_listar_todos_calendarios() {
                List<Calendario> calendarios = Arrays.asList(calendario, new Calendario());
                when(calendarioRepository.findAll()).thenReturn(calendarios);

                List<Calendario> resultado = calendarioService.listarTodos();

                assertEquals(2, resultado.size());
                verify(calendarioRepository).findAll();
            }

            @Test
            void deve_listar_calendarios_por_usuario() {
                List<Calendario> calendarios = Arrays.asList(calendario);
                when(calendarioRepository.findByUsuarioId(usuarioId)).thenReturn(calendarios);

                List<Calendario> resultado = calendarioService.listarPorUsuario(usuarioId);

                assertEquals(1, resultado.size());
                assertEquals("Trabalho", resultado.get(0).getNome());
            }

            @Test
            void deve_listar_calendarios_por_usuario_paginado() {
                Pageable pageable = PageRequest.of(0, 10);
                Page<Calendario> page = new PageImpl<>(Arrays.asList(calendario));
                when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
                when(calendarioRepository.findByUsuario(usuario, pageable)).thenReturn(page);

                Page<Calendario> resultado = calendarioService.listarPorUsuarioPaginado(usuarioId, pageable);

                assertEquals(1, resultado.getTotalElements());
                assertEquals("Trabalho", resultado.getContent().get(0).getNome());
            }
        }

        @Nested
        class Quando_buscar_calendarios_por_nome {

            @BeforeEach
            void setup() {
                Pageable pageable = PageRequest.of(0, 10);
                Page<Calendario> page = new PageImpl<>(Arrays.asList(calendario));
                when(calendarioRepository.findByNomeContainingIgnoreCase("Trabalho", pageable)).thenReturn(page);
            }

            @Test
            void deve_retornar_calendarios_encontrados() {
                Pageable pageable = PageRequest.of(0, 10);
                Page<Calendario> resultado = calendarioService.buscarPorNome("Trabalho", pageable);

                assertEquals(1, resultado.getTotalElements());
            }
        }

        @Nested
        class Quando_atualizar_calendario {

            Calendario calendarioAtualizado;

            @BeforeEach
            void setup() {
                calendarioAtualizado = new Calendario();
                calendarioAtualizado.setNome("Trabalho Updated");
                calendarioAtualizado.setDescricao("Nova descrição");
                calendarioAtualizado.setCor("#00FF00");

                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
                when(calendarioRepository.save(any(Calendario.class))).thenReturn(calendario);
            }

            @Test
            void deve_atualizar_calendario_com_sucesso() {
                Calendario resultado = calendarioService.atualizar(calendarioId, calendarioAtualizado);

                assertNotNull(resultado);
                verify(calendarioRepository).save(any(Calendario.class));
            }
        }

        @Nested
        class Quando_atualizar_calendario_inexistente {

            @BeforeEach
            void setup() {
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.empty());
            }

            @Test
            void deve_lancar_excecao() {
                IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> calendarioService.atualizar(calendarioId, calendario)
                );

                assertEquals("Calendário não encontrado", exception.getMessage());
            }
        }

        @Nested
        class Quando_deletar_calendario {

            @Test
            void deve_deletar_calendario_existente() {
                when(calendarioRepository.existsById(calendarioId)).thenReturn(true);
                doNothing().when(calendarioRepository).deleteById(calendarioId);

                calendarioService.deletar(calendarioId);

                verify(calendarioRepository).existsById(calendarioId);
                verify(calendarioRepository).deleteById(calendarioId);
            }

            @Test
            void deve_lancar_excecao_quando_calendario_nao_existe() {
                when(calendarioRepository.existsById(calendarioId)).thenReturn(false);

                IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> calendarioService.deletar(calendarioId)
                );

                assertEquals("Calendário não encontrado", exception.getMessage());
            }
        }

        @Nested
        class Quando_verificar_proprietario {

            @BeforeEach
            void setup() {
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
            }

            @Test
            void deve_retornar_true_quando_usuario_eh_proprietario() {
                boolean resultado = calendarioService.verificarProprietario(calendarioId, usuarioId);

                assertTrue(resultado);
            }

            @Test
            void deve_retornar_false_quando_usuario_nao_eh_proprietario() {
                UUID outroUsuarioId = UUID.randomUUID();

                boolean resultado = calendarioService.verificarProprietario(calendarioId, outroUsuarioId);

                assertFalse(resultado);
            }
        }

        @Nested
        class Quando_contar_calendarios_por_usuario {

            @BeforeEach
            void setup() {
                List<Calendario> calendarios = Arrays.asList(calendario, new Calendario());
                when(calendarioRepository.findByUsuarioId(usuarioId)).thenReturn(calendarios);
            }

            @Test
            void deve_retornar_quantidade_correta() {
                long resultado = calendarioService.contarPorUsuario(usuarioId);

                assertEquals(2, resultado);
            }
        }
    }
}