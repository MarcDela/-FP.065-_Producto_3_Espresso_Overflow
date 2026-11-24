package Espresso.Overflow.Libreria.controller;

import Espresso.Overflow.Libreria.model.Libro;
import Espresso.Overflow.Libreria.model.Prestamo;
import Espresso.Overflow.Libreria.model.Usuario;
import Espresso.Overflow.Libreria.repository.LibroRepository;
import Espresso.Overflow.Libreria.repository.PrestamoRepository;
import Espresso.Overflow.Libreria.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class GestionController {

    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;
    private final PasswordEncoder passwordEncoder;

    public GestionController(
            UsuarioRepository usuarioRepository,
            LibroRepository libroRepository,
            PrestamoRepository prestamoRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Mostrar Panel de Gestión
    @GetMapping("/gestion")
    public String verPanelGestion(Model model) {
        model.addAttribute("listaUsuarios", usuarioRepository.findAll());
        model.addAttribute("listaLibros", libroRepository.findAll());
        model.addAttribute("listaPrestamos", prestamoRepository.findAll());

        return "gestion";
    }

    // 2. Guardar o Editar Usuario
    @PostMapping("/guardar-usuario")
    public String guardarUsuario(@ModelAttribute Usuario usuario) {
        if (usuario.getId() != null) {
            // Si es edición y la contraseña viene vacía, conservar la clave actual
            Optional<Usuario> uExistente = usuarioRepository.findById(usuario.getId());
            if (uExistente.isPresent() && (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty())) {
                usuario.setPassword(uExistente.get().getPassword());
            } else if (usuario.getPassword() != null && !usuario.getPassword().trim().isEmpty()) {
                // Si introdujeron una contraseña nueva en la edición, encriptarla
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            }
        } else {
            // Usuario nuevo: encriptar contraseña siempre
            if (usuario.getPassword() != null && !usuario.getPassword().trim().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            }
        }

        usuarioRepository.save(usuario);
        return "redirect:/admin/gestion";
    }

    // 3. Guardar o Editar Libro
    @PostMapping("/guardar-libro")
    public String guardarLibro(@ModelAttribute Libro libro) {
        if (libro.getId() == null) {
            // Libro nuevo: disponible por defecto
            libro.setDisponible(true);
        } else {
            // Editar libro: conservar su estado de disponibilidad actual
            Optional<Libro> lExistente = libroRepository.findById(libro.getId());
            lExistente.ifPresent(value -> libro.setDisponible(value.getDisponible()));
        }

        libroRepository.save(libro);
        return "redirect:/admin/gestion";
    }

    // Registrar Nuevo Préstamo (Selección manual de Usuario, Libro y Semanas)
    @PostMapping("/nuevo-prestamo")
    public String nuevoPrestamo(
            @RequestParam Long usuarioId,
            @RequestParam Long libroId,
            @RequestParam(defaultValue = "2") Integer semanas) {

            Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
            Optional<Libro> libroOpt = libroRepository.findById(libroId);

            if (usuarioOpt.isPresent() && libroOpt.isPresent()) {
                Libro libro = libroOpt.get();
                Usuario usuario = usuarioOpt.get();

                if (libro.getDisponible()) {
                    // Validar que las semanas estén entre 1 y 4
                    int semanasValidadas = Math.min(Math.max(semanas, 1), 4);

                    // Crear entidad préstamo
                    Prestamo prestamo = new Prestamo();
                    prestamo.setUsuario(usuario);
                    prestamo.setLibro(libro);
                    prestamo.setFechaPrestamo(LocalDate.now());
                    prestamo.setFechaDevolucion(LocalDate.now().plusWeeks(semanasValidadas));

                    // Marcar libro como no disponible
                    libro.setDisponible(false);

                    libroRepository.save(libro);
                    prestamoRepository.save(prestamo);
                }
            }
        return "redirect:/admin/gestion";
    }

    // 5. Editar Fecha Límite de Préstamo
    @PostMapping("/editar-prestamo")
    public String editarPrestamo(@RequestParam Long id, @RequestParam String fechaDevolucion) {
        Optional<Prestamo> prestamoOpt = prestamoRepository.findById(id);
        if (prestamoOpt.isPresent()) {
            Prestamo prestamo = prestamoOpt.get();
            prestamo.setFechaDevolucion(LocalDate.parse(fechaDevolucion));
            prestamoRepository.save(prestamo);
        }
        return "redirect:/admin/gestion";
    }

    // 6. Finalizar / Eliminar Préstamo (Liberar Libro)
    @GetMapping("/eliminar-prestamo/{id}")
    public String eliminarPrestamo(@PathVariable Long id) {
        Optional<Prestamo> prestamoOpt = prestamoRepository.findById(id);
        if (prestamoOpt.isPresent()) {
            Prestamo prestamo = prestamoOpt.get();
            if (prestamo.getLibro() != null) {
                Libro libro = prestamo.getLibro();
                libro.setDisponible(true);
                libroRepository.save(libro);
            }
            prestamoRepository.deleteById(id);
        }
        return "redirect:/admin/gestion";
    }

    // 7. Eliminar Usuario
    @GetMapping("/eliminar-usuario/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        usuarioRepository.deleteById(id);
        return "redirect:/admin/gestion";
    }

    // 8. Eliminar Libro
    @GetMapping("/eliminar-libro/{id}")
    public String eliminarLibro(@PathVariable Long id) {
        libroRepository.deleteById(id);
        return "redirect:/admin/gestion";
    }
}