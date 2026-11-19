package Espresso.Overflow.Libreria.service;

import Espresso.Overflow.Libreria.model.Usuario;
import Espresso.Overflow.Libreria.repository.UsuarioRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(email.trim())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado"));

        String rol = usuario.getRol();

        if (!"ADMIN".equals(rol) && !"USER".equals(rol)) {
            throw new UsernameNotFoundException("Rol no válido");
        }

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .roles(rol)
                .build();
    }
}