package Espresso.Overflow.Libreria.config;

import Espresso.Overflow.Libreria.model.Usuario;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class UsuarioActualAdvice {

    @ModelAttribute("usuarioLogueado")
    public Usuario usuarioLogueado() {
        return SesionActual.usuario();
    }
}
