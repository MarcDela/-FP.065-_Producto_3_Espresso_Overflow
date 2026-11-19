package Espresso.Overflow.Libreria.repository;

import Espresso.Overflow.Libreria.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar un usuario por su correo electrónico
    Optional<Usuario> findByEmailIgnoreCase(String email);

    // Comprobar si un correo electrónico ya está registrado
    boolean existsByEmailIgnoreCase(String email);

    // Método antiguo, se eliminará al sustituir AuthController
    Optional<Usuario> findByEmailAndPassword(String email, String password);
}