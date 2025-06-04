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
            // Debug: verificar que el token esté presente
            String authHeader = request.getHeader("Authorization");
            logger.debug("Auth header presente: {}", authHeader != null);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Token de autorización faltante o inválido");
                return ResponseEntity.badRequest().body("Token de autorización faltante o inválido");
            }

            Long usuarioId = jwtUtils.extractUserIdFromRequest(request);
            logger.info("Usuario ID extraído: {}", usuarioId);

            if (usuarioId == null) {
                logger.warn("No se pudo extraer el ID del usuario del token");
                return ResponseEntity.badRequest().body("No se pudo extraer el ID del usuario del token");
            }

            List<Mensajes> mensajes = mensajesRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
            logger.info("Mensajes encontrados para usuario {}: {}", usuarioId, mensajes.size());

            return ResponseEntity.ok(mensajes);
        } catch (Exception e) {
            logger.error("Error en GET /mensajes: {}", e.getMessage(), e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del servidor");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Crear nuevo mensaje
    @PostMapping
    public ResponseEntity<?> crearMensaje(@RequestBody Map<String, Object> mensajeData,
                                          HttpServletRequest request) {
        try {
            logger.debug("=== Iniciando creación de mensaje ===");

            // Verificar token
            String authHeader = request.getHeader("Authorization");
            logger.debug("Auth header presente: {}", authHeader != null);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Token faltante o inválido");
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Token de autorización faltante o inválido");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Extraer usuario ID
            Long usuarioId = jwtUtils.extractUserIdFromRequest(request);
            logger.info("Usuario ID extraído: {}", usuarioId);

            if (usuarioId == null) {
                logger.warn("No se pudo extraer usuario ID del token");
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "No se pudo extraer el ID del usuario del token");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Validar campos requeridos
            logger.debug("Datos recibidos: {}", mensajeData);

            if (!mensajeData.containsKey("contenido") ||
                    mensajeData.get("contenido") == null ||
                    mensajeData.get("contenido").toString().trim().isEmpty()) {
                logger.warn("Contenido del mensaje faltante");
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "El contenido del mensaje es requerido");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (!mensajeData.containsKey("autor") ||
                    mensajeData.get("autor") == null ||
                    mensajeData.get("autor").toString().trim().isEmpty()) {
                logger.warn("Autor del mensaje faltante");
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "El autor del mensaje es requerido");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Crear el mensaje
            logger.debug("Creando nuevo mensaje");
            Mensajes mensaje = new Mensajes();
            mensaje.setContenido((String) mensajeData.get("contenido"));
            mensaje.setAutor((String) mensajeData.get("autor"));
            mensaje.setUsuarioId(usuarioId);
            mensaje.setEsRespuesta((Boolean) mensajeData.getOrDefault("esRespuesta", false));

            if (mensajeData.containsKey("mensajePadreId") && mensajeData.get("mensajePadreId") != null) {
                mensaje.setMensajePadreId(Long.valueOf(mensajeData.get("mensajePadreId").toString()));
            }

            Mensajes mensajeGuardado = mensajesRepository.save(mensaje);
            logger.info("Mensaje guardado con ID: {}", mensajeGuardado.getId());

            // Crear notificación (versión simplificada)
            try {
                logger.debug("Intentando crear notificación para mensaje ID: {}", mensajeGuardado.getId());
                crearNotificacionParaMensaje(mensajeGuardado);
                logger.info("Proceso de notificación completado para mensaje ID: {}", mensajeGuardado.getId());
            } catch (Exception notifError) {
                // No fallar el guardado del mensaje si hay error en la notificación
                logger.warn("Error al crear notificación (no crítico): {}", notifError.getMessage());
            }

            logger.debug("=== Creación de mensaje completada ===");
            return ResponseEntity.ok(mensajeGuardado);

        } catch (Exception e) {
            logger.error("Error en POST /mensajes: {}", e.getMessage(), e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del servidor");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("type", e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Obtener mensaje específico (solo si pertenece al usuario)
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerMensaje(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long usuarioId = jwtUtils.extractUserIdFromRequest(request);

            if (usuarioId == null) {
                logger.warn("No se pudo extraer usuario ID para obtener mensaje {}", id);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "No se pudo extraer el ID del usuario del token");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            logger.debug("Buscando mensaje {} para usuario {}", id, usuarioId);
            return mensajesRepository.findByIdAndUsuarioId(id, usuarioId)
                    .map(mensaje -> {
                        logger.info("Mensaje {} encontrado para usuario {}", id, usuarioId);
                        return ResponseEntity.ok(mensaje);
                    })
                    .orElseGet(() -> {
                        logger.info("Mensaje {} no encontrado para usuario {}", id, usuarioId);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            logger.error("Error en GET /mensajes/{}: {}", id, e.getMessage(), e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del servidor");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Endpoint para crear notificaciones de mensajes existentes
    @PostMapping("/generar-notificaciones-existentes")
    public ResponseEntity<?> generarNotificacionesExistentes() {
        try {
            logger.info("Iniciando generación de notificaciones para mensajes existentes");

            List<Mensajes> todosMensajes = mensajesRepository.findAll();
            int notificacionesCreadas = 0;

            for (Mensajes mensaje : todosMensajes) {
                try {
                    List<Notificaciones> notificacionesExistentes =
                            notificacionService.obtenerNotificacionesPorMensaje(mensaje.getId());

                    if (notificacionesExistentes.isEmpty()) {
                        crearNotificacionParaMensaje(mensaje);
                        notificacionesCreadas++;
                    }
                } catch (Exception e) {
                    logger.error("Error al procesar mensaje ID {}: {}", mensaje.getId(), e.getMessage(), e);
                }
            }

            logger.info("Proceso completado: {} notificaciones creadas de {} mensajes totales",
                    notificacionesCreadas, todosMensajes.size());

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Proceso completado");
            response.put("notificacionesCreadas", notificacionesCreadas);
            response.put("mensajesTotales", todosMensajes.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error al generar notificaciones existentes: {}", e.getMessage(), e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al generar notificaciones");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Eliminar mensaje (solo si pertenece al usuario)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarMensaje(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long usuarioId = jwtUtils.extractUserIdFromRequest(request);

            if (usuarioId == null) {
                logger.warn("No se pudo extraer usuario ID para eliminar mensaje {}", id);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "No se pudo extraer el ID del usuario del token");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (mensajesRepository.existsByIdAndUsuarioId(id, usuarioId)) {
                mensajesRepository.deleteById(id);
                logger.info("Mensaje {} eliminado por usuario {}", id, usuarioId);
                return ResponseEntity.ok().build();
            }

            logger.info("Mensaje {} no encontrado para eliminar por usuario {}", id, usuarioId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error en DELETE /mensajes/{}: {}", id, e.getMessage(), e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del servidor");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     */
    private void crearNotificacionParaMensaje(Mensajes mensaje) {
        try {
            logger.debug("Creando notificación para mensaje ID: {} de autor: {}",
                    mensaje.getId(), mensaje.getAutor());

            // CAMBIO PRINCIPAL: Crear notificaciones para TODOS los mensajes
            // Removí la condición: if ("Administrador".equals(mensaje.getAutor()))

            Notificaciones notificacion = new Notificaciones();

            // Configurar título según si es respuesta o mensaje nuevo
            if (Boolean.TRUE.equals(mensaje.getEsRespuesta())) {
                notificacion.setTitulo("Nueva respuesta recibida");
            } else {
                notificacion.setTitulo("Nuevo mensaje recibido");
            }

            // Configurar contenido (limitar a 100 caracteres)
            String contenidoCompleto = "De: " + mensaje.getAutor() + " - " + mensaje.getContenido();
            if (contenidoCompleto.length() > 100) {
                notificacion.setContenido(contenidoCompleto.substring(0, 97) + "...");
            } else {
                notificacion.setContenido(contenidoCompleto);
            }

            notificacion.setLeida(false);
            notificacion.setFecha(mensaje.getFecha() != null ? mensaje.getFecha() : LocalDateTime.now());
            notificacion.setMensajeId(mensaje.getId());

            Notificaciones notificacionCreada = notificacionService.crearNotificacion(notificacion);
            logger.info("Notificación creada con ID: {} para mensaje ID: {}",
                    notificacionCreada.getId(), mensaje.getId());

        } catch (Exception e) {
            // No fallar el guardado del mensaje si hay error en la notificación
            logger.error("Error al crear notificación para mensaje ID {} (no crítico): {}",
                    mensaje.getId(), e.getMessage());
        }
    }
}