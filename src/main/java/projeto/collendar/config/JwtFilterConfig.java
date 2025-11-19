package projeto.collendar.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import projeto.collendar.utils.JwtUtil;

@Configuration
public class JwtFilterConfig {

    @Bean
    public JwtAuthFilter jwtAuthFilter(UserDetailsService userDetailsService, JwtUtil jwtUtil) {
        return new JwtAuthFilter(userDetailsService, jwtUtil);
    }

}
