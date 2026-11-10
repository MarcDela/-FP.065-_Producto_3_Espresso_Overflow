package Espresso.Overflow.Libreria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    private String telefono;

    @Column(nullable = false)
    private String rol; // Quitamos el = "USER" de aquí para no enfadar a Lombok

    @PrePersist
    public void prePersist() {
        if (this.rol == null) {
            this.rol = "USER";
        }
    }
    
    @Column(nullable = false)
    private String password;
}
