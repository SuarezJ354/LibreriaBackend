package com.cesde.library.Modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes")
public class Mensajes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(nullable = false, length = 100)
    private String autor;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "es_respuesta")
    private Boolean esRespuesta = false;

    @Column(name = "mensaje_padre_id")
    private Long mensajePadreId;

    // Constructores
    public Mensajes() {}

    public Mensajes(String contenido, String autor) {
        this.contenido = contenido;
        this.autor = autor;
        this.fecha = LocalDateTime.now();
        this.esRespuesta = false;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Boolean getEsRespuesta() {
        return esRespuesta;
    }

    public void setEsRespuesta(Boolean esRespuesta) {
        this.esRespuesta = esRespuesta;
    }

    public Long getMensajePadreId() {
        return mensajePadreId;
    }

    public void setMensajePadreId(Long mensajePadreId) {
        this.mensajePadreId = mensajePadreId;
    }
}