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

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user")
public class HistorialController {

    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;

    public HistorialController(PrestamoRepository prestamoRepository,
                               LibroRepository libroRepository,
                               UsuarioRepository usuarioRepository) {
        this.prestamoRepository = prestamoRepository;
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/historial")
    public String mostrarHistorial(Principal principal, Model model) {
        if (principal != null) {
            Usuario usuarioActivo = usuarioRepository.findByEmail(principal.getName()).orElse(null);

            if (usuarioActivo != null) {
                List<Prestamo> prestamosUsuario = prestamoRepository.findByUsuarioId(usuarioActivo.getId());
                model.addAttribute("prestamos", prestamosUsuario);
                return "historial";
            }
        }

        model.addAttribute("prestamos", List.of());
        return "historial";
    }

    // --- Método para procesar la devolución ---
    @PostMapping("/prestamos/devolver")
    public String devolverPrestamo(@RequestParam("prestamoId") Long prestamoId, Principal principal) {
        if (principal != null) {
            Usuario usuarioActivo = usuarioRepository.findByEmail(principal.getName()).orElse(null);
            Prestamo prestamo = prestamoRepository.findById(prestamoId).orElse(null);

            // Verificamos que el préstamo exista y pertenezca al usuario logueado
            if (usuarioActivo != null && prestamo != null
                    && prestamo.getUsuario().getId().equals(usuarioActivo.getId())) {
                // 1. Cambiamos el estado del libro a disponible
                Libro libro = prestamo.getLibro();
                libro.setDisponible(true);
                libroRepository.save(libro);

                // 2. Eliminamos el registro del préstamo
                prestamoRepository.delete(prestamo);
            }
        }

        return "redirect:/user/historial";
    }
}