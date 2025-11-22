package projeto.collendar.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import projeto.collendar.dto.LoginResponseDTO;
import projeto.collendar.model.Usuario;
import projeto.collendar.repository.UsuarioRepository;
import projeto.collendar.utils.JwtUtil;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDto request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getSenha())
            );

            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getLogin());
            final String token = jwtUtil.generateToken(userDetails);

            Usuario usuario = usuarioRepository.findByEmail(request.getLogin())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

            LoginResponseDTO response = new LoginResponseDTO();
            response.setToken(token);
            response.setUsuarioId(usuario.getId());
            response.setNome(usuario.getNome());
            response.setEmail(usuario.getEmail());
            response.setRoles(usuario.getRoles().stream()
                    .map(role -> role.getNome())
                    .collect(Collectors.toSet()));

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
        }
    }
}
