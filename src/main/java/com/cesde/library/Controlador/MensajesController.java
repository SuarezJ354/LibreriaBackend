package com.cesde.library.Controlador;

import com.cesde.library.Modelo.Mensajes;
import com.cesde.library.Modelo.Notificaciones;
import com.cesde.library.Repositorio.MensajeRepository;
import com.cesde.library.Servicios.NotificacionService;
import com.cesde.library.Utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/mensajes")
@CrossOrigin(origins = "*")
public class MensajesController {

    private static final Logger logger = LoggerFactory.getLogger(MensajesController.class);

    @Autowired
    private MensajeRepository mensajesRepository;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private JwtUtils jwtUtils;

    // Obtener todos los mensajes del usuario autenticado
    @GetMapping
    public ResponseEntity<?> obtenerMensajesDelUsuario(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(createErrorResponse("Token de autorización faltante o inválido"));
            }

            Long usuarioId = jwtUtils.extractUserIdFromRequest(request);

            if (usuarioId == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("No se pudo extraer el ID del usuario del token"));
            }

            List<Mensajes> mensajes = mensajesRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
            logger.info("Mensajes encontrados para usuario {}: {}", usuarioId, mensajes.size());

            return ResponseEntity.ok(mensajes);
        } catch (Exception e) {
            logger.error("Error en GET /mensajes: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("Error interno del servidor", e.getMessage()));
        }
    }

    // Crear nuevo mensaje
    @PostMapping
    public ResponseEntity<?> crearMensaje(@RequestBody Map<String, Object> mensajeData,
                                          HttpServletRequest request) {
        try {
            // Verificar token
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(createErrorResponse("Token de autorización faltante o inválido"));
            }

            // Extraer usuario ID
            Long usuarioId = jwtUtils.extractUserIdFromRequest(request);

            if (usuarioId == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("No se pudo extraer el ID del usuario del token"));
            }

            // Validar campos requeridos
            String contenido = (String) mensajeData.get("contenido");
            String autor = (String) mensajeData.get("autor");

            if (contenido == null || contenido.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("El contenido del mensaje es requerido"));
            }

            if (autor == null || autor.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("El autor del mensaje es requerido"));
            }

            // Crear el mensaje
            Mensajes mensaje = new Mensajes();
            mensaje.setContenido(contenido.trim());
            mensaje.setAutor(autor.trim());
            mensaje.setUsuarioId(usuarioId);
            mensaje.setEsRespuesta((Boolean) mensajeData.getOrDefault("esRespuesta", false));

            if (mensajeData.containsKey("mensajePadreId") && mensajeData.get("mensajePadreId") != null) {
                mensaje.setMensajePadreId(Long.valueOf(mensajeData.get("mensajePadreId").toString()));
            }

            Mensajes mensajeGuardado = mensajesRepository.save(mensaje);
            logger.info("Mensaje guardado con ID: {}", mensajeGuardado.getId());

            // Crear notificación de forma asíncrona para evitar bloqueos
            try {
                crearNotificacionParaMensaje(mensajeGuardado);
            } catch (Exception notifError) {
                logger.warn("Error al crear notificación: {}", notifError.getMessage());
            }

            return ResponseEntity.ok(mensajeGuardado);

        } catch (Exception e) {
            logger.error("Error en POST /mensajes: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("Error interno del servidor", e.getMessage()));
        }
    }

    // Obtener mensaje específico
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerMensaje(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long usuarioId = jwtUtils.extractUserIdFromRequest(request);

            if (usuarioId == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("No se pudo extraer el ID del usuario del token"));
            }

            return mensajesRepository.findByIdAndUsuarioId(id, usuarioId)
                    .map(mensaje -> ResponseEntity.ok(mensaje))
                    .orElseGet(() -> ResponseEntity.notFound().build());

        } catch (Exception e) {
            logger.error("Error en GET /mensajes/{}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("Error interno del servidor", e.getMessage()));
        }
    }

    // ENDPOINT PELIGROSO - Puede causar OOM si hay muchos mensajes
    @PostMapping("/generar-notificaciones-existentes")
    public ResponseEntity<?> generarNotificacionesExistentes() {
        try {
            logger.info("Iniciando generación de notificaciones para mensajes existentes");

            // CAMBIO CRÍTICO: Procesar en lotes para evitar OOM
            int pageSize = 100; // Procesar máximo 100 mensajes a la vez
            int page = 0;
            int notificacionesCreadas = 0;
            int mensajesTotales = 0;

            List<Mensajes> mensajes;
            do {
                // Usar paginación para evitar ca
                mensajes = mensajesRepository.findAllByOrderByIdAsc(
                        org.springframework.data.domain.PageRequest.of(page, pageSize)
                ).getContent();

                mensajesTotales += mensajes.size();

                for (Mensajes mensaje : mensajes) {
                    try {
                        List<Notificaciones> notificacionesExistentes =
                                notificacionService.obtenerNotificacionesPorMensaje(mensaje.getId());

                        if (notificacionesExistentes.isEmpty()) {
                            crearNotificacionParaMensaje(mensaje);
                            notificacionesCreadas++;
                        }
                    } catch (Exception e) {
                        logger.error("Error al procesar mensaje ID {}: {}", mensaje.getId(), e.getMessage());
                    }
                }

                page++;

                // Limpiar memoria explícitamente
                System.gc();

            } while (mensajes.size() == pageSize);

            logger.info("Proceso completado: {} notificaciones creadas de {} mensajes totales",
                    notificacionesCreadas, mensajesTotales);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Proceso completado");
            response.put("notificacionesCreadas", notificacionesCreadas);
            response.put("mensajesTotales", mensajesTotales);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error al generar notificaciones existentes: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("Error al generar notificaciones", e.getMessage()));
        }
    }

    // Eliminar mensaje
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarMensaje(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long usuarioId = jwtUtils.extractUserIdFromRequest(request);

            if (usuarioId == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("No se pudo extraer el ID del usuario del token"));
            }

            if (mensajesRepository.existsByIdAndUsuarioId(id, usuarioId)) {
                mensajesRepository.deleteById(id);
                logger.info("Mensaje {} eliminado por usuario {}", id, usuarioId);
                return ResponseEntity.ok().build();
            }

            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Error en DELETE /mensajes/{}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("Error interno del servidor", e.getMessage()));
        }
    }

    /**
     * Método optimizado para crear notificaciones
     */
    private void crearNotificacionParaMensaje(Mensajes mensaje) {
        try {
            Notificaciones notificacion = new Notificaciones();

            if (Boolean.TRUE.equals(mensaje.getEsRespuesta())) {
                notificacion.setTitulo("Nueva respuesta recibida");
            } else {
                notificacion.setTitulo("Nuevo mensaje recibido");
            }

            // Limitar contenido para evitar usar demasiada memoria
            String contenidoCompleto = "De: " + mensaje.getAutor() + " - " + mensaje.getContenido();
            if (contenidoCompleto.length() > 100) {
                notificacion.setContenido(contenidoCompleto.substring(0, 97) + "...");
            } else {
                notificacion.setContenido(contenidoCompleto);
            }

            notificacion.setLeida(false);
            notificacion.setFecha(mensaje.getFecha() != null ? mensaje.getFecha() : LocalDateTime.now());
            notificacion.setMensajeId(mensaje.getId());

            notificacionService.crearNotificacion(notificacion);

        } catch (Exception e) {
            logger.error("Error al crear notificación para mensaje ID {}: {}", mensaje.getId(), e.getMessage());
        }
    }

    /**
     * Método utilitario para crear respuestas de error consistentes
     */
    private Map<String, String> createErrorResponse(String error) {
        Map<String, String> response = new HashMap<>();
        response.put("error", error);
        return response;
    }

    private Map<String, String> createErrorResponse(String error, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", error);
        response.put("message", message);
        return response;
    }
}