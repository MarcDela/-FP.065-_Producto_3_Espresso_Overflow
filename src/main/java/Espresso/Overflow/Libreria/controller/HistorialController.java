package Espresso.Overflow.Libreria.controller;

import Espresso.Overflow.Libreria.model.Libro;
import Espresso.Overflow.Libreria.model.Prestamo;
import Espresso.Overflow.Libreria.model.Usuario;
import Espresso.Overflow.Libreria.repository.LibroRepository;
import Espresso.Overflow.Libreria.repository.PrestamoRepository;
import Espresso.Overflow.Libreria.repository.UsuarioRepository;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.AccessDeniedException;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user")
public class HistorialController {

    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;

    public HistorialController(
            PrestamoRepository prestamoRepository,
            LibroRepository libroRepository,
            UsuarioRepository usuarioRepository) {

        this.prestamoRepository = prestamoRepository;
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Mostrar únicamente los préstamos del usuario autenticado
    @GetMapping("/historial")
    public String mostrarHistorial(
            Principal principal,
            Model model) {

        Usuario usuarioActivo = usuarioRepository
                .findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Usuario no encontrado"));

        List<Prestamo> prestamosUsuario =
                prestamoRepository.findByUsuarioId(
                        usuarioActivo.getId());

        model.addAttribute("prestamos", prestamosUsuario);

        return "historial";
    }

    // Devolver un préstamo propio
    @Transactional
    @PostMapping("/prestamos/devolver")
    public String devolverPrestamo(
            @RequestParam("prestamoId") Long prestamoId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioActivo = usuarioRepository
                .findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Usuario no encontrado"));

        // Buscar el préstamo verificando su propietario
        Prestamo prestamo = prestamoRepository
                .findByIdAndUsuarioId(
                        prestamoId,
                        usuarioActivo.getId())
                .orElse(null);

        // Impedir devolver préstamos de otros usuarios
        if (prestamo == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No tienes permiso para devolver este préstamo.");

            return "redirect:/user/historial";
        }

        // Liberar el libro
        Libro libro = prestamo.getLibro();

        libro.setDisponible(true);
        libroRepository.save(libro);

        // Eliminar el préstamo
        prestamoRepository.delete(prestamo);

        redirectAttributes.addFlashAttribute(
                "success",
                "Libro devuelto correctamente.");

        return "redirect:/user/historial";
    }
}