package Espresso.Overflow.Libreria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String apellidos;

    @Column(nullable = false, unique = true)
    private String email;

    @ToString.Exclude
    @Column(nullable = false)
    private String password;

    private String telefono;

    @Column(nullable = false)
    private String rol;

    private Boolean activo;

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

    @OneToMany(mappedBy = "usuario")
    @OrderBy("fechaPrestamo DESC")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Prestamo> prestamos = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.rol = normalizarRol(this.rol);
        if (this.activo == null) {
            this.activo = true;
        }
        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDate.now();
        }
    }

    public static String normalizarRol(String rol) {
        if (rol == null || rol.isBlank()) {
            return "ROLE_USER";
        }
        String valor = rol.trim().toUpperCase();
        if (!valor.startsWith("ROLE_")) {
            valor = "ROLE_" + valor;
        }
        return valor;
    }

    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(normalizarRol(this.rol));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(normalizarRol(this.rol)));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo == null || activo;
    }
}
