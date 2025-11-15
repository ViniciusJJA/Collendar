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
import projeto.collendar.model.Role;
import projeto.collendar.repository.RoleRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class RoleControllerTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleController roleController;

    @Nested
    class Dado_uma_requisicao_para_criar_role {

        Role roleParaCriar;

        @BeforeEach
        void setup() {
            roleParaCriar = new Role();
            roleParaCriar.setNome("ADMIN");
        }

        @Nested
        class Quando_role_nao_existe_no_sistema {

            ResponseEntity<Role> resposta;
            Role roleSalva;

            @BeforeEach
            void setup() {
                roleSalva = new Role();
                roleSalva.setId(UUID.randomUUID());
                roleSalva.setNome("ADMIN");

                when(roleRepository.existsByNome("ADMIN")).thenReturn(false);
                when(roleRepository.save(any(Role.class))).thenReturn(roleSalva);

                resposta = roleController.criar(roleParaCriar);
            }

            @Test
            void deve_retornar_status_created() {
                assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_role_criada_com_id() {
                assertNotNull(resposta.getBody());
                assertNotNull(resposta.getBody().getId());
                assertEquals("ADMIN", resposta.getBody().getNome());
            }

            @Test
            void deve_chamar_repositorio_para_salvar() {
                verify(roleRepository, times(1)).save(any(Role.class));
            }
        }

        @Nested
        class Quando_role_ja_existe_no_sistema {

            ResponseEntity<Role> resposta;

            @BeforeEach
            void setup() {
                when(roleRepository.existsByNome("ADMIN")).thenReturn(true);
                resposta = roleController.criar(roleParaCriar);
            }

            @Test
            void deve_retornar_status_bad_request() {
                assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
            }

            @Test
            void nao_deve_salvar_role_duplicada() {
                verify(roleRepository, never()).save(any(Role.class));
            }
        }

        @Nested
        class Quando_ocorre_excecao_ao_salvar {

            ResponseEntity<Role> resposta;

            @BeforeEach
            void setup() {
                when(roleRepository.existsByNome("ADMIN")).thenReturn(false);
                when(roleRepository.save(any(Role.class))).thenThrow(new RuntimeException("Erro de banco"));
                resposta = roleController.criar(roleParaCriar);
            }

            @Test
            void deve_retornar_status_bad_request() {
                assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_buscar_role_por_id {

        UUID idExistente;
        UUID idInexistente;
        Role roleExistente;

        @BeforeEach
        void setup() {
            idExistente = UUID.randomUUID();
            idInexistente = UUID.randomUUID();

            roleExistente = new Role();
            roleExistente.setId(idExistente);
            roleExistente.setNome("USER");
        }

        @Nested
        class Quando_role_existe {

            ResponseEntity<Role> resposta;

            @BeforeEach
            void setup() {
                when(roleRepository.findById(idExistente)).thenReturn(Optional.of(roleExistente));
                resposta = roleController.buscarPorId(idExistente);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_role_encontrada() {
                assertNotNull(resposta.getBody());
                assertEquals(idExistente, resposta.getBody().getId());
                assertEquals("USER", resposta.getBody().getNome());
            }
        }

        @Nested
        class Quando_role_nao_existe {

            ResponseEntity<Role> resposta;

            @BeforeEach
            void setup() {
                when(roleRepository.findById(idInexistente)).thenReturn(Optional.empty());
                resposta = roleController.buscarPorId(idInexistente);
            }

            @Test
            void deve_retornar_status_not_found() {
                assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
            }

            @Test
            void nao_deve_retornar_corpo_na_resposta() {
                assertNull(resposta.getBody());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_buscar_role_por_nome {

        String nomeExistente;
        String nomeInexistente;
        Role roleExistente;

        @BeforeEach
        void setup() {
            nomeExistente = "MODERADOR";
            nomeInexistente = "INEXISTENTE";

            roleExistente = new Role();
            roleExistente.setId(UUID.randomUUID());
            roleExistente.setNome(nomeExistente);
        }

        @Nested
        class Quando_role_existe_com_nome_informado {

            ResponseEntity<Role> resposta;

            @BeforeEach
            void setup() {
                when(roleRepository.findByNome(nomeExistente)).thenReturn(Optional.of(roleExistente));
                resposta = roleController.buscarPorNome(nomeExistente);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_role_com_nome_correto() {
                assertNotNull(resposta.getBody());
                assertEquals(nomeExistente, resposta.getBody().getNome());
            }
        }

        @Nested
        class Quando_role_nao_existe_com_nome_informado {

            ResponseEntity<Role> resposta;

            @BeforeEach
            void setup() {
                when(roleRepository.findByNome(nomeInexistente)).thenReturn(Optional.empty());
                resposta = roleController.buscarPorNome(nomeInexistente);
            }

            @Test
            void deve_retornar_status_not_found() {
                assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_listar_todas_roles {

        List<Role> rolesExistentes;

        @BeforeEach
        void setup() {
            Role role1 = new Role();
            role1.setId(UUID.randomUUID());
            role1.setNome("ADMIN");

            Role role2 = new Role();
            role2.setId(UUID.randomUUID());
            role2.setNome("USER");

            Role role3 = new Role();
            role3.setId(UUID.randomUUID());
            role3.setNome("MODERADOR");

            rolesExistentes = List.of(role1, role2, role3);
        }

        @Nested
        class Quando_existem_roles_cadastradas {

            ResponseEntity<List<Role>> resposta;

            @BeforeEach
            void setup() {
                when(roleRepository.findAll()).thenReturn(rolesExistentes);
                resposta = roleController.listarTodas();
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_todas_roles_cadastradas() {
                assertNotNull(resposta.getBody());
                assertEquals(3, resposta.getBody().size());
            }

            @Test
            void deve_conter_roles_esperadas() {
                List<String> nomesRetornados = resposta.getBody().stream()
                        .map(Role::getNome)
                        .toList();

                assertTrue(nomesRetornados.contains("ADMIN"));
                assertTrue(nomesRetornados.contains("USER"));
                assertTrue(nomesRetornados.contains("MODERADOR"));
            }
        }

        @Nested
        class Quando_nao_existem_roles_cadastradas {

            ResponseEntity<List<Role>> resposta;

            @BeforeEach
            void setup() {
                when(roleRepository.findAll()).thenReturn(List.of());
                resposta = roleController.listarTodas();
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_lista_vazia() {
                assertNotNull(resposta.getBody());
                assertTrue(resposta.getBody().isEmpty());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_deletar_role {

        UUID idExistente;
        UUID idInexistente;

        @BeforeEach
        void setup() {
            idExistente = UUID.randomUUID();
            idInexistente = UUID.randomUUID();
        }

        @Nested
        class Quando_role_existe {

            ResponseEntity<Void> resposta;

            @BeforeEach
            void setup() {
                when(roleRepository.existsById(idExistente)).thenReturn(true);
                doNothing().when(roleRepository).deleteById(idExistente);
                resposta = roleController.deletar(idExistente);
            }

            @Test
            void deve_retornar_status_no_content() {
                assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
            }

            @Test
            void deve_chamar_repositorio_para_deletar() {
                verify(roleRepository, times(1)).deleteById(idExistente);
            }
        }

        @Nested
        class Quando_role_nao_existe {

            ResponseEntity<Void> resposta;

            @BeforeEach
            void setup() {
                when(roleRepository.existsById(idInexistente)).thenReturn(false);
                resposta = roleController.deletar(idInexistente);
            }

            @Test
            void deve_retornar_status_not_found() {
                assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
            }

            @Test
            void nao_deve_tentar_deletar() {
                verify(roleRepository, never()).deleteById(any(UUID.class));
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_para_verificar_existencia_de_role {

        String nomeExistente;
        String nomeInexistente;

        @BeforeEach
        void setup() {
            nomeExistente = "ADMIN";
            nomeInexistente = "ROLE_INEXISTENTE";
        }

        @Nested
        class Quando_role_existe_com_nome_informado {

            ResponseEntity<Boolean> resposta;

            @BeforeEach
            void setup() {
                when(roleRepository.existsByNome(nomeExistente)).thenReturn(true);
                resposta = roleController.verificarExistencia(nomeExistente);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_true() {
                assertNotNull(resposta.getBody());
                assertTrue(resposta.getBody());
            }
        }

        @Nested
        class Quando_role_nao_existe_com_nome_informado {

            ResponseEntity<Boolean> resposta;

            @BeforeEach
            void setup() {
                when(roleRepository.existsByNome(nomeInexistente)).thenReturn(false);
                resposta = roleController.verificarExistencia(nomeInexistente);
            }

            @Test
            void deve_retornar_status_ok() {
                assertEquals(HttpStatus.OK, resposta.getStatusCode());
            }

            @Test
            void deve_retornar_false() {
                assertNotNull(resposta.getBody());
                assertFalse(resposta.getBody());
            }
        }
    }
}