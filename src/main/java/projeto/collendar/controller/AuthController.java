package projeto.collendar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import projeto.collendar.dtos.request.LoginRequestDTO;
import projeto.collendar.dtos.response.LoginResponseDTO;
import projeto.collendar.exception.BusinessException;
import projeto.collendar.model.Role;
import projeto.collendar.model.Usuario;
import projeto.collendar.repository.UsuarioRepository;
import projeto.collendar.utils.JwtUtil;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(
        name = "Autenticação",
        description = "Endpoints responsáveis pela autenticação de usuários no sistema. " +
                "Permite realizar login e obter token JWT para acesso aos recursos protegidos."
)
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    @Operation(
            summary = "Realizar login no sistema",
            description = "Autentica um usuário com email e senha, retornando um token JWT para acesso aos endpoints protegidos. " +
                    "O token deve ser incluído no header Authorization como 'Bearer {token}' nas requisições subsequentes.",
            tags = {"Autenticação"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login realizado com sucesso. Retorna o token JWT e informações básicas do usuário.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais inválidas. Email ou senha incorretos.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos. Verifique se email e senha foram fornecidos corretamente.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<LoginResponseDTO> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciais de login do usuário (email e senha)",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequestDTO.class),
                            mediaType = "application/json"
                    )
            )
            @RequestBody @Valid LoginRequestDTO request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.senha())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
            String token = jwtUtil.generateToken(userDetails);

            Usuario usuario = usuarioRepository.findByEmail(request.email())
                    .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

            LoginResponseDTO response = new LoginResponseDTO(
                    token,
                    "Bearer",
                    usuario.getId(),
                    usuario.getNome(),
                    usuario.getEmail(),
                    usuario.getRoles().stream()
                            .map(Role::getNome)
                            .collect(Collectors.toSet())
            );

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}