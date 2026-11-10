package Espresso.Overflow.Libreria.controller;

import Espresso.Overflow.Libreria.model.Libro;
import Espresso.Overflow.Libreria.repository.LibroRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
public class HomeController {

    private final LibroRepository libroRepository;

    public HomeController(LibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    // Ruta principal: Carga el catálogo de libros en index.html con filtros
    @GetMapping("/")
    public String index(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false, defaultValue = "false") Boolean soloDisponibles,
            Model model) {

        List<Libro> libros;

        // 1. Si hay texto de búsqueda
        if (buscar != null && !buscar.isBlank()) {
            libros = libroRepository.buscarPorTexto(buscar);
            
            // Si además se activa el filtro de solo disponibles, filtramos la lista obtenida
            if (soloDisponibles) {
                libros = libros.stream()
                        .filter(Libro::getDisponible)
                        .toList();
            }
        } 
        // 2. Si no hay búsqueda pero se solicita solo los disponibles
        else if (soloDisponibles) {
            libros = libroRepository.findByDisponible(true);
        } 
        // 3. Si no hay filtros, mostramos todos
        else {
            libros = libroRepository.findAll();
        }

        model.addAttribute("libros", libros);
        model.addAttribute("buscar", buscar);
        model.addAttribute("soloDisponibles", soloDisponibles);

        return "index";
    }

    // Ruta de pagina de prueba (Producto 1)
    @GetMapping("/test")
    public String helloWorld(Model model) {
        model.addAttribute("mensaje", "¡Bienvenido a la librería! El servidor de Spring Boot está funcionando correctamente.");
        return "Hola_Mundo";
    }
}