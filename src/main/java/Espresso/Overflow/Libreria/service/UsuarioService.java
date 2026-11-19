package Espresso.Overflow.Libreria.service;

import Espresso.Overflow.Libreria.model.Usuario;
import Espresso.Overflow.Libreria.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.nio.charset.StandardCharsets;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrarUsuario(String nombre,
                                    String email,
                                    String telefono,
                                    String password,
                                    String confirmarPassword) {

        nombre = nombre.trim();
        email = email.trim().toLowerCase(Locale.ROOT);
        telefono = telefono.trim();

        // Validar los datos obligatorios
        if (nombre.isBlank() || nombre.length() > 255) {
            throw new IllegalArgumentException(
                    "Introduce un nombre válido.");
        }

        // Validar el correo electrónico
        if (email.length() > 255 ||
                !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                    "Introduce un correo electrónico válido.");
        }

        if (telefono.length() > 255) {
            throw new IllegalArgumentException(
                    "El teléfono es demasiado largo.");
        }

        // Comprobar correo duplicado
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "El correo electrónico ya está registrado.");
        }

        // Validar la contraseña
        if (password.length() < 8 ||
                password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 8 caracteres y un máximo de 72 bytes.");
        }

        // Comprobar que las contraseñas coinciden
        if (!password.equals(confirmarPassword)) {
            throw new IllegalArgumentException(
                    "Las contraseñas no coinciden.");
        }

        // Crear el usuario
        Usuario usuario = new Usuario();

        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setTelefono(telefono);

        // Generar el hash de la contraseña
        usuario.setPassword(passwordEncoder.encode(password));

        // Todos los registros públicos serán USER
        usuario.setRol("USER");

        return usuarioRepository.saveAndFlush(usuario);
    }
}