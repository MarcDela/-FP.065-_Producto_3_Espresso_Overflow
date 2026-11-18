package Espresso.Overflow.Libreria.config;

import Espresso.Overflow.Libreria.model.Usuario;
import Espresso.Overflow.Libreria.repository.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. Cifrador de contraseñas (inyectado también en AuthController)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Conexión con tu tabla 'usuarios' de MySQL
    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
        return email -> {
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

            // .roles(...) añade automáticamente el prefijo "ROLE_", encajando con hasRole('ADMIN') en navbar.html
            return User.builder()
                    .username(usuario.getEmail())
                    .password(usuario.getPassword())
                    .roles(usuario.getRol())
                    .build();
        };
    }

    // 3. Reglas de acceso y rutas del modal de login/logout
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
               // Rutas exclusivas para administradores
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Rutas para usuarios autenticados (tanto USER como ADMIN)
                .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                
                // Resto de rutas públicas (catálogo, login, registro, CSS/JS...)
                .requestMatchers("/", "/login", "/registro", "/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                // Si alguien entra a una ruta protegida sin loguearse, redirige a inicio con aviso
                .loginPage("/?loginRequired=true")
                // Ruta exacta del <form th:action="@{/iniciar-sesion}"> del navbar.html
                .loginProcessingUrl("/iniciar-sesion")
                .defaultSuccessUrl("/", true)
                .failureUrl("/?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                // Ruta exacta del <form th:action="@{/cerrar-sesion}"> del navbar.html
                .logoutUrl("/cerrar-sesion")
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }
}