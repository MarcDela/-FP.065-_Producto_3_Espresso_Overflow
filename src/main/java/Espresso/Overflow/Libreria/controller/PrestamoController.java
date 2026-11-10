package Espresso.Overflow.Libreria.controller;

import Espresso.Overflow.Libreria.model.Libro;
import Espresso.Overflow.Libreria.model.Prestamo;
import Espresso.Overflow.Libreria.model.Usuario;
import Espresso.Overflow.Libreria.repository.LibroRepository;
import Espresso.Overflow.Libreria.repository.PrestamoRepository;
import Espresso.Overflow.Libreria.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class PrestamoController {

    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;

    public PrestamoController(PrestamoRepository prestamoRepository,
                              LibroRepository libroRepository,
                              UsuarioRepository usuarioRepository) {
        this.prestamoRepository = prestamoRepository;
        this.libroRepository = libroRepository;
    }

    // 1. Mostrar la vista del formulario para solicitar un préstamo
    @GetMapping ("/prestamos")
    public String mostrarFormularioPrestamo(@RequestParam(name = "libroId", required = false) Long libroId, Model model) {
        
        // Si el usuario hizo clic en "Solicitar" desde el catálogo, precargamos la info del libro
        if (libroId != null) {
            Libro libroSeleccionado = libroRepository.findById(libroId).orElse(null);
            model.addAttribute("libroSeleccionado", libroSeleccionado);
        }

        // Cargamos todos los libros que están disponibles (disponible = true) para el modal de selección
        List<Libro> librosDisponibles = libroRepository.findByDisponible(true);
        model.addAttribute("librosDisponibles", librosDisponibles);

        return "prestamos";
    }

    // 2. Procesar el guardado de la solicitud de préstamo
    @PostMapping("/guardar")
    public String guardarPrestamo(@RequestParam("libroId") Long libroId,
                                  @RequestParam("semanas") Integer semanas,
                                  UsuarioRepository usuarioRepository,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {

       if (authentication == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para poder solicitar un préstamo.");
            return "redirect:/user/prestamos";
        }

        // Obtener el usuario activo según su email en Spring Security
        String email = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            return "redirect:/user/prestamos";
        }

        Usuario usuarioActivo = usuarioOpt.get();

        // Obtener el libro a prestar
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado ID: " + libroId));

        if (!libro.getDisponible()) {
            redirectAttributes.addFlashAttribute("error", "Este libro ya no está disponible.");
            return "redirect:/user/prestamos";
        }

        // Crear la instancia del nuevo préstamo
        Prestamo prestamo = new Prestamo();
        prestamo.setLibro(libro);
        prestamo.setUsuario(usuarioActivo);
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setFechaDevolucion(LocalDate.now().plusWeeks(semanas));

        // Guardar el préstamo en la base de datos
        prestamoRepository.save(prestamo);

        // Actualizar el estado del libro a no disponible
        libro.setDisponible(false);
        libroRepository.save(libro);

        // Redirigir al historial de préstamos
        return "redirect:/user/historial";
    }
}