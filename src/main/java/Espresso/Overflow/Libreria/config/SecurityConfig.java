package Espresso.Overflow.Libreria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .authorizeHttpRequests(auth -> auth

                // Rutas públicas
                .requestMatchers(
                    "/",
                    "/login",
                    "/registro",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                ).permitAll()

                // Rutas exclusivas para administradores
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // Rutas para usuarios autenticados
                .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")

                // Bloquear rutas no contempladas
                .anyRequest().denyAll()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/iniciar-sesion")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/cerrar-sesion")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}