package Espresso.Overflow.Libreria.repository;

import Espresso.Overflow.Libreria.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Buscar usuario por email y contraseña (para autenticación)
    Optional<Usuario> findByEmailAndPassword(String email, String password);
}