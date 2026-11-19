package Espresso.Overflow.Libreria.controller;

import Espresso.Overflow.Libreria.service.UsuarioService;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Mostrar formulario de login
    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    // Mostrar formulario de registro
    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "registro";
    }

    // Procesar el registro
    @PostMapping("/registro")
    public String registrarUsuario(
            @RequestParam("nombre") String nombre,
            @RequestParam("email") String email,
            @RequestParam(value = "telefono", defaultValue = "")
            String telefono,
            @RequestParam("password") String password,
            @RequestParam("confirmarPassword") String confirmarPassword,
            RedirectAttributes redirectAttributes) {

        try {

            usuarioService.registrarUsuario(
                    nombre,
                    email,
                    telefono,
                    password,
                    confirmarPassword
            );

            return "redirect:/login?registro";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorRegistro",
                    e.getMessage()
            );

        } catch (DataIntegrityViolationException e) {

            redirectAttributes.addFlashAttribute(
                    "errorRegistro",
                    "No se ha podido registrar el usuario. Comprueba los datos."
            );
        }

        // Conservar los datos introducidos, excepto las contraseñas
        redirectAttributes.addFlashAttribute("nombre", nombre);
        redirectAttributes.addFlashAttribute("email", email);
        redirectAttributes.addFlashAttribute("telefono", telefono);

        return "redirect:/registro";
    }
}