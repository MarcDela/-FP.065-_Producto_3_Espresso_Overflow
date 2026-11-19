package Espresso.Overflow.Libreria.repository;

import Espresso.Overflow.Libreria.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrestamoRepository
        extends JpaRepository<Prestamo, Long> {

    // Obtener los préstamos de un usuario
    List<Prestamo> findByUsuarioId(Long usuarioId);

    // Obtener los préstamos de un libro
    List<Prestamo> findByLibroId(Long libroId);

    // Buscar un préstamo que pertenezca a un usuario concreto
    Optional<Prestamo> findByIdAndUsuarioId(
            Long id, Long usuarioId);
}