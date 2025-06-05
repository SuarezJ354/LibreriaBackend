package com.cesde.library.Modelo;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Set;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String autor;
    private String anioPublicacion;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "imagen_portada")
    private String imagenPortada; // URL de la portada

    // Configuración de acceso
    @Column(name = "es_gratuito")
    private Boolean esGratuito = false; // Si todo el libro es gratis

    @Column(name = "precio_descarga")
    private BigDecimal precioDescarga; // Precio para descargar PDF

    @Column(name = "capitulos_gratis")
    private Integer capitulosGratis = 3; // Cantidad de capítulos gratuitos

    @Column(name = "total_capitulos")
    private Integer totalCapitulos;

    @Column(name = "archivo_pdf")
    private String archivoPdf; // Ruta del PDF completo

    @Column(name = "estado")
    @Enumerated(EnumType.STRING)
    private EstadoLibro estado = EstadoLibro.Disponible;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // Relación con capítulos
    @OneToMany(mappedBy = "libro", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Capitulo> capitulos;

    // Enum para estados
       public enum EstadoLibro {
        Disponible, No_disponible, Reservado
    }

    @ManyToMany(mappedBy = "librosFavoritos")
    @JsonIgnore
    private Set<Usuario> usuariosQueFavoritan;

}