package Espresso.Overflow.Libreria.repository;

import Espresso.Overflow.Libreria.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // BComo el registro de la contraseña estña encriptada se utiliza el mçetodo findByEmail de Spring Security
    Optional<Usuario> findByEmail(String email);
}