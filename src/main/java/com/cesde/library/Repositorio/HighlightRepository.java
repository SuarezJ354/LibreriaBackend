package com.cesde.library.Repositorio;

import com.cesde.library.Modelo.Highlight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HighlightRepository extends JpaRepository<Highlight, Long> {

    // Obtener todos los resaltados de un usuario para un capítulo específico
    List<Highlight> findByUsuarioIdAndCapituloIdOrderByStartOffsetAsc(Long usuarioId, Long capituloId);

    // Obtener todos los resaltados de un usuario
    List<Highlight> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId);

    // Obtener un resaltado específico de un usuario
    Optional<Highlight> findByIdAndUsuarioId(Long id, Long usuarioId);

    // Contar resaltados por usuario y capítulo
    long countByUsuarioIdAndCapituloId(Long usuarioId, Long capituloId);

    // Obtener resaltados que se superponen (para validación)
    @Query("SELECT h FROM Highlight h WHERE h.usuarioId = :usuarioId AND h.capituloId = :capituloId " +
            "AND ((h.startOffset <= :startOffset AND h.endOffset > :startOffset) " +
            "OR (h.startOffset < :endOffset AND h.endOffset >= :endOffset) " +
            "OR (h.startOffset >= :startOffset AND h.endOffset <= :endOffset))")
    List<Highlight> findOverlappingHighlights(@Param("usuarioId") Long usuarioId,
                                              @Param("capituloId") Long capituloId,
                                              @Param("startOffset") Integer startOffset,
                                              @Param("endOffset") Integer endOffset);

    // Eliminar todos los resaltados de un capítulo (para limpieza)
    void deleteByCapituloId(Long capituloId);

    // Eliminar todos los resaltados de un usuario (para limpieza)
    void deleteByUsuarioId(Long usuarioId);
}