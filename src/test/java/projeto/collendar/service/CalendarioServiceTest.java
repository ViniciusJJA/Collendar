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
import projeto.collendar.dtos.request.CalendarioRequestDTO;
import projeto.collendar.dtos.response.CalendarioResponseDTO;
import projeto.collendar.exception.ResourceNotFoundException;
import projeto.collendar.model.Calendario;
import projeto.collendar.model.Usuario;
import projeto.collendar.repository.CalendarioRepository;

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
    private UsuarioService usuarioService;

    @InjectMocks
    private CalendarioService calendarioService;

    @Nested
    class Dado_um_calendario_valido_para_criar {

        CalendarioRequestDTO dto;
        Usuario usuario;
        UUID usuarioId;

        @BeforeEach
        void setup() {
            usuarioId = UUID.randomUUID();

            dto = new CalendarioRequestDTO(
                    "Trabalho",
                    "Calendário de trabalho",
                    "#FF5733"
            );

            usuario = new Usuario();
            usuario.setId(usuarioId);
            usuario.setNome("João Silva");
            usuario.setEmail("joao@email.com");
        }

        @Nested
        class Quando_criar_calendario {

            CalendarioResponseDTO resultado;

            @BeforeEach
            void setup() {
                when(usuarioService.findEntityById(usuarioId)).thenReturn(usuario);
                when(calendarioRepository.save(any(Calendario.class))).thenAnswer(invocation -> {
                    Calendario calendario = invocation.getArgument(0);
                    calendario.setId(UUID.randomUUID());
                    return calendario;
                });

                resultado = calendarioService.create(dto, usuarioId);
            }

            @Test
            void deve_criar_calendario_com_sucesso() {
                assertNotNull(resultado);
                assertEquals("Trabalho", resultado.nome());
                assertEquals("Calendário de trabalho", resultado.descricao());
                assertEquals("#FF5733", resultado.cor());
            }

            @Test
            void deve_associar_usuario_ao_calendario() {
                assertEquals(usuarioId, resultado.usuarioId());
                assertEquals("João Silva", resultado.usuarioNome());
            }

            @Test
            void deve_marcar_como_proprietario() {
                assertTrue(resultado.proprietario());
            }

            @Test
            void deve_salvar_calendario_no_repositorio() {
                verify(calendarioRepository).save(any(Calendario.class));
            }
        }

        @Nested
        class Quando_usuario_nao_existe {

            @BeforeEach
            void setup() {
                when(usuarioService.findEntityById(usuarioId))
                        .thenThrow(new ResourceNotFoundException("Usuário", usuarioId.toString()));
            }

            @Test
            void deve_lancar_resource_not_found_exception() {
                ResourceNotFoundException exception = assertThrows(
                        ResourceNotFoundException.class,
                        () -> calendarioService.create(dto, usuarioId)
                );

                assertTrue(exception.getMessage().contains("Usuário não encontrado"));
            }

            @Test
            void nao_deve_salvar_calendario() {
                assertThrows(ResourceNotFoundException.class,
                        () -> calendarioService.create(dto, usuarioId));
                verify(calendarioRepository, never()).save(any());
            }
        }
    }

    @Nested
    class Dado_um_calendario_existente {

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
        class Quando_buscar_por_id {

            CalendarioResponseDTO resultado;

            @BeforeEach
            void setup() {
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
                resultado = calendarioService.findById(calendarioId);
            }

            @Test
            void deve_retornar_calendario_encontrado() {
                assertNotNull(resultado);
                assertEquals("Trabalho", resultado.nome());
                assertEquals(calendarioId, resultado.id());
            }
        }

        @Nested
        class Quando_buscar_por_id_inexistente {

            @BeforeEach
            void setup() {
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.empty());
            }

            @Test
            void deve_lancar_resource_not_found_exception() {
                ResourceNotFoundException exception = assertThrows(
                        ResourceNotFoundException.class,
                        () -> calendarioService.findById(calendarioId)
                );

                assertTrue(exception.getMessage().contains("Calendário não encontrado"));
            }
        }

        @Nested
        class Quando_listar_todos_calendarios {

            List<CalendarioResponseDTO> resultado;

            @BeforeEach
            void setup() {
                Calendario calendario2 = new Calendario();
                calendario2.setId(UUID.randomUUID());
                calendario2.setNome("Pessoal");
                calendario2.setUsuario(usuario);

                when(calendarioRepository.findAll()).thenReturn(Arrays.asList(calendario, calendario2));
                resultado = calendarioService.listAll();
            }

            @Test
            void deve_retornar_todos_calendarios() {
                assertNotNull(resultado);
                assertEquals(2, resultado.size());
            }
        }

        @Nested
        class Quando_listar_por_usuario {

            List<CalendarioResponseDTO> resultado;

            @BeforeEach
            void setup() {
                when(calendarioRepository.findByUsuarioId(usuarioId))
                        .thenReturn(Arrays.asList(calendario));
                resultado = calendarioService.listByUsuario(usuarioId);
            }

            @Test
            void deve_retornar_calendarios_do_usuario() {
                assertNotNull(resultado);
                assertEquals(1, resultado.size());
                assertEquals(usuarioId, resultado.get(0).usuarioId());
            }

            @Test
            void deve_marcar_todos_como_proprietario() {
                assertTrue(resultado.get(0).proprietario());
            }
        }

        @Nested
        class Quando_listar_por_usuario_paginado {

            Page<CalendarioResponseDTO> resultado;
            Pageable pageable;

            @BeforeEach
            void setup() {
                pageable = PageRequest.of(0, 10);
                Page<Calendario> page = new PageImpl<>(Arrays.asList(calendario));

                when(usuarioService.findEntityById(usuarioId)).thenReturn(usuario);
                when(calendarioRepository.findByUsuario(usuario, pageable)).thenReturn(page);

                resultado = calendarioService.listByUsuarioPaginated(usuarioId, pageable);
            }

            @Test
            void deve_retornar_pagina_de_calendarios() {
                assertNotNull(resultado);
                assertEquals(1, resultado.getTotalElements());
            }
        }

        @Nested
        class Quando_buscar_por_nome {

            Page<CalendarioResponseDTO> resultado;
            Pageable pageable;

            @BeforeEach
            void setup() {
                pageable = PageRequest.of(0, 10);
                Page<Calendario> page = new PageImpl<>(Arrays.asList(calendario));

                when(calendarioRepository.findByNomeContainingIgnoreCase("Trabalho", pageable))
                        .thenReturn(page);

                resultado = calendarioService.searchByNome("Trabalho", pageable);
            }

            @Test
            void deve_retornar_calendarios_encontrados() {
                assertNotNull(resultado);
                assertEquals(1, resultado.getTotalElements());
                assertEquals("Trabalho", resultado.getContent().get(0).nome());
            }
        }

        @Nested
        class Quando_atualizar_calendario {

            CalendarioRequestDTO dtoAtualizado;
            CalendarioResponseDTO resultado;

            @BeforeEach
            void setup() {
                dtoAtualizado = new CalendarioRequestDTO(
                        "Trabalho Atualizado",
                        "Nova descrição",
                        "#00FF00"
                );

                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
                when(calendarioRepository.save(any(Calendario.class))).thenReturn(calendario);

                resultado = calendarioService.update(calendarioId, dtoAtualizado);
            }

            @Test
            void deve_atualizar_calendario_com_sucesso() {
                assertNotNull(resultado);
                verify(calendarioRepository).save(any(Calendario.class));
            }
        }

        @Nested
        class Quando_atualizar_calendario_inexistente {

            CalendarioRequestDTO dtoAtualizado;

            @BeforeEach
            void setup() {
                dtoAtualizado = new CalendarioRequestDTO(
                        "Trabalho Atualizado",
                        "Nova descrição",
                        "#00FF00"
                );

                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.empty());
            }

            @Test
            void deve_lancar_resource_not_found_exception() {
                ResourceNotFoundException exception = assertThrows(
                        ResourceNotFoundException.class,
                        () -> calendarioService.update(calendarioId, dtoAtualizado)
                );

                assertTrue(exception.getMessage().contains("Calendário não encontrado"));
            }
        }

        @Nested
        class Quando_deletar_calendario {

            @BeforeEach
            void setup() {
                when(calendarioRepository.existsById(calendarioId)).thenReturn(true);
                doNothing().when(calendarioRepository).deleteById(calendarioId);

                calendarioService.delete(calendarioId);
            }

            @Test
            void deve_deletar_calendario() {
                verify(calendarioRepository).deleteById(calendarioId);
            }
        }

        @Nested
        class Quando_deletar_calendario_inexistente {

            @BeforeEach
            void setup() {
                when(calendarioRepository.existsById(calendarioId)).thenReturn(false);
            }

            @Test
            void deve_lancar_resource_not_found_exception() {
                ResourceNotFoundException exception = assertThrows(
                        ResourceNotFoundException.class,
                        () -> calendarioService.delete(calendarioId)
                );

                assertTrue(exception.getMessage().contains("Calendário não encontrado"));
            }
        }

        @Nested
        class Quando_verificar_se_eh_proprietario {

            boolean resultado;

            @BeforeEach
            void setup() {
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
                resultado = calendarioService.isOwner(calendarioId, usuarioId);
            }

            @Test
            void deve_retornar_true_quando_eh_proprietario() {
                assertTrue(resultado);
            }
        }

        @Nested
        class Quando_verificar_se_nao_eh_proprietario {

            boolean resultado;
            UUID outroUsuarioId;

            @BeforeEach
            void setup() {
                outroUsuarioId = UUID.randomUUID();
                when(calendarioRepository.findById(calendarioId)).thenReturn(Optional.of(calendario));
                resultado = calendarioService.isOwner(calendarioId, outroUsuarioId);
            }

            @Test
            void deve_retornar_false_quando_nao_eh_proprietario() {
                assertFalse(resultado);
            }
        }

        @Nested
        class Quando_contar_calendarios_por_usuario {

            long resultado;

            @BeforeEach
            void setup() {
                Calendario calendario2 = new Calendario();
                calendario2.setId(UUID.randomUUID());

                when(calendarioRepository.findByUsuarioId(usuarioId))
                        .thenReturn(Arrays.asList(calendario, calendario2));

                resultado = calendarioService.countByUsuario(usuarioId);
            }

            @Test
            void deve_retornar_quantidade_correta() {
                assertEquals(2, resultado);
            }
        }
    }
}