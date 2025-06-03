// ========== MensajeRepository.java ==========
package com.cesde.library.Repositorio;

import com.cesde.library.Modelo.Mensajes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensajes, Long> {
    List<Mensajes> findAllByOrderByFechaAsc();
    List<Mensajes> findByMensajePadreIdOrderByFechaAsc(Long mensajePadreId);
    List<Mensajes> findByAutorOrderByFechaDesc(String autor);
}