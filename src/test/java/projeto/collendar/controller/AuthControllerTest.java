package projeto.collendar.controller;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import projeto.collendar.dtos.request.LoginRequestDTO;
import projeto.collendar.dtos.response.LoginResponseDTO;
import projeto.collendar.model.Role;
import projeto.collendar.model.Usuario;
import projeto.collendar.repository.UsuarioRepository;
import projeto.collendar.utils.JwtUtil;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AuthController authController;

    @Nested
    class Dado_uma_requisicao_de_login {

        LoginRequestDTO dto;
        String email;
        String senha;

        @BeforeEach
        void setup() {
            email = "joao@email.com";
            senha = "senha123";

            dto = new LoginRequestDTO(email, senha);
        }

        @Nested
        class Quando_credenciais_validas {

            UUID usuarioId;
            Usuario usuario;
            UserDetails userDetails;
            String token;

            @BeforeEach
            void setup() {
                usuarioId = UUID.randomUUID();

                // Setup Usuario
                usuario = new Usuario();
                usuario.setId(usuarioId);
                usuario.setNome("João Silva");
                usuario.setEmail(email);
                usuario.setSenha("senhaEncriptada");

                Role roleUser = new Role();
                roleUser.setNome("USER");

                Role roleAdmin = new Role();
                roleAdmin.setNome("ADMIN");

                usuario.setRoles(Set.of(roleUser, roleAdmin));

                // Setup UserDetails
                userDetails = User.builder()
                        .username(email)
                        .password("senhaEncriptada")
                        .authorities("ROLE_USER", "ROLE_ADMIN")
                        .build();

                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
            }

            @Test
            void deve_realizar_login_com_sucesso() {
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .thenReturn(null);
                when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
                when(jwtUtil.generateToken(userDetails)).thenReturn(token);
                when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

                ResponseEntity<LoginResponseDTO> resposta = authController.login(dto);

                assertEquals(HttpStatus.OK, resposta.getStatusCode());
                assertNotNull(resposta.getBody());
                assertEquals(token, resposta.getBody().token());
                assertEquals("Bearer", resposta.getBody().tipo());
                assertEquals(usuarioId, resposta.getBody().usuarioId());
                assertEquals("João Silva", resposta.getBody().nome());
                assertEquals(email, resposta.getBody().email());
                assertTrue(resposta.getBody().roles().contains("USER"));
                assertTrue(resposta.getBody().roles().contains("ADMIN"));
            }

            @Test
            void deve_chamar_authentication_manager() {
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .thenReturn(null);
                when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
                when(jwtUtil.generateToken(userDetails)).thenReturn(token);
                when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

                authController.login(dto);

                verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            }

            @Test
            void deve_gerar_token_jwt() {
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .thenReturn(null);
                when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
                when(jwtUtil.generateToken(userDetails)).thenReturn(token);
                when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

                authController.login(dto);

                verify(jwtUtil).generateToken(userDetails);
            }

            @Test
            void deve_carregar_detalhes_do_usuario() {
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .thenReturn(null);
                when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
                when(jwtUtil.generateToken(userDetails)).thenReturn(token);
                when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

                authController.login(dto);

                verify(userDetailsService).loadUserByUsername(email);
                verify(usuarioRepository).findByEmail(email);
            }
        }

        @Nested
        class Quando_credenciais_invalidas {

            @Test
            void deve_retornar_unauthorized() {
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .thenThrow(new BadCredentialsException("Credenciais inválidas"));

                ResponseEntity<LoginResponseDTO> resposta = authController.login(dto);

                assertEquals(HttpStatus.UNAUTHORIZED, resposta.getStatusCode());
                assertNull(resposta.getBody());
            }

            @Test
            void nao_deve_gerar_token() {
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .thenThrow(new BadCredentialsException("Credenciais inválidas"));

                authController.login(dto);

                verify(jwtUtil, never()).generateToken(any());
            }

            @Test
            void nao_deve_buscar_usuario() {
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .thenThrow(new BadCredentialsException("Credenciais inválidas"));

                authController.login(dto);

                verify(usuarioRepository, never()).findByEmail(any());
            }
        }

        @Nested
        class Quando_senha_incorreta {

            @Test
            void deve_retornar_unauthorized() {
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .thenThrow(new BadCredentialsException("Senha incorreta"));

                ResponseEntity<LoginResponseDTO> resposta = authController.login(dto);

                assertEquals(HttpStatus.UNAUTHORIZED, resposta.getStatusCode());
            }
        }

        @Nested
        class Quando_usuario_nao_existe {

            @Test
            void deve_retornar_unauthorized() {
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .thenThrow(new BadCredentialsException("Usuário não encontrado"));

                ResponseEntity<LoginResponseDTO> resposta = authController.login(dto);

                assertEquals(HttpStatus.UNAUTHORIZED, resposta.getStatusCode());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_de_login_com_email_vazio {

        LoginRequestDTO dto;

        @BeforeEach
        void setup() {
            dto = new LoginRequestDTO("", "senha123");
        }

        @Nested
        class Quando_tentar_autenticar {

            @Test
            void deve_lancar_excecao_de_validacao() {
                // A validação seria feita pela anotação @Valid no controller
                // Este teste verifica o comportamento caso a validação não pegue
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .thenThrow(new BadCredentialsException("Email é obrigatório"));

                ResponseEntity<LoginResponseDTO> resposta = authController.login(dto);

                assertEquals(HttpStatus.UNAUTHORIZED, resposta.getStatusCode());
            }
        }
    }

    @Nested
    class Dado_uma_requisicao_de_login_com_senha_vazia {

        LoginRequestDTO dto;

        @BeforeEach
        void setup() {
            dto = new LoginRequestDTO("joao@email.com", "");
        }

        @Nested
        class Quando_tentar_autenticar {

            @Test
            void deve_lancar_excecao_de_validacao() {
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .thenThrow(new BadCredentialsException("Senha é obrigatória"));

                ResponseEntity<LoginResponseDTO> resposta = authController.login(dto);

                assertEquals(HttpStatus.UNAUTHORIZED, resposta.getStatusCode());
            }
        }
    }

    @Nested
    class Dado_usuario_com_multiplas_roles {

        LoginRequestDTO dto;
        Usuario usuario;
        UserDetails userDetails;
        String token;

        @BeforeEach
        void setup() {
            String email = "admin@email.com";
            dto = new LoginRequestDTO(email, "senha123");

            usuario = new Usuario();
            usuario.setId(UUID.randomUUID());
            usuario.setNome("Admin User");
            usuario.setEmail(email);

            Role roleUser = new Role();
            roleUser.setNome("USER");

            Role roleAdmin = new Role();
            roleAdmin.setNome("ADMIN");

            Role roleModerator = new Role();
            roleModerator.setNome("MODERATOR");

            usuario.setRoles(Set.of(roleUser, roleAdmin, roleModerator));

            userDetails = User.builder()
                    .username(email)
                    .password("senhaEncriptada")
                    .authorities("ROLE_USER", "ROLE_ADMIN", "ROLE_MODERATOR")
                    .build();

            token = "token.jwt.test";
        }

        @Nested
        class Quando_fazer_login {

            @Test
            void deve_retornar_todas_as_roles() {
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .thenReturn(null);
                when(userDetailsService.loadUserByUsername(usuario.getEmail())).thenReturn(userDetails);
                when(jwtUtil.generateToken(userDetails)).thenReturn(token);
                when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

                ResponseEntity<LoginResponseDTO> resposta = authController.login(dto);

                assertNotNull(resposta.getBody());
                assertEquals(3, resposta.getBody().roles().size());
                assertTrue(resposta.getBody().roles().contains("USER"));
                assertTrue(resposta.getBody().roles().contains("ADMIN"));
                assertTrue(resposta.getBody().roles().contains("MODERATOR"));
            }
        }
    }
}