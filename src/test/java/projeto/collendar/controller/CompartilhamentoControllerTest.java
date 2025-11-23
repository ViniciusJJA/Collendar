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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import projeto.collendar.enums.TipoPermissao;
import projeto.collendar.model.Calendario;
import projeto.collendar.model.Compartilhamento;
import projeto.collendar.model.Usuario;
import projeto.collendar.service.CompartilhamentoService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CompartilhamentoControllerTest {

    @Mock
    private CompartilhamentoService compartilhamentoService;

    @InjectMocks
    private CompartilhamentoController compartilhamentoController;

    @Nested
    class Dado_um_compartilhamento_valido {

        Compartilhamento compartilhamento;
        Calendario calendario;
        Usuario dono;
        Usuario usuarioCompartilhado;
        UUID compartilhamentoId;
        UUID calendarioId;
        UUID usuarioCompartilhadoId;

        @BeforeEach
        void setup() {
            compartilhamentoId = UUID.randomUUID();
            calendarioId = UUID.randomUUID();
            usuarioCompartilhadoId = UUID.randomUUID();

            dono = new Usuario();
            dono.setId(UUID.randomUUID());
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

            ResponseEntity<CompartilhamentoDTO> resposta;

            @BeforeEach
            void setup() {
                when(compartilhamentoService.compartilhar(calendarioId, usuarioCompartilhadoId, TipoPermissao.VISUALIZAR))
                        .thenReturn(compartilhamento);
                resposta = compartilhamentoController.compartilhar(calendarioId, usuarioCompartilhadoId, TipoPermissao.VISUALIZAR);
            }

            @Test
            void deve_retornar_status_created() {
                assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_compartilhamento_criado() {
                assertNotNull(resposta.getBody());
                assertEquals(TipoPermissao.VISUALIZAR, resposta.getBody().getPermissao());
                assertEquals("Maria Santos", resposta.getBody().getUsuarioNome());
            }

            @Test
            void deve_chamar_service_compartilhar() {
                verify(compartilhamentoService, times(1))
                        .compartilhar(calendarioId, usuarioCompartilhadoId, TipoPermissao.VISUALIZAR);
            }
        }

        @Nested
        class Quando_buscar_por_id {

            ResponseEntity<CompartilhamentoDTO> resposta;

            @BeforeEach
            void setup() {
                when(compartilhamentoService.buscarPorId(compartilhamentoId)).thenReturn(Optional.of(compartilhamento));
                resposta = compartilhamentoController.buscarPorId(compartilhamentoId);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_compartilhamento_encontrado() {
                assertNotNull(resposta.getBody());
                assertEquals(TipoPermissao.VISUALIZAR, resposta.getBody().getPermissao());
            }
        }

        @Nested
        class Quando_listar_por_calendario {

            ResponseEntity<List<CompartilhamentoDTO>> resposta;

            @BeforeEach
            void setup() {
                when(compartilhamentoService.listarPorCalendario(calendarioId))
                        .thenReturn(Arrays.asList(compartilhamento));
                resposta = compartilhamentoController.listarPorCalendario(calendarioId);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_compartilhamentos_do_calendario() {
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().size());
            }
        }

        @Nested
        class Quando_listar_calendarios_compartilhados {

            ResponseEntity<List<CalendarioDTO>> resposta;

            @BeforeEach
            void setup() {
                when(compartilhamentoService.listarCalendariosCompartilhados(usuarioCompartilhadoId))
                        .thenReturn(Arrays.asList(calendario));
                resposta = compartilhamentoController.listarCalendariosCompartilhados(usuarioCompartilhadoId);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_calendarios_compartilhados() {
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().size());
                assertEquals("Trabalho", resposta.getBody().get(0).getNome());
            }
        }

        @Nested
        class Quando_listar_compartilhamentos_recebidos {

            ResponseEntity<List<CompartilhamentoDTO>> resposta;

            @BeforeEach
            void setup() {
                when(compartilhamentoService.listarCompartilhamentosRecebidos(usuarioCompartilhadoId))
                        .thenReturn(Arrays.asList(compartilhamento));
                resposta = compartilhamentoController.listarCompartilhamentosRecebidos(usuarioCompartilhadoId);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_compartilhamentos_recebidos() {
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().size());
            }
        }

        @Nested
        class Quando_buscar_compartilhamento_especifico {

            ResponseEntity<CompartilhamentoDTO> resposta;

            @BeforeEach
            void setup() {
                when(compartilhamentoService.buscarCompartilhamento(calendarioId, usuarioCompartilhadoId))
                        .thenReturn(Optional.of(compartilhamento));
                resposta = compartilhamentoController.buscarCompartilhamento(calendarioId, usuarioCompartilhadoId);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_compartilhamento_encontrado() {
                assertNotNull(resposta.getBody());
            }
        }

        @Nested
        class Quando_atualizar_permissao {

            ResponseEntity<CompartilhamentoDTO> resposta;

            @BeforeEach
            void setup() {
                compartilhamento.setPermissao(TipoPermissao.EDITAR);
                when(compartilhamentoService.atualizarPermissao(compartilhamentoId, TipoPermissao.EDITAR))
                        .thenReturn(compartilhamento);
                resposta = compartilhamentoController.atualizarPermissao(compartilhamentoId, TipoPermissao.EDITAR);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_permissao_atualizada() {
                assertNotNull(resposta.getBody());
                assertEquals(TipoPermissao.EDITAR, resposta.getBody().getPermissao());
            }

            @Test
            void deve_chamar_service_atualizar_permissao() {
                verify(compartilhamentoService, times(1))
                        .atualizarPermissao(compartilhamentoId, TipoPermissao.EDITAR);
            }
        }

        @Nested
        class Quando_remover_compartilhamento {

            ResponseEntity<Void> resposta;

            @BeforeEach
            void setup() {
                doNothing().when(compartilhamentoService).removerCompartilhamento(calendarioId, usuarioCompartilhadoId);
                resposta = compartilhamentoController.removerCompartilhamento(calendarioId, usuarioCompartilhadoId);
            }

            @Test
            void deve_retornar_status_no_content() {
                assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
            }

            @Test
            void deve_chamar_service_remover() {
                verify(compartilhamentoService, times(1))
                        .removerCompartilhamento(calendarioId, usuarioCompartilhadoId);
            }
        }

        @Nested
        class Quando_deletar_compartilhamento {

            ResponseEntity<Void> resposta;

            @BeforeEach
            void setup() {
                doNothing().when(compartilhamentoService).deletar(compartilhamentoId);
                resposta = compartilhamentoController.deletar(compartilhamentoId);
            }

            @Test
            void deve_retornar_status_no_content() {
                assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
            }

            @Test
            void deve_chamar_service_deletar() {
                verify(compartilhamentoService, times(1)).deletar(compartilhamentoId);
            }
        }

        @Nested
        class Quando_verificar_acesso {

            ResponseEntity<Boolean> resposta;

            @BeforeEach
            void setup() {
                when(compartilhamentoService.temAcesso(calendarioId, usuarioCompartilhadoId)).thenReturn(true);
                resposta = compartilhamentoController.temAcesso(calendarioId, usuarioCompartilhadoId);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_true_quando_tem_acesso() {
                assertTrue(resposta.getBody());
            }
        }

        @Nested
        class Quando_verificar_permissao_edicao {

            ResponseEntity<Boolean> resposta;

            @BeforeEach
            void setup() {
                when(compartilhamentoService.podeEditar(calendarioId, usuarioCompartilhadoId)).thenReturn(true);
                resposta = compartilhamentoController.podeEditar(calendarioId, usuarioCompartilhadoId);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_true_quando_pode_editar() {
                assertTrue(resposta.getBody());
            }
        }

        @Nested
        class Quando_contar_por_calendario {

            ResponseEntity<Long> resposta;

            @BeforeEach
            void setup() {
                when(compartilhamentoService.contarPorCalendario(calendarioId)).thenReturn(3L);
                resposta = compartilhamentoController.contarPorCalendario(calendarioId);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_quantidade_correta() {
                assertEquals(3L, resposta.getBody());
            }
        }
    }

    @Nested
    class Dado_um_compartilhamento_inexistente {

        UUID compartilhamentoId;

        @BeforeEach
        void setup() {
            compartilhamentoId = UUID.randomUUID();
        }

        @Nested
        class Quando_buscar_por_id {

            ResponseEntity<CompartilhamentoDTO> resposta;

            @BeforeEach
            void setup() {
                when(compartilhamentoService.buscarPorId(compartilhamentoId)).thenReturn(Optional.empty());
                resposta = compartilhamentoController.buscarPorId(compartilhamentoId);
            }

            @Test
            void deve_retornar_status_not_found() {
                assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
            }

            @Test
            void nao_deve_retornar_corpo() {
                assertNull(resposta.getBody());
            }
        }

        @Nested
        class Quando_deletar_compartilhamento {

            ResponseEntity<Void> resposta;

            @BeforeEach
            void setup() {
                doThrow(new IllegalArgumentException("Compartilhamento não encontrado"))
                        .when(compartilhamentoService).deletar(compartilhamentoId);
                resposta = compartilhamentoController.deletar(compartilhamentoId);
            }

            @Test
            void deve_retornar_status_not_found() {
                assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
            }
        }
    }

    @Nested
    class Dado_um_calendario_ja_compartilhado {

        UUID calendarioId;
        UUID usuarioId;

        @BeforeEach
        void setup() {
            calendarioId = UUID.randomUUID();
            usuarioId = UUID.randomUUID();
        }

        @Nested
        class Quando_tentar_compartilhar_novamente {

            ResponseEntity<CompartilhamentoDTO> resposta;

            @BeforeEach
            void setup() {
                when(compartilhamentoService.compartilhar(calendarioId, usuarioId, TipoPermissao.VISUALIZAR))
                        .thenThrow(new IllegalArgumentException("Calendário já compartilhado com este usuário"));
                resposta = compartilhamentoController.compartilhar(calendarioId, usuarioId, TipoPermissao.VISUALIZAR);
            }

            @Test
            void deve_retornar_status_bad_request() {
                assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
            }
        }
    }

    @Nested
    class Dado_compartilhamento_com_proprietario {

        UUID calendarioId;
        UUID donoId;

        @BeforeEach
        void setup() {
            calendarioId = UUID.randomUUID();
            donoId = UUID.randomUUID();
        }

        @Nested
        class Quando_tentar_compartilhar_consigo_mesmo {

            ResponseEntity<CompartilhamentoDTO> resposta;

            @BeforeEach
            void setup() {
                when(compartilhamentoService.compartilhar(calendarioId, donoId, TipoPermissao.VISUALIZAR))
                        .thenThrow(new IllegalArgumentException("Não é possível compartilhar o calendário consigo mesmo"));
                resposta = compartilhamentoController.compartilhar(calendarioId, donoId, TipoPermissao.VISUALIZAR);
            }

            @Test
            void deve_retornar_status_bad_request() {
                assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
            }
        }
    }
}