package Espresso.Overflow.Libreria.repository;

import Espresso.Overflow.Libreria.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    
    // Buscar libros por disponibilidad (para mostrar en el catálogo)
    List<Libro> findByDisponible(Boolean disponible);

    // Buscar libros por autor/titulo
    @Query(value = "SELECT * FROM libros WHERE LOWER(titulo) LIKE LOWER(CONCAT('%', :texto, '%')) OR LOWER(autor) LIKE LOWER(CONCAT('%', :texto, '%'))", nativeQuery = true)
    List<Libro> buscarPorTexto(@Param("texto") String texto);
}