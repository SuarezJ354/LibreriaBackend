// ========== MensajeRepository.java ==========
package com.cesde.library.Repositorio;

import com.cesde.library.Modelo.Mensajes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MensajeRepository extends JpaRepository<Mensajes, Long> {
    List<Mensajes> findAllByOrderByFechaAsc();
    List<Mensajes> findByMensajePadreIdOrderByFechaAsc(Long mensajePadreId);
    List<Mensajes> findByAutorOrderByFechaDesc(String autor);
    // Buscar mensajes por usuario ID ordenados por fecha descendente
    List<Mensajes> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    // Buscar mensaje específico por ID y usuario ID
    Optional<Mensajes> findByIdAndUsuarioId(Long id, Long usuarioId);

    // Verificar si existe un mensaje por ID y usuario ID
    boolean existsByIdAndUsuarioId(Long id, Long usuarioId);

    // Buscar mensajes principales (no respuestas) de un usuario
    List<Mensajes> findByUsuarioIdAndEsRespuestaFalseOrderByFechaDesc(Long usuarioId);

    // Buscar respuestas de un mensaje específico del usuario
    List<Mensajes> findByUsuarioIdAndEsRespuestaTrueAndMensajePadreIdOrderByFechaAsc(Long usuarioId, Long mensajePadreId);

    // Contar mensajes de un usuario
    @Query("SELECT COUNT(m) FROM Mensajes m WHERE m.usuarioId = :usuarioId")
    long countByUsuarioId(@Param("usuarioId") Long usuarioId);

    // Contar respuestas de un usuario
    @Query("SELECT COUNT(m) FROM Mensajes m WHERE m.usuarioId = :usuarioId AND m.esRespuesta = true")
    long countRespuestasByUsuarioId(@Param("usuarioId") Long usuarioId);
}

