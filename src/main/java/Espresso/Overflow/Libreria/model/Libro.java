package Espresso.Overflow.Libreria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "libros")
/*
    Mediante la anotacion @Data, Lombok se encarga de generar de forma automatica en tiempo
    de compilación todos los getters, setters y los metodos equals(), hashCode() y toString()
    
    Mediante la anotación @NoArgsConstructor genera el constructor vacío con todos los
    atributos que exige el estándar JPA
*/
@Data
@NoArgsConstructor
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String autor;

    @Column(name = "link_imagen", columnDefinition = "TEXT")
    private String linkimagen;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private Boolean disponible = true;

    @OneToMany(mappedBy = "libro")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Prestamo> prestamos = new ArrayList<>();
}