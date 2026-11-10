package Espresso.Overflow.Libreria.controller;

import Espresso.Overflow.Libreria.model.Libro;
import Espresso.Overflow.Libreria.model.Prestamo;
import Espresso.Overflow.Libreria.model.Usuario;
import Espresso.Overflow.Libreria.repository.LibroRepository;
import Espresso.Overflow.Libreria.repository.PrestamoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HistorialController {

    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;

    // Inyectamos ambos repositorios en el constructor
    public HistorialController(PrestamoRepository prestamoRepository, LibroRepository libroRepository) {
        this.prestamoRepository = prestamoRepository;
        this.libroRepository = libroRepository;
    }

    @GetMapping("/historial")
    public String mostrarHistorial(HttpSession session, Model model) {
        Usuario usuarioActivo = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuarioActivo != null) {
            List<Prestamo> prestamosUsuario = prestamoRepository.findByUsuarioId(usuarioActivo.getId());
            model.addAttribute("prestamos", prestamosUsuario);
        } else {
            model.addAttribute("prestamos", List.of());
        }

        return "historial";
    }

    // --- Método para procesar la devolución ---
    @PostMapping("/prestamos/devolver")
    public String devolverPrestamo(@RequestParam("prestamoId") Long prestamoId, HttpSession session) {
        Usuario usuarioActivo = (Usuario) session.getAttribute("usuarioLogueado");

        // Verificamos que haya usuario en sesión por seguridad
        if (usuarioActivo != null) {
            Prestamo prestamo = prestamoRepository.findById(prestamoId).orElse(null);

            if (prestamo != null) {
                // 1. Cambiamos el estado del libro a disponible
                Libro libro = prestamo.getLibro();
                libro.setDisponible(true);
                libroRepository.save(libro);

                // 2. Eliminamos el registro del préstamo
                prestamoRepository.delete(prestamo);
            }
        }

        return "redirect:/historial";
    }
}