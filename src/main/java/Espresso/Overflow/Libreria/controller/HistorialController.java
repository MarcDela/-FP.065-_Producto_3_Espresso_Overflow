package Espresso.Overflow.Libreria.controller;

import Espresso.Overflow.Libreria.config.SesionActual;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HistorialController {

    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;

    public HistorialController(PrestamoRepository prestamoRepository, LibroRepository libroRepository, UsuarioRepository usuarioRepository) {
        this.prestamoRepository = prestamoRepository;
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/user/historial")
    public String mostrarHistorial(Model model) {
        Usuario usuarioActivo = SesionActual.usuario();

        if (usuarioActivo != null) {
            Usuario usuario = usuarioRepository.findById(usuarioActivo.getId()).orElse(usuarioActivo);
            model.addAttribute("prestamos", usuario.getPrestamos());
        } else {
            model.addAttribute("prestamos", List.of());
        }

        return "historial";
    }

    // --- Método para procesar la devolución ---
    @PostMapping("/user/prestamos/devolver")
    public String devolverPrestamo(@RequestParam("prestamoId") Long prestamoId) {
        Usuario usuarioActivo = SesionActual.usuario();

        // Verificamos que haya usuario en sesión por seguridad
        if (usuarioActivo != null) {
            Prestamo prestamo = prestamoRepository.findById(prestamoId).orElse(null);

            boolean esSuyo = prestamo != null
                    && prestamo.getUsuario() != null
                    && usuarioActivo.getId().equals(prestamo.getUsuario().getId());
            if(esSuyo){
                Libro libro = prestamo.getLibro();

                if(libro != null) {
                    libro.setDisponible(true);
                    libroRepository.save(libro);
                }

                // 2. Eliminamos el registro del préstamo
                prestamoRepository.delete(prestamo);
            }
        }

        return "redirect:/user/historial";
    }
}