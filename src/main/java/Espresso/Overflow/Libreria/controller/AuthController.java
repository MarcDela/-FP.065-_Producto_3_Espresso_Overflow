package Espresso.Overflow.Libreria.controller;

import Espresso.Overflow.Libreria.model.Usuario;
import Espresso.Overflow.Libreria.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/registro")
    public String registrarUsuario(@ModelAttribute Usuario usuario) {
        // Encriptar la contraseña introducida por el usuario
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        
        if (usuario.getRol() == null || usuario.getRol().isEmpty()) {
            usuario.setRol("USER");
        }

        usuarioRepository.save(usuario);

        // Redirigir al home o abrir el login directamente
        return "redirect:/?registrado=true";
    }

    @PostMapping("/iniciar-sesion")
    public String login(@RequestParam("email") String email,
                        @RequestParam("password") String password,
                        HttpSession session,
                        HttpServletRequest request,
                        RedirectAttributes redirectAttributes) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailAndPassword(email, password);

        if (usuarioOpt.isPresent()) {
            // Guardar usuario en la sesión HTTP
            session.setAttribute("usuarioLogueado", usuarioOpt.get());
        } else {
            // Enviar mensaje de error
            redirectAttributes.addFlashAttribute("errorLogin", "Email o contraseña incorrectos");
        }

        // Redirigir de vuelta a la página previa
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    @GetMapping("/cerrar-sesion")
    public String logout(HttpSession session) {
        session.invalidate(); // Destruir la sesión
        return "redirect:/";
    }
}