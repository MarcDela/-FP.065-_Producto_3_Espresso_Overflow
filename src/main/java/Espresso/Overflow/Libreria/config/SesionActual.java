package Espresso.Overflow.Libreria.config;

import Espresso.Overflow.Libreria.model.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SesionActual {

    private SesionActual() {
    }

    public static Usuario usuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Usuario usuario)) {
            return null;
        }
        return usuario;
    }
}