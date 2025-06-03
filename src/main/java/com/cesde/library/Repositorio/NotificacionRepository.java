package com.cesde.library.Repositorio;

import com.cesde.library.Modelo.Notificaciones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificaciones, Long> {
    List<Notificaciones> findAllByOrderByFechaDesc();
    List<Notificaciones> findByLeidaFalseOrderByFechaDesc();
    List<Notificaciones> findByMensajeId(Long mensajeId);
    Long countByLeidaFalse();
    List<Notificaciones> findByTipoOrderByFechaDesc(String tipo);
}