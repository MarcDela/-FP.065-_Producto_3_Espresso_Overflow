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

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class HistorialController {

    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;

    // Inyectamos ambos repositorios en el constructor
    public HistorialController(PrestamoRepository prestamoRepository, LibroRepository libroRepository, UsuarioRepository usuarioRepository) {
        this.prestamoRepository = prestamoRepository;
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;

    }

    @GetMapping("/historial")
    public String mostrarHistorial(Authentication authentication, Model model) {
        if (authentication != null) {
            String email = authentication.getName();
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

            if (usuarioOpt.isPresent()) {
                List<Prestamo> prestamosUsuario = prestamoRepository.findByUsuarioId(usuarioOpt.get().getId());
                model.addAttribute("prestamos", prestamosUsuario);
                return "historial";
            }
        }

        model.addAttribute("prestamos", List.of());
        return "historial";
    }

    // --- Método para procesar la devolución ---
    @PostMapping("/prestamos/devolver")
    public String devolverPrestamo(@RequestParam("prestamoId") Long prestamoId) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId).orElse(null);

        if (prestamo != null) {
            // 1. Cambiamos el estado del libro a disponible
            Libro libro = prestamo.getLibro();
            if (libro != null) {
                libro.setDisponible(true);
                libroRepository.save(libro);
            }

            // 2. Eliminamos el registro del préstamo
            prestamoRepository.delete(prestamo);
        }

        return "redirect:/user/historial";
    }
}