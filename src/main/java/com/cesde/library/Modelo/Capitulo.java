package com.cesde.library.Modelo;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "capitulos")
public class Capitulo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_capitulo")
    private Integer numeroCapitulo;

    private String titulo;

    @Column(columnDefinition = "LONGTEXT")
    private String contenido; // Contenido HTML/texto del capítulo

    @Column(name = "es_gratuito")
    private Boolean esGratuito = false;

    @Column(name = "orden")
    private Integer orden; // Para ordenar capítulos

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "libro_id", nullable = false)
    @JsonBackReference
    private Libro libro;

    // Métodos de utilidad
    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}