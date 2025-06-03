package com.cesde.library.Servicios;

import com.cesde.library.Modelo.Mensajes;
import com.cesde.library.Repositorio.MensajeRepository;
import com.cesde.library.Servicios.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MensajeService {

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private NotificacionService notificacionService;

    /**
     * Obtiene todos los mensajes ordenados por fecha ascendente
     */
    public List<Mensajes> listarTodosMensajes() {
        return mensajeRepository.findAllByOrderByFechaAsc();
    }

    /**
     * Guarda un nuevo mensaje y crea notificación si no es respuesta
     */
    public Mensajes guardarMensaje(Mensajes mensaje) {
        // Establecer fecha actual si no se proporciona
        if (mensaje.getFecha() == null) {
            mensaje.setFecha(LocalDateTime.now());
        }

        // Validaciones básicas
        if (mensaje.getContenido() == null || mensaje.getContenido().trim().isEmpty()) {
            throw new IllegalArgumentException("El contenido del mensaje no puede estar vacío");
        }

        if (mensaje.getAutor() == null || mensaje.getAutor().trim().isEmpty()) {
            throw new IllegalArgumentException("El autor del mensaje no puede estar vacío");
        }

        // Si es una respuesta, validar que el mensaje padre exista
        if (mensaje.getEsRespuesta() && mensaje.getMensajePadreId() != null) {
            if (!existeMensaje(mensaje.getMensajePadreId())) {
                throw new IllegalArgumentException("El mensaje padre no existe");
            }
        }

        Mensajes mensajeGuardado = mensajeRepository.save(mensaje);

        // Si no es una respuesta, crear notificación
        if (!mensaje.getEsRespuesta()) {
            notificacionService.crearNotificacionPorMensaje(mensajeGuardado);
        }

        return mensajeGuardado;
    }

    /**
     * Obtiene las respuestas de un mensaje específico
     */
    public List<Mensajes> obtenerRespuestasPorMensaje(Long mensajeId) {
        if (!existeMensaje(mensajeId)) {
            throw new IllegalArgumentException("El mensaje no existe");
        }
        return mensajeRepository.findByMensajePadreIdOrderByFechaAsc(mensajeId);
    }

    /**
     * Obtiene un mensaje por su ID
     */
    public Optional<Mensajes> obtenerMensajePorId(Long id) {
        return mensajeRepository.findById(id);
    }

    /**
     * Obtiene mensajes por autor
     */
    public List<Mensajes> obtenerMensajesPorAutor(String autor) {
        if (autor == null || autor.trim().isEmpty()) {
            throw new IllegalArgumentException("El autor no puede estar vacío");
        }
        return mensajeRepository.findByAutorOrderByFechaDesc(autor);
    }

    /**
     * Verifica si existe un mensaje con el ID dado
     */
    public boolean existeMensaje(Long id) {
        return mensajeRepository.existsById(id);
    }

    /**
     * Elimina un mensaje por ID
     */
    public boolean eliminarMensaje(Long id) {
        if (existeMensaje(id)) {
            mensajeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Actualiza un mensaje existente
     */
    public Mensajes actualizarMensaje(Long id, Mensajes mensajeActualizado) {
        Optional<Mensajes> mensajeExistente = obtenerMensajePorId(id);

        if (mensajeExistente.isPresent()) {
            Mensajes mensaje = mensajeExistente.get();

            if (mensajeActualizado.getContenido() != null) {
                mensaje.setContenido(mensajeActualizado.getContenido());
            }

            if (mensajeActualizado.getAutor() != null) {
                mensaje.setAutor(mensajeActualizado.getAutor());
            }

            return mensajeRepository.save(mensaje);
        }

        throw new IllegalArgumentException("El mensaje no existe");
    }

    /**
     * Cuenta el total de mensajes
     */
    public long contarMensajes() {
        return mensajeRepository.count();
    }

    /**
     * Obtiene mensajes que no son respuestas (mensajes principales)
     */
    public List<Mensajes> obtenerMensajesPrincipales() {
        return mensajeRepository.findAllByOrderByFechaAsc()
                .stream()
                .filter(mensaje -> !mensaje.getEsRespuesta())
                .toList();
    }
}



