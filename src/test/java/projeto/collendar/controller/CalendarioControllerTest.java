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
import projeto.collendar.dto.CalendarioDTO;
import projeto.collendar.model.Calendario;
import projeto.collendar.model.Usuario;
import projeto.collendar.service.CalendarioService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CalendarioControllerTest {

    @Mock
    private CalendarioService calendarioService;

    @InjectMocks
    private CalendarioController calendarioController;

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

            ResponseEntity<CalendarioDTO> resposta;

            @BeforeEach
            void setup() {
                when(calendarioService.criar(any(Calendario.class), eq(usuarioId))).thenReturn(calendario);
                resposta = calendarioController.criar(calendario, usuarioId);
            }

            @Test
            void deve_retornar_status_created() {
                assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_calendario_criado() {
                assertNotNull(resposta.getBody());
                assertEquals("Trabalho", resposta.getBody().getNome());
                assertEquals("#FF5733", resposta.getBody().getCor());
            }

            @Test
            void deve_chamar_service_criar() {
                verify(calendarioService, times(1)).criar(any(Calendario.class), eq(usuarioId));
            }
        }

        @Nested
        class Quando_buscar_por_id {

            ResponseEntity<CalendarioDTO> resposta;

            @BeforeEach
            void setup() {
                when(calendarioService.buscarPorId(calendarioId)).thenReturn(Optional.of(calendario));
                resposta = calendarioController.buscarPorId(calendarioId);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_calendario_encontrado() {
                assertNotNull(resposta.getBody());
                assertEquals("Trabalho", resposta.getBody().getNome());
            }
        }

        @Nested
        class Quando_listar_por_usuario {

            ResponseEntity<List<CalendarioDTO>> resposta;

            @BeforeEach
            void setup() {
                when(calendarioService.listarPorUsuario(usuarioId)).thenReturn(Arrays.asList(calendario));
                resposta = calendarioController.listarPorUsuario(usuarioId);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_calendarios_do_usuario() {
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().size());
                assertEquals("Trabalho", resposta.getBody().get(0).getNome());
            }
        }

        @Nested
        class Quando_listar_por_usuario_paginado {

            ResponseEntity<Page<CalendarioDTO>> resposta;
            Pageable pageable;

            @BeforeEach
            void setup() {
                pageable = PageRequest.of(0, 10);
                Page<Calendario> page = new PageImpl<>(Arrays.asList(calendario));

                when(calendarioService.listarPorUsuarioPaginado(usuarioId, pageable)).thenReturn(page);
                resposta = calendarioController.listarPorUsuarioPaginado(usuarioId, pageable);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_pagina_com_calendarios() {
                assertNotNull(resposta.getBody());
                assertEquals(1, resposta.getBody().getTotalElements());
            }
        }

        @Nested
        class Quando_atualizar_calendario {

            ResponseEntity<CalendarioDTO> resposta;
            Calendario calendarioAtualizado;

            @BeforeEach
            void setup() {
                calendarioAtualizado = new Calendario();
                calendarioAtualizado.setNome("Trabalho Atualizado");
                calendarioAtualizado.setDescricao("Nova descrição");
                calendarioAtualizado.setCor("#00FF00");

                when(calendarioService.atualizar(eq(calendarioId), any(Calendario.class))).thenReturn(calendario);
                resposta = calendarioController.atualizar(calendarioId, calendarioAtualizado);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_chamar_service_atualizar() {
                verify(calendarioService, times(1)).atualizar(eq(calendarioId), any(Calendario.class));
            }
        }

        @Nested
        class Quando_deletar_calendario {

            ResponseEntity<Void> resposta;

            @BeforeEach
            void setup() {
                doNothing().when(calendarioService).deletar(calendarioId);
                resposta = calendarioController.deletar(calendarioId);
            }

            @Test
            void deve_retornar_status_no_content() {
                assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
            }

            @Test
            void deve_chamar_service_deletar() {
                verify(calendarioService, times(1)).deletar(calendarioId);
            }
        }

        @Nested
        class Quando_verificar_proprietario {

            ResponseEntity<Boolean> resposta;

            @BeforeEach
            void setup() {
                when(calendarioService.verificarProprietario(calendarioId, usuarioId)).thenReturn(true);
                resposta = calendarioController.verificarProprietario(calendarioId, usuarioId);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_true_quando_eh_proprietario() {
                assertTrue(resposta.getBody());
            }
        }

        @Nested
        class Quando_contar_por_usuario {

            ResponseEntity<Long> resposta;

            @BeforeEach
            void setup() {
                when(calendarioService.contarPorUsuario(usuarioId)).thenReturn(5L);
                resposta = calendarioController.contarPorUsuario(usuarioId);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_quantidade_correta() {
                assertEquals(5L, resposta.getBody());
            }
        }
    }

    @Nested
    class Dado_um_calendario_inexistente {

        UUID calendarioId;

        @BeforeEach
        void setup() {
            calendarioId = UUID.randomUUID();
        }

        @Nested
        class Quando_buscar_por_id {

            ResponseEntity<CalendarioDTO> resposta;

            @BeforeEach
            void setup() {
                when(calendarioService.buscarPorId(calendarioId)).thenReturn(Optional.empty());
                resposta = calendarioController.buscarPorId(calendarioId);
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
        class Quando_deletar_calendario {

            ResponseEntity<Void> resposta;

            @BeforeEach
            void setup() {
                doThrow(new IllegalArgumentException("Calendário não encontrado"))
                        .when(calendarioService).deletar(calendarioId);
                resposta = calendarioController.deletar(calendarioId);
            }

            @Test
            void deve_retornar_status_not_found() {
                assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
            }
        }
    }

    @Nested
    class Dado_um_usuario_inexistente {

        Calendario calendario;
        UUID usuarioId;

        @BeforeEach
        void setup() {
            usuarioId = UUID.randomUUID();
            calendario = new Calendario();
            calendario.setNome("Teste");
        }

        @Nested
        class Quando_criar_calendario {

            ResponseEntity<CalendarioDTO> resposta;

            @BeforeEach
            void setup() {
                when(calendarioService.criar(any(Calendario.class), eq(usuarioId)))
                        .thenThrow(new IllegalArgumentException("Usuário não encontrado"));
                resposta = calendarioController.criar(calendario, usuarioId);
            }

            @Test
            void deve_retornar_status_bad_request() {
                assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
            }
        }
    }

    @Nested
    class Quando_buscar_por_nome {

        Calendario calendario;
        ResponseEntity<Page<CalendarioDTO>> resposta;
        Pageable pageable;

        @BeforeEach
        void setup() {
            pageable = PageRequest.of(0, 10);

            Usuario usuario = new Usuario();
            usuario.setId(UUID.randomUUID());
            usuario.setNome("João");

            calendario = new Calendario();
            calendario.setId(UUID.randomUUID());
            calendario.setNome("Trabalho");
            calendario.setUsuario(usuario);

            Page<Calendario> page = new PageImpl<>(Arrays.asList(calendario));

            when(calendarioService.buscarPorNome("Trabalho", pageable)).thenReturn(page);
            resposta = calendarioController.buscarPorNome("Trabalho", pageable);
        }

        @Test
        void deve_retornar_status_ok() {
            assertEquals(HttpStatus.OK, resposta.getStatusCode());
        }

        @Test
        void deve_retornar_calendarios_encontrados() {
            assertNotNull(resposta.getBody());
            assertEquals(1, resposta.getBody().getTotalElements());
            assertEquals("Trabalho", resposta.getBody().getContent().get(0).getNome());
        }
    }

    @Nested
    class Quando_listar_todos_calendarios {

        List<Calendario> calendarios;
        ResponseEntity<List<CalendarioDTO>> resposta;

        @BeforeEach
        void setup() {
            Usuario usuario = new Usuario();
            usuario.setId(UUID.randomUUID());
            usuario.setNome("João");

            Calendario calendario1 = new Calendario();
            calendario1.setId(UUID.randomUUID());
            calendario1.setNome("Trabalho");
            calendario1.setUsuario(usuario);

            Calendario calendario2 = new Calendario();
            calendario2.setId(UUID.randomUUID());
            calendario2.setNome("Pessoal");
            calendario2.setUsuario(usuario);

            calendarios = Arrays.asList(calendario1, calendario2);

            when(calendarioService.listarTodos()).thenReturn(calendarios);
            resposta = calendarioController.listarTodos();
        }

        @Test
        void deve_retornar_status_ok() {
            assertEquals(HttpStatus.OK, resposta.getStatusCode());
        }

        @Test
        void deve_retornar_todos_calendarios() {
            assertNotNull(resposta.getBody());
            assertEquals(2, resposta.getBody().size());
        }
    }
}