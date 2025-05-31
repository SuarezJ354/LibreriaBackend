package com.cesde.library.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LibroDTO {
    private String titulo;
    private String autor;
    private String anioPublicacion;   // Nota: String aquí
    private String descripcion;
    private String imagenPortada;
    private Boolean esGratuito;
    private BigDecimal precioDescarga;
    private Integer capitulosGratis;
    private Integer totalCapitulos;
    private String estado;
    private Long categoriaId;
}
