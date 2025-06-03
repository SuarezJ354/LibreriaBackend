package com.cesde.library.Servicios;

import com.cesde.library.Modelo.Mensajes;
import com.cesde.library.Modelo.Notificaciones;
import com.cesde.library.Repositorio.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    /**
     * Obtiene todas las notificaciones ordenadas por fecha descendente
     */
    public List<Notificaciones> listarTodasNotificaciones() {
        return notificacionRepository.findAllByOrderByFechaDesc();
    }

    /**
     * Obtiene solo las notificaciones no leídas
     */
    public List<Notificaciones> listarNotificacionesNoLeidas() {
        return notificacionRepository.findByLeidaFalseOrderByFechaDesc();
    }

    /**
     * Crea una nueva notificación
     */
    public Notificaciones crearNotificacion(Notificaciones notificacion) {
        // Validaciones básicas
        if (notificacion.getTipo() == null || notificacion.getTipo().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de notificación no puede estar vacío");
        }

        if (notificacion.getTitulo() == null || notificacion.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("El título de la notificación no puede estar vacío");
        }

        if (notificacion.getContenido() == null || notificacion.getContenido().trim().isEmpty()) {
            throw new IllegalArgumentException("El contenido de la notificación no puede estar vacío");
        }

        // Establecer valores por defecto
        if (notificacion.getFecha() == null) {
            notificacion.setFecha(LocalDateTime.now());
        }

        if (notificacion.getLeida() == null) {
            notificacion.setLeida(false);
        }

        return notificacionRepository.save(notificacion);
    }

    /**
     * Crea una notificación automáticamente cuando se crea un nuevo mensaje
     */
    public void crearNotificacionPorMensaje(Mensajes mensaje) {
        try {
            Notificaciones notificacion = new Notificaciones();
            notificacion.setTipo("mensaje_nuevo");
            notificacion.setTitulo("Nuevo mensaje de " + mensaje.getAutor());
            notificacion.setContenido(truncarContenido(mensaje.getContenido(), 200));
            notificacion.setFecha(LocalDateTime.now());
            notificacion.setLeida(false);
            notificacion.setMensajeId(mensaje.getId());

            notificacionRepository.save(notificacion);
        } catch (Exception e) {
            System.err.println("Error al crear notificación para mensaje: " + e.getMessage());
        }
    }

    /**
     * Marca las notificaciones relacionadas con un mensaje como leídas
     */
    public boolean marcarNotificacionesComoLeidas(Long mensajeId) {
        try {
            List<Notificaciones> notificaciones = notificacionRepository.findByMensajeId(mensajeId);

            for (Notificaciones notificacion : notificaciones) {
                notificacion.setLeida(true);
                notificacionRepository.save(notificacion);
            }

            return true;
        } catch (Exception e) {
            System.err.println("Error al marcar notificaciones como leídas: " + e.getMessage());
            return false;
        }
    }

    /**
     * Marca una notificación específica como leída
     */
    public boolean marcarNotificacionComoLeida(Long notificacionId) {
        Optional<Notificaciones> notificacionOpt = notificacionRepository.findById(notificacionId);

        if (notificacionOpt.isPresent()) {
            Notificaciones notificacion = notificacionOpt.get();
            notificacion.setLeida(true);
            notificacionRepository.save(notificacion);
            return true;
        }

        return false;
    }

    /**
     * Elimina una notificación por ID
     */
    public boolean eliminarNotificacion(Long id) {
        if (notificacionRepository.existsById(id)) {
            notificacionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Cuenta las notificaciones no leídas
     */
    public Long contarNotificacionesNoLeidas() {
        return notificacionRepository.countByLeidaFalse();
    }

    /**
     * Obtiene notificaciones por tipo
     */
    public List<Notificaciones> obtenerNotificacionesPorTipo(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo no puede estar vacío");
        }
        return notificacionRepository.findByTipoOrderByFechaDesc(tipo);
    }

    /**
     * Obtiene notificaciones relacionadas con un mensaje específico
     */
    public List<Notificaciones> obtenerNotificacionesPorMensaje(Long mensajeId) {
        return notificacionRepository.findByMensajeId(mensajeId);
    }

    /**
     * Elimina todas las notificaciones leídas
     */
    public int eliminarNotificacionesLeidas() {
        List<Notificaciones> notificacionesLeidas = notificacionRepository.findAllByOrderByFechaDesc()
                .stream()
                .filter(Notificaciones::getLeida)
                .toList();

        notificacionRepository.deleteAll(notificacionesLeidas);
        return notificacionesLeidas.size();
    }

    /**
     * Marca todas las notificaciones como leídas
     */
    public int marcarTodasComoLeidas() {
        List<Notificaciones> notificacionesNoLeidas = listarNotificacionesNoLeidas();

        for (Notificaciones notificacion : notificacionesNoLeidas) {
            notificacion.setLeida(true);
            notificacionRepository.save(notificacion);
        }

        return notificacionesNoLeidas.size();
    }

    /**
     * Trunca el contenido si es muy largo
     */
    private String truncarContenido(String contenido, int maxLength) {
        if (contenido == null) {
            return "";
        }

        if (contenido.length() <= maxLength) {
            return contenido;
        }

        return contenido.substring(0, maxLength - 3) + "...";
    }

    /**
     * Verifica si existe una notificación
     */
    public boolean existeNotificacion(Long id) {
        return notificacionRepository.existsById(id);
    }

    /**
     * Obtiene una notificación por ID
     */
    public Optional<Notificaciones> obtenerNotificacionPorId(Long id) {
        return notificacionRepository.findById(id);
    }
}
