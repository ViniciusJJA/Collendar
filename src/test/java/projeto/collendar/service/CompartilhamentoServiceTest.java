package projeto.collendar.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import projeto.collendar.dtos.request.CompartilhamentoRequestDTO;
import projeto.collendar.dtos.response.CalendarioResponseDTO;
import projeto.collendar.dtos.response.CompartilhamentoResponseDTO;
import projeto.collendar.dtos.response.PermissaoResponseDTO;
import projeto.collendar.enums.TipoPermissao;
import projeto.collendar.exception.BusinessException;
import projeto.collendar.exception.ResourceNotFoundException;
import projeto.collendar.model.Calendario;
import projeto.collendar.model.Compartilhamento;
import projeto.collendar.model.Usuario;
import projeto.collendar.repository.CompartilhamentoRepository;

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
    private CalendarioService calendarioService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private CompartilhamentoService compartilhamentoService;

    @Nested
    class Dado_um_compartilhamento_valido_para_criar {

        CompartilhamentoRequestDTO dto;
        Calendario calendario;
        Usuario dono;
        Usuario destinatario;
        UUID calendarioId;
        UUID donoId;
        UUID destinatarioId;

        @BeforeEach
        void setup() {
            calendarioId = UUID.randomUUID();
            donoId = UUID.randomUUID();
            destinatarioId = UUID.randomUUID();

            dono = new Usuario();
            dono.setId(donoId);
            dono.setNome("João Silva");
            dono.setEmail("joao@email.com");

            destinatario = new Usuario();
            destinatario.setId(destinatarioId);
            destinatario.setNome("Maria Santos");
            destinatario.setEmail("maria@email.com");

            calendario = new Calendario();
            calendario.setId(calendarioId);
            calendario.setNome("Trabalho");
            calendario.setUsuario(dono);

            dto = new CompartilhamentoRequestDTO(
                    calendarioId,
                    "maria@email.com",
                    TipoPermissao.VISUALIZAR
            );
        }

        @Nested
        class Quando_compartilhar_calendario {

            CompartilhamentoResponseDTO resultado;

            @BeforeEach
            void setup() {
                when(calendarioService.findEntityById(calendarioId)).thenReturn(calendario);
                when(usuarioService.findEntityByEmail("maria@email.com")).thenReturn(destinatario);
                when(compartilhamentoRepository.existsByCalendarioAndUsuario(calendario, destinatario))
                        .thenReturn(false);
                when(compartilhamentoRepository.save(any(Compartilhamento.class)))
                        .thenAnswer(invocation -> {
                            Compartilhamento compartilhamento = invocation.getArgument(0);
                            compartilhamento.setId(UUID.randomUUID());
                            return compartilhamento;
                        });

                resultado = compartilhamentoService.create(dto);
            }

            @Test
            void deve_compartilhar_calendario_com_sucesso() {
                assertNotNull(resultado);
                assertEquals(calendarioId, resultado.calendarioId());
                assertEquals("Trabalho", resultado.calendarioNome());
            }

            @Test
            void deve_associar_destinatario_ao_compartilhamento() {
                assertEquals(destinatarioId, resultado.usuarioId());
                assertEquals("Maria Santos", resultado.usuarioNome());
                assertEquals("maria@email.com", resultado.usuarioEmail());
            }

            @Test
            void deve_definir_permissao_corretamente() {
                assertEquals(TipoPermissao.VISUALIZAR, resultado.permissao());
            }

            @Test
            void deve_salvar_compartilhamento_no_repositorio() {
                verify(compartilhamentoRepository).save(any(Compartilhamento.class));
            }
        }

        @Nested
        class Quando_calendario_nao_existe {

            @BeforeEach
            void setup() {
                when(calendarioService.findEntityById(calendarioId))
                        .thenThrow(new ResourceNotFoundException("Calendário", calendarioId.toString()));
            }

            @Test
            void deve_lancar_resource_not_found_exception() {
                ResourceNotFoundException exception = assertThrows(
                        ResourceNotFoundException.class,
                        () -> compartilhamentoService.create(dto)
                );

                assertTrue(exception.getMessage().contains("Calendário não encontrado"));
            }

            @Test
            void nao_deve_salvar_compartilhamento() {
                assertThrows(ResourceNotFoundException.class,
                        () -> compartilhamentoService.create(dto));
                verify(compartilhamentoRepository, never()).save(any());
            }
        }

        @Nested
        class Quando_destinatario_nao_existe {

            @BeforeEach
            void setup() {
                when(calendarioService.findEntityById(calendarioId)).thenReturn(calendario);
                when(usuarioService.findEntityByEmail("maria@email.com"))
                        .thenThrow(new ResourceNotFoundException("Usuário", "maria@email.com"));
            }

            @Test
            void deve_lancar_resource_not_found_exception() {
                ResourceNotFoundException exception = assertThrows(
                        ResourceNotFoundException.class,
                        () -> compartilhamentoService.create(dto)
                );

                assertTrue(exception.getMessage().contains("Usuário não encontrado"));
            }
        }

        @Nested
        class Quando_tentar_compartilhar_consigo_mesmo {

            @BeforeEach
            void setup() {
                when(calendarioService.findEntityById(calendarioId)).thenReturn(calendario);
                when(usuarioService.findEntityByEmail("maria@email.com")).thenReturn(dono);
            }

            @Test
            void deve_lancar_business_exception() {
                BusinessException exception = assertThrows(
                        BusinessException.class,
                        () -> compartilhamentoService.create(dto)
                );

                assertEquals("Não é possível compartilhar o calendário consigo mesmo",
                        exception.getMessage());
            }
        }

        @Nested
        class Quando_calendario_ja_compartilhado {

            @BeforeEach
            void setup() {
                when(calendarioService.findEntityById(calendarioId)).thenReturn(calendario);
                when(usuarioService.findEntityByEmail("maria@email.com")).thenReturn(destinatario);
                when(compartilhamentoRepository.existsByCalendarioAndUsuario(calendario, destinatario))
                        .thenReturn(true);
            }

            @Test
            void deve_lancar_business_exception() {
                BusinessException exception = assertThrows(
                        BusinessException.class,
                        () -> compartilhamentoService.create(dto)
                );

                assertEquals("Calendário já compartilhado com este usuário",
                        exception.getMessage());
            }
        }
    }

    @Nested
    class Dado_um_compartilhamento_existente {

        Compartilhamento compartilhamento;
        Calendario calendario;
        Usuario dono;
        Usuario destinatario;
        UUID compartilhamentoId;
        UUID calendarioId;
        UUID destinatarioId;

        @BeforeEach
        void setup() {
            compartilhamentoId = UUID.randomUUID();
            calendarioId = UUID.randomUUID();
            destinatarioId = UUID.randomUUID();

            dono = new Usuario();
            dono.setId(UUID.randomUUID());
            dono.setNome("João Silva");

            destinatario = new Usuario();
            destinatario.setId(destinatarioId);
            destinatario.setNome("Maria Santos");
            destinatario.setEmail("maria@email.com");

            calendario = new Calendario();
            calendario.setId(calendarioId);
            calendario.setNome("Trabalho");
            calendario.setUsuario(dono);

            compartilhamento = new Compartilhamento();
            compartilhamento.setId(compartilhamentoId);
            compartilhamento.setCalendario(calendario);
            compartilhamento.setUsuario(destinatario);
            compartilhamento.setPermissao(TipoPermissao.VISUALIZAR);
        }

        @Nested
        class Quando_buscar_por_id {

            CompartilhamentoResponseDTO resultado;

            @BeforeEach
            void setup() {
                when(compartilhamentoRepository.findById(compartilhamentoId))
                        .thenReturn(Optional.of(compartilhamento));
                resultado = compartilhamentoService.findById(compartilhamentoId);
            }

            @Test
            void deve_retornar_compartilhamento_encontrado() {
                assertNotNull(resultado);
                assertEquals(compartilhamentoId, resultado.id());
                assertEquals(TipoPermissao.VISUALIZAR, resultado.permissao());
            }
        }

        @Nested
        class Quando_buscar_por_id_inexistente {

            @BeforeEach
            void setup() {
                when(compartilhamentoRepository.findById(compartilhamentoId))
                        .thenReturn(Optional.empty());
            }

            @Test
            void deve_lancar_resource_not_found_exception() {
                ResourceNotFoundException exception = assertThrows(
                        ResourceNotFoundException.class,
                        () -> compartilhamentoService.findById(compartilhamentoId)
                );

                assertTrue(exception.getMessage().contains("Compartilhamento não encontrado"));
            }
        }

        @Nested
        class Quando_listar_por_calendario {

            List<CompartilhamentoResponseDTO> resultado;

            @BeforeEach
            void setup() {
                when(compartilhamentoRepository.findByCalendarioId(calendarioId))
                        .thenReturn(Arrays.asList(compartilhamento));
                resultado = compartilhamentoService.listByCalendario(calendarioId);
            }

            @Test
            void deve_retornar_compartilhamentos_do_calendario() {
                assertNotNull(resultado);
                assertEquals(1, resultado.size());
                assertEquals(calendarioId, resultado.get(0).calendarioId());
            }
        }

        @Nested
        class Quando_listar_calendarios_compartilhados_com_usuario {

            List<CalendarioResponseDTO> resultado;

            @BeforeEach
            void setup() {
                when(compartilhamentoRepository.findCalendariosCompartilhadosComUsuario(destinatarioId))
                        .thenReturn(Arrays.asList(calendario));
                when(compartilhamentoRepository.findByCalendarioAndUsuario(calendario, destinatario))
                        .thenReturn(Optional.of(compartilhamento));
                when(usuarioService.findEntityById(destinatarioId)).thenReturn(destinatario);
                when(calendarioService.findEntityById(calendarioId)).thenReturn(calendario);

                resultado = compartilhamentoService.listSharedWithUsuario(destinatarioId);
            }

            @Test
            void deve_retornar_calendarios_compartilhados() {
                assertNotNull(resultado);
                assertEquals(1, resultado.size());
                assertEquals("Trabalho", resultado.get(0).nome());
            }

            @Test
            void deve_marcar_como_nao_proprietario() {
                assertFalse(resultado.get(0).proprietario());
            }
        }

        @Nested
        class Quando_listar_compartilhamentos_recebidos {

            List<CompartilhamentoResponseDTO> resultado;

            @BeforeEach
            void setup() {
                when(compartilhamentoRepository.findByUsuarioId(destinatarioId))
                        .thenReturn(Arrays.asList(compartilhamento));
                resultado = compartilhamentoService.listReceivedByUsuario(destinatarioId);
            }

            @Test
            void deve_retornar_compartilhamentos_recebidos() {
                assertNotNull(resultado);
                assertEquals(1, resultado.size());
                assertEquals(destinatarioId, resultado.get(0).usuarioId());
            }
        }

        @Nested
        class Quando_atualizar_permissao {

            CompartilhamentoResponseDTO resultado;

            @BeforeEach
            void setup() {
                when(compartilhamentoRepository.findById(compartilhamentoId))
                        .thenReturn(Optional.of(compartilhamento));
                when(compartilhamentoRepository.save(any(Compartilhamento.class)))
                        .thenReturn(compartilhamento);

                resultado = compartilhamentoService.updatePermissao(
                        compartilhamentoId,
                        TipoPermissao.EDITAR
                );
            }

            @Test
            void deve_atualizar_permissao_com_sucesso() {
                assertNotNull(resultado);
                verify(compartilhamentoRepository).save(any(Compartilhamento.class));
            }
        }

        @Nested
        class Quando_deletar_compartilhamento {

            @BeforeEach
            void setup() {
                when(compartilhamentoRepository.existsById(compartilhamentoId)).thenReturn(true);
                doNothing().when(compartilhamentoRepository).deleteById(compartilhamentoId);

                compartilhamentoService.delete(compartilhamentoId);
            }

            @Test
            void deve_deletar_compartilhamento() {
                verify(compartilhamentoRepository).deleteById(compartilhamentoId);
            }
        }

        @Nested
        class Quando_deletar_compartilhamento_inexistente {

            @BeforeEach
            void setup() {
                when(compartilhamentoRepository.existsById(compartilhamentoId)).thenReturn(false);
            }

            @Test
            void deve_lancar_resource_not_found_exception() {
                ResourceNotFoundException exception = assertThrows(
                        ResourceNotFoundException.class,
                        () -> compartilhamentoService.delete(compartilhamentoId)
                );

                assertTrue(exception.getMessage().contains("Compartilhamento não encontrado"));
            }
        }

        @Nested
        class Quando_verificar_se_tem_acesso {

            boolean resultado;

            @BeforeEach
            void setup() {
                when(calendarioService.findEntityById(calendarioId)).thenReturn(calendario);
                when(usuarioService.findEntityById(destinatarioId)).thenReturn(destinatario);
                when(compartilhamentoRepository.existsByCalendarioAndUsuario(calendario, destinatario))
                        .thenReturn(true);

                resultado = compartilhamentoService.hasAccess(calendarioId, destinatarioId);
            }

            @Test
            void deve_retornar_true_quando_tem_acesso() {
                assertTrue(resultado);
            }
        }

        @Nested
        class Quando_verificar_se_proprietario_tem_acesso {

            boolean resultado;

            @BeforeEach
            void setup() {
                when(calendarioService.findEntityById(calendarioId)).thenReturn(calendario);

                resultado = compartilhamentoService.hasAccess(calendarioId, dono.getId());
            }

            @Test
            void deve_retornar_true_quando_eh_proprietario() {
                assertTrue(resultado);
            }
        }

        @Nested
        class Quando_verificar_se_pode_editar {

            boolean resultado;

            @BeforeEach
            void setup() {
                compartilhamento.setPermissao(TipoPermissao.EDITAR);
                when(calendarioService.findEntityById(calendarioId)).thenReturn(calendario);
                when(usuarioService.findEntityById(destinatarioId)).thenReturn(destinatario);
                when(compartilhamentoRepository.findByCalendarioAndUsuario(calendario, destinatario))
                        .thenReturn(Optional.of(compartilhamento));

                resultado = compartilhamentoService.canEdit(calendarioId, destinatarioId);
            }

            @Test
            void deve_retornar_true_quando_tem_permissao_editar() {
                assertTrue(resultado);
            }
        }

        @Nested
        class Quando_verificar_se_pode_editar_com_permissao_visualizar {

            boolean resultado;

            @BeforeEach
            void setup() {
                when(calendarioService.findEntityById(calendarioId)).thenReturn(calendario);
                when(usuarioService.findEntityById(destinatarioId)).thenReturn(destinatario);
                when(compartilhamentoRepository.findByCalendarioAndUsuario(calendario, destinatario))
                        .thenReturn(Optional.of(compartilhamento));

                resultado = compartilhamentoService.canEdit(calendarioId, destinatarioId);
            }

            @Test
            void deve_retornar_false_quando_so_tem_permissao_visualizar() {
                assertFalse(resultado);
            }
        }

        @Nested
        class Quando_obter_permissao_do_usuario {

            PermissaoResponseDTO resultado;

            @BeforeEach
            void setup() {
                when(calendarioService.findEntityById(calendarioId)).thenReturn(calendario);
                when(usuarioService.findEntityById(destinatarioId)).thenReturn(destinatario);
                when(compartilhamentoRepository.findByCalendarioAndUsuario(calendario, destinatario))
                        .thenReturn(Optional.of(compartilhamento));

                resultado = compartilhamentoService.getMyPermission(calendarioId, destinatarioId);
            }

            @Test
            void deve_retornar_permissao_do_usuario() {
                assertNotNull(resultado);
                assertFalse(resultado.proprietario());
                assertTrue(resultado.podeVisualizar());
                assertFalse(resultado.podeEditar());
                assertEquals(TipoPermissao.VISUALIZAR, resultado.permissao());
            }
        }

        @Nested
        class Quando_obter_permissao_do_proprietario {

            PermissaoResponseDTO resultado;

            @BeforeEach
            void setup() {
                when(calendarioService.findEntityById(calendarioId)).thenReturn(calendario);

                resultado = compartilhamentoService.getMyPermission(calendarioId, dono.getId());
            }

            @Test
            void deve_retornar_permissao_de_proprietario() {
                assertNotNull(resultado);
                assertTrue(resultado.proprietario());
                assertTrue(resultado.podeVisualizar());
                assertTrue(resultado.podeEditar());
                assertNull(resultado.permissao());
            }
        }

        @Nested
        class Quando_contar_compartilhamentos_por_calendario {

            long resultado;

            @BeforeEach
            void setup() {
                Compartilhamento comp2 = new Compartilhamento();
                comp2.setId(UUID.randomUUID());

                when(compartilhamentoRepository.findByCalendarioId(calendarioId))
                        .thenReturn(Arrays.asList(compartilhamento, comp2));

                resultado = compartilhamentoService.countByCalendario(calendarioId);
            }

            @Test
            void deve_retornar_quantidade_correta() {
                assertEquals(2, resultado);
            }
        }

        @Nested
        class Quando_obter_calendario_id_do_compartilhamento {

            UUID resultado;

            @BeforeEach
            void setup() {
                when(compartilhamentoRepository.findById(compartilhamentoId))
                        .thenReturn(Optional.of(compartilhamento));
                resultado = compartilhamentoService.getCalendarioIdByCompartilhamento(compartilhamentoId);
            }

            @Test
            void deve_retornar_id_do_calendario() {
                assertEquals(calendarioId, resultado);
            }
        }

        @Nested
        class Quando_obter_destinatario_id_do_compartilhamento {

            UUID resultado;

            @BeforeEach
            void setup() {
                when(compartilhamentoRepository.findById(compartilhamentoId))
                        .thenReturn(Optional.of(compartilhamento));
                resultado = compartilhamentoService.getDestinatarioIdByCompartilhamento(compartilhamentoId);
            }

            @Test
            void deve_retornar_id_do_destinatario() {
                assertEquals(destinatarioId, resultado);
            }
        }
    }
}