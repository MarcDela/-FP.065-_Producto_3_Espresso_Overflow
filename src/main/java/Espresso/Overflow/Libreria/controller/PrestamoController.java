package Espresso.Overflow.Libreria.controller;

import Espresso.Overflow.Libreria.model.Libro;
import Espresso.Overflow.Libreria.model.Prestamo;
import Espresso.Overflow.Libreria.model.Usuario;
import Espresso.Overflow.Libreria.repository.LibroRepository;
import Espresso.Overflow.Libreria.repository.PrestamoRepository;
import Espresso.Overflow.Libreria.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/user/prestamos")
public class PrestamoController {

    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;

    public PrestamoController(PrestamoRepository prestamoRepository,
                              LibroRepository libroRepository,
                              UsuarioRepository usuarioRepository) {
        this.prestamoRepository = prestamoRepository;
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // 1. Mostrar la vista del formulario para solicitar un préstamo
    @GetMapping
    public String mostrarFormularioPrestamo(@RequestParam(name = "libroId", required = false) Long libroId,
                                            Model model) {

        if (libroId != null) {
            Libro libroSeleccionado = libroRepository.findById(libroId).orElse(null);
            model.addAttribute("libroSeleccionado", libroSeleccionado);
        }

        List<Libro> librosDisponibles = libroRepository.findByDisponible(true);
        model.addAttribute("librosDisponibles", librosDisponibles);

        return "prestamos";
    }

    // 2. Procesar el guardado de la solicitud de préstamo
    @PostMapping("/guardar")
    public String guardarPrestamo(@RequestParam("libroId") Long libroId,
                                  @RequestParam("semanas") Integer semanas,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {

        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para poder solicitar un préstamo.");
            return "redirect:/user/prestamos";
        }

        Usuario usuarioActivo = usuarioRepository.findByEmail(principal.getName()).orElse(null);
        if (usuarioActivo == null) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
            return "redirect:/user/prestamos";
        }

        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado ID: " + libroId));

        if (!libro.getDisponible()) {
            redirectAttributes.addFlashAttribute("error", "El libro seleccionado ya no está disponible.");
            return "redirect:/user/prestamos";
        }

        int semanasValidadas = Math.min(Math.max(semanas, 1), 4);

        Prestamo prestamo = new Prestamo();
        prestamo.setLibro(libro);
        prestamo.setUsuario(usuarioActivo);
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setFechaDevolucion(LocalDate.now().plusWeeks(semanasValidadas));

        prestamoRepository.save(prestamo);

        libro.setDisponible(false);
        libroRepository.save(libro);

        return "redirect:/user/historial";
    }
}