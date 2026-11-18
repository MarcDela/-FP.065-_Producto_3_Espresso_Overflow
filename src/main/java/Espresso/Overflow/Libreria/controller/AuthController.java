package Espresso.Overflow.Libreria.controller;

import Espresso.Overflow.Libreria.model.Usuario;
import Espresso.Overflow.Libreria.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/registro")
    public String registrarUsuario(@ModelAttribute Usuario usuario,
                                   RedirectAttributes redirectAttributes) {
        // 1. Comprobar si el email ya existe en la tabla usuarios
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            redirectAttributes.addFlashAttribute("errorRegistro", "Ese correo electrónico ya está registrado.");
            return "redirect:/";
        }

        // 2. Asignar rol por defecto y cifrar contraseña antes de guardar
        usuario.setRol("USER");
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuarioRepository.save(usuario);

        // 3. Redirigir a inicio; el script del navbar abrirá el modal de login con este mensaje
        redirectAttributes.addFlashAttribute("registroExitoso", "Cuenta creada correctamente. Ya puedes iniciar sesión.");
        return "redirect:/";
    }
}