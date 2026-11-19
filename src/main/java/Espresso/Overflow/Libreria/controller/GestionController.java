package Espresso.Overflow.Libreria.controller;

import Espresso.Overflow.Libreria.model.Libro;
import Espresso.Overflow.Libreria.model.Prestamo;
import Espresso.Overflow.Libreria.model.Usuario;
import Espresso.Overflow.Libreria.repository.LibroRepository;
import Espresso.Overflow.Libreria.repository.PrestamoRepository;
import Espresso.Overflow.Libreria.repository.UsuarioRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Controller
@RequestMapping("/admin/gestion")
public class GestionController {

    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;
    private final PasswordEncoder passwordEncoder;

    public GestionController(UsuarioRepository usuarioRepository,
                             LibroRepository libroRepository,
                             PrestamoRepository prestamoRepository,
                             PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Spring Security protege todas las rutas de este controlador: solo ADMIN.
    @GetMapping
    public String verPanelGestion(Model model) {
        model.addAttribute("listaUsuarios", usuarioRepository.findAll());
        model.addAttribute("listaLibros", libroRepository.findAll());
        model.addAttribute("listaPrestamos", prestamoRepository.findAll());
        return "gestion";
    }

    // Crear o editar usuarios. Solo se reciben los campos permitidos.
    @PostMapping("/guardar-usuario")
    public String guardarUsuario(
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam("nombre") String nombre,
            @RequestParam("email") String email,
            @RequestParam(name = "telefono", defaultValue = "") String telefono,
            @RequestParam(name = "password", defaultValue = "") String password,
            @RequestParam("rol") String rol,
            Principal principal,
            RedirectAttributes mensajes) {

        nombre = nombre.trim();
        email = email.trim().toLowerCase(Locale.ROOT);
        telefono = telefono.trim();

        if (nombre.isBlank() || nombre.length() > 255
                || email.length() > 255
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || telefono.length() > 255) {
            return error(mensajes, "usuarios", "Comprueba el nombre, correo y teléfono.");
        }
        if (!"ADMIN".equals(rol) && !"USER".equals(rol)) {
            return error(mensajes, "usuarios", "El rol indicado no es válido.");
        }

        Usuario usuario = id == null ? new Usuario()
                : usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            return error(mensajes, "usuarios", "El usuario no existe.");
        }

        // Evitar perder la propia identidad o los permisos de administración.
        if (id != null && usuario.getEmail().equalsIgnoreCase(principal.getName())
                && (!email.equalsIgnoreCase(usuario.getEmail()) || !"ADMIN".equals(rol))) {
            return error(mensajes, "usuarios", "No puedes cambiar tu propio correo ni quitarte el rol ADMIN.");
        }

        Usuario duplicado = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
        if (duplicado != null && !duplicado.getId().equals(id)) {
            return error(mensajes, "usuarios", "El correo electrónico ya está registrado.");
        }

        // En una edición, una contraseña vacía conserva el hash existente.
        if (id == null || !password.isEmpty()) {
            if (password.length() < 8
                    || password.getBytes(StandardCharsets.UTF_8).length > 72) {
                return error(mensajes, "usuarios", "La contraseña debe tener al menos 8 caracteres y un máximo de 72 bytes.");
            }
            usuario.setPassword(passwordEncoder.encode(password));
        }
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setTelefono(telefono);
        usuario.setRol(rol);
        try {
            usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException e) {
            return error(mensajes, "usuarios", "No se ha podido guardar el usuario. Comprueba que el correo sea único.");
        }
        return exito(mensajes, "usuarios", "Usuario guardado correctamente.");
    }

    @PostMapping("/guardar-libro")
    public String guardarLibro(
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam("titulo") String titulo,
            @RequestParam("autor") String autor,
            @RequestParam(name = "linkimagen", defaultValue = "") String linkimagen,
            @RequestParam(name = "descripcion", defaultValue = "") String descripcion,
            RedirectAttributes mensajes) {

        titulo = titulo.trim();
        autor = autor.trim();
        linkimagen = linkimagen.trim();
        if (titulo.isBlank() || titulo.length() > 255
                || autor.isBlank() || autor.length() > 255) {
            return error(mensajes, "libros", "Introduce un título y un autor válidos.");
        }
        if (!linkimagen.isEmpty()) {
            try {
                java.net.URI url = java.net.URI.create(linkimagen);
                if (!("https".equalsIgnoreCase(url.getScheme()) || "http".equalsIgnoreCase(url.getScheme()))
                        || url.getHost() == null) {
                    return error(mensajes, "libros", "La URL de portada debe ser HTTP o HTTPS.");
                }
            } catch (IllegalArgumentException e) {
                return error(mensajes, "libros", "La URL de portada no es válida.");
            }
        }

        Libro libro = id == null ? new Libro() : libroRepository.findById(id).orElse(null);
        if (libro == null) {
            return error(mensajes, "libros", "El libro no existe.");
        }
        libro.setTitulo(titulo);
        libro.setAutor(autor);
        libro.setLinkimagen(linkimagen.isEmpty() ? null : linkimagen);
        libro.setDescripcion(descripcion);
        // Los libros nuevos están disponibles; en edición se conserva el estado.
        if (id == null) {
            libro.setDisponible(true);
        }
        libroRepository.save(libro);
        return exito(mensajes, "libros", "Libro guardado correctamente.");
    }

    @Transactional
    @PostMapping("/nuevo-prestamo")
    public String nuevoPrestamo(@RequestParam("usuarioId") Long usuarioId,
                                @RequestParam("libroId") Long libroId,
                                @RequestParam(name = "semanas", defaultValue = "2") Integer semanas,
                                RedirectAttributes mensajes) {
        if (semanas < 1 || semanas > 4) {
            return error(mensajes, "prestamos", "La duración debe estar entre 1 y 4 semanas.");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        Libro libro = libroRepository.findById(libroId).orElse(null);
        if (usuario == null || libro == null || !Boolean.TRUE.equals(libro.getDisponible())) {
            return error(mensajes, "prestamos", "El usuario no existe o el libro no está disponible.");
        }
        LocalDate hoy = LocalDate.now();
        Prestamo prestamo = new Prestamo();
        prestamo.setUsuario(usuario);
        prestamo.setLibro(libro);
        prestamo.setFechaPrestamo(hoy);
        prestamo.setFechaDevolucion(hoy.plusWeeks(semanas));
        prestamoRepository.save(prestamo);
        libro.setDisponible(false);
        libroRepository.save(libro);
        return exito(mensajes, "prestamos", "Préstamo registrado correctamente.");
    }

    @PostMapping("/editar-prestamo")
    public String editarPrestamo(@RequestParam("id") Long id,
                                 @RequestParam("fechaDevolucion") String fechaDevolucion,
                                 RedirectAttributes mensajes) {
        Prestamo prestamo = prestamoRepository.findById(id).orElse(null);
        if (prestamo == null) {
            return error(mensajes, "prestamos", "El préstamo no existe.");
        }
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(fechaDevolucion);
        } catch (DateTimeParseException e) {
            return error(mensajes, "prestamos", "Introduce una fecha válida.");
        }
        if (fecha.isBefore(prestamo.getFechaPrestamo())) {
            return error(mensajes, "prestamos", "La devolución no puede ser anterior al inicio del préstamo.");
        }
        prestamo.setFechaDevolucion(fecha);
        prestamoRepository.save(prestamo);
        return exito(mensajes, "prestamos", "Fecha de devolución actualizada.");
    }

    @Transactional
    @PostMapping("/eliminar-prestamo/{id}")
    public String eliminarPrestamo(@PathVariable("id") Long id, RedirectAttributes mensajes) {
        Prestamo prestamo = prestamoRepository.findById(id).orElse(null);
        if (prestamo == null) {
            return error(mensajes, "prestamos", "El préstamo no existe.");
        }
        Libro libro = prestamo.getLibro();
        libro.setDisponible(true);
        libroRepository.save(libro);
        prestamoRepository.delete(prestamo);
        return exito(mensajes, "prestamos", "Préstamo finalizado y libro disponible de nuevo.");
    }

    @PostMapping("/eliminar-usuario/{id}")
    public String eliminarUsuario(@PathVariable("id") Long id,
                                  Principal principal, RedirectAttributes mensajes) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            return error(mensajes, "usuarios", "El usuario no existe.");
        }
        if (usuario.getEmail().equalsIgnoreCase(principal.getName())) {
            return error(mensajes, "usuarios", "No puedes eliminar tu propia cuenta de administrador.");
        }
        if (!prestamoRepository.findByUsuarioId(id).isEmpty()) {
            return error(mensajes, "usuarios", "No se puede eliminar un usuario con préstamos activos.");
        }
        try {
            usuarioRepository.delete(usuario);
        } catch (DataIntegrityViolationException e) {
            return error(mensajes, "usuarios", "El usuario tiene registros relacionados y no se puede eliminar.");
        }
        return exito(mensajes, "usuarios", "Usuario eliminado correctamente.");
    }

    @PostMapping("/eliminar-libro/{id}")
    public String eliminarLibro(@PathVariable("id") Long id, RedirectAttributes mensajes) {
        Libro libro = libroRepository.findById(id).orElse(null);
        if (libro == null) {
            return error(mensajes, "libros", "El libro no existe.");
        }
        if (!Boolean.TRUE.equals(libro.getDisponible()) || !prestamoRepository.findByLibroId(id).isEmpty()) {
            return error(mensajes, "libros", "No se puede eliminar un libro con préstamos activos.");
        }
        try {
            libroRepository.delete(libro);
        } catch (DataIntegrityViolationException e) {
            return error(mensajes, "libros", "El libro tiene registros relacionados y no se puede eliminar.");
        }
        return exito(mensajes, "libros", "Libro eliminado correctamente.");
    }

    private String error(RedirectAttributes mensajes, String tab, String texto) {
        mensajes.addFlashAttribute("error", texto);
        return "redirect:/admin/gestion?tab=" + tab;
    }

    private String exito(RedirectAttributes mensajes, String tab, String texto) {
        mensajes.addFlashAttribute("success", texto);
        return "redirect:/admin/gestion?tab=" + tab;
    }
}