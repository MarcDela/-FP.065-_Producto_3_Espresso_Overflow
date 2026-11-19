package Espresso.Overflow.Libreria.controller;

import Espresso.Overflow.Libreria.model.Usuario;
import Espresso.Overflow.Libreria.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@RequestParam String nombre,
                            @RequestParam String apellidos,
                            @RequestParam String email,
                            @RequestParam(required = false) String telefono,
                            @RequestParam String password,
                            @RequestParam String confirmar,
                            RedirectAttributes redirectAttributes) {

        nombre = nombre == null ? "" : nombre.trim();
        apellidos = apellidos == null ? "" : apellidos.trim();
        email = email == null ? "" : email.trim();
        telefono = telefono == null ? "" : telefono.trim();

        if (nombre.isBlank() || apellidos.isBlank() || email.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Nombre, apellidos y email son obligatorios");
            rellenar(redirectAttributes, nombre, apellidos, email, telefono);
            return "redirect:/registro";
        }

        if (!password.equals(confirmar)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
            rellenar(redirectAttributes, nombre, apellidos, email, telefono);
            return "redirect:/registro";
        }

        if (usuarioRepository.findByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Ya existe una cuenta con ese email");
            rellenar(redirectAttributes, nombre, apellidos, email, telefono);
            return "redirect:/registro";
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setEmail(email);
        usuario.setTelefono(telefono.isBlank() ? null : telefono);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol("ROLE_USER");
        usuario.setActivo(true);
        usuario.setFechaRegistro(LocalDate.now());
        usuarioRepository.save(usuario);

        return "redirect:/login?registro";
    }

    private void rellenar(RedirectAttributes redirectAttributes,
                          String nombre,
                          String apellidos,
                          String email,
                          String telefono) {
        redirectAttributes.addFlashAttribute("nombre", nombre);
        redirectAttributes.addFlashAttribute("apellidos", apellidos);
        redirectAttributes.addFlashAttribute("email", email);
        redirectAttributes.addFlashAttribute("telefono", telefono);
    }
}

