package Espresso.Overflow.Libreria.repository;

import Espresso.Overflow.Libreria.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    
    // Obtener préstamos de un usuario específico
    List<Prestamo> findByUsuarioId(Long usuarioId);

    // Obtener préstamos asignados a un libro concreto
    List<Prestamo> findByLibroId(Long libroId);
}