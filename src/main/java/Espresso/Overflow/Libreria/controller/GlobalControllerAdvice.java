package Espresso.Overflow.Libreria.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute
    public void agregarDatosAutenticacion(Model model) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        boolean autenticado = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        boolean esAdmin = autenticado &&
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(rol ->
                                rol.getAuthority().equals("ROLE_ADMIN"));

        model.addAttribute("usuarioAutenticado", autenticado);
        model.addAttribute("esAdmin", esAdmin);

        model.addAttribute(
                "emailAutenticado",
                autenticado ? authentication.getName() : ""
        );
    }
}