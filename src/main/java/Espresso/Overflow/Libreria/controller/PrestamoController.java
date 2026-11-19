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
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/user/prestamos")
public class PrestamoController {

    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;

    public PrestamoController(
            PrestamoRepository prestamoRepository,
            LibroRepository libroRepository,
            UsuarioRepository usuarioRepository) {

        this.prestamoRepository = prestamoRepository;
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Mostrar formulario de préstamo
    @GetMapping
    public String mostrarFormularioPrestamo(
            @RequestParam(name = "libroId", required = false)
            Long libroId,
            Model model) {

        if (libroId != null) {

            Libro libroSeleccionado = libroRepository
                    .findById(libroId)
                    .filter(libro ->
                            Boolean.TRUE.equals(libro.getDisponible()))
                    .orElse(null);

            model.addAttribute(
                    "libroSeleccionado", libroSeleccionado);
        }

        List<Libro> librosDisponibles =
                libroRepository.findByDisponible(true);

        model.addAttribute(
                "librosDisponibles", librosDisponibles);

        return "prestamos";
    }

    // Guardar un préstamo del usuario autenticado
    @Transactional
    @PostMapping("/guardar")
    public String guardarPrestamo(
            @RequestParam("libroId") Long libroId,
            @RequestParam("semanas") Integer semanas,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        // Recuperar el usuario autenticado
        Usuario usuarioActivo = usuarioRepository
                .findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Usuario no encontrado"));

        // Validar duración del préstamo
        if (semanas < 1 || semanas > 4) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El préstamo debe durar entre 1 y 4 semanas.");

            return "redirect:/user/prestamos";
        }

        // Buscar el libro
        Libro libro = libroRepository
                .findById(libroId)
                .orElse(null);

        // Comprobar disponibilidad
        if (libro == null ||
                !Boolean.TRUE.equals(libro.getDisponible())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El libro seleccionado no está disponible.");

            return "redirect:/user/prestamos";
        }

        // Crear el préstamo
        Prestamo prestamo = new Prestamo();

        prestamo.setLibro(libro);
        prestamo.setUsuario(usuarioActivo);
        prestamo.setFechaPrestamo(LocalDate.now());

        prestamo.setFechaDevolucion(
                LocalDate.now().plusWeeks(semanas));

        // Guardar el préstamo
        prestamoRepository.save(prestamo);

        // Actualizar disponibilidad del libro
        libro.setDisponible(false);
        libroRepository.save(libro);

        redirectAttributes.addFlashAttribute(
                "success",
                "Préstamo solicitado correctamente.");

        return "redirect:/user/historial";
    }
}