package com.cesde.library.Modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "highlights", indexes = {
        @Index(name = "idx_usuario_capitulo", columnList = "usuario_id, capitulo_id"),
        @Index(name = "idx_capitulo", columnList = "capitulo_id"),
        @Index(name = "idx_usuario", columnList = "usuario_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Highlight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "capitulo_id", nullable = false)
    private Long capituloId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String texto;

    @Column(name = "start_offset", nullable = false)
    private Integer startOffset;

    @Column(name = "end_offset", nullable = false)
    private Integer endOffset;

    @Column(length = 20)
    private String color = "#ffeb3b";

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    @JsonIgnore
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capitulo_id", insertable = false, updatable = false)
    @JsonIgnore
    private Capitulo capitulo;
}