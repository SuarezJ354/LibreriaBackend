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
            logger.debug("Datos recibidos: {}", mensajeData);

            // Verificar token
            String authHeader = request.getHeader("Authorization");
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
            mensaje.setContenido(mensajeData.get("contenido").toString().trim());
            mensaje.setAutor(mensajeData.get("autor").toString().trim());
            mensaje.setUsuarioId(usuarioId);
            mensaje.setEsRespuesta((Boolean) mensajeData.getOrDefault("esRespuesta", false));
            mensaje.setFecha(LocalDateTime.now()); // Asegurar que tenga fecha

            if (mensajeData.containsKey("mensajePadreId") && mensajeData.get("mensajePadreId") != null) {
                mensaje.setMensajePadreId(Long.valueOf(mensajeData.get("mensajePadreId").toString()));
            }

            // Guardar el mensaje
            Mensajes mensajeGuardado = mensajesRepository.save(mensaje);
            logger.info("✅ Mensaje guardado con ID: {}", mensajeGuardado.getId());

            // IMPORTANTE: Crear notificación DESPUÉS de guardar el mensaje
            logger.debug("🔔 Iniciando creación de notificación...");
            boolean notificacionCreada = crearNotificacionParaMensaje(mensajeGuardado);

            if (notificacionCreada) {
                logger.info("✅ Notificación creada exitosamente para mensaje ID: {}", mensajeGuardado.getId());
            } else {
                logger.warn("⚠️ No se pudo crear la notificación para mensaje ID: {}", mensajeGuardado.getId());
            }

            logger.debug("=== Creación de mensaje completada ===");
            return ResponseEntity.ok(mensajeGuardado);

        } catch (Exception e) {
            logger.error("❌ Error en POST /mensajes: {}", e.getMessage(), e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del servidor");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("type", e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

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

    // Endpoint para debugging - verificar si las notificaciones se están creando
    @GetMapping("/debug/notificaciones")
    public ResponseEntity<?> debugNotificaciones() {
        try {
            List<Notificaciones> todasNotificaciones = notificacionService.listarTodasNotificaciones();
            logger.info("🔍 Total de notificaciones en sistema: {}", todasNotificaciones.size());

            Map<String, Object> response = new HashMap<>();
            response.put("totalNotificaciones", todasNotificaciones.size());
            response.put("notificaciones", todasNotificaciones);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error en debug notificaciones: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Endpoint para forzar creación de notificaciones
    @PostMapping("/debug/crear-notificacion/{mensajeId}")
    public ResponseEntity<?> forzarCreacionNotificacion(@PathVariable Long mensajeId) {
        try {
            logger.info("🔧 Forzando creación de notificación para mensaje ID: {}", mensajeId);

            Mensajes mensaje = mensajesRepository.findById(mensajeId).orElse(null);
            if (mensaje == null) {
                return ResponseEntity.notFound().build();
            }

            boolean creada = crearNotificacionParaMensaje(mensaje);

            Map<String, Object> response = new HashMap<>();
            response.put("mensajeId", mensajeId);
            response.put("notificacionCreada", creada);
            response.put("autor", mensaje.getAutor());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error forzando notificación: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Método mejorado para crear notificaciones de mensajes
     */
    private boolean crearNotificacionParaMensaje(Mensajes mensaje) {
        try {
            logger.debug("🔔 Intentando crear notificación para mensaje ID: {}", mensaje.getId());
            logger.debug("📝 Autor del mensaje: '{}'", mensaje.getAutor());
            logger.debug("📝 Contenido: '{}'", mensaje.getContenido());
            logger.debug("📝 Es respuesta: {}", mensaje.getEsRespuesta());

            // REMOVÍ LA CONDICIÓN QUE EXCLUÍA AL ADMINISTRADOR
            // Ahora se crean notificaciones para TODOS los mensajes

            Notificaciones notificacion = new Notificaciones();

            // Configurar título según si es respuesta o mensaje nuevo
            if (Boolean.TRUE.equals(mensaje.getEsRespuesta())) {
                notificacion.setTitulo("Nueva respuesta recibida");
                logger.debug("🔔 Tipo: Nueva respuesta");
            } else {
                notificacion.setTitulo("Nuevo mensaje recibido");
                logger.debug("🔔 Tipo: Nuevo mensaje");
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

            logger.debug("🔔 Datos de notificación preparados:");
            logger.debug("   - Título: {}", notificacion.getTitulo());
            logger.debug("   - Contenido: {}", notificacion.getContenido());
            logger.debug("   - Mensaje ID: {}", notificacion.getMensajeId());
            logger.debug("   - Fecha: {}", notificacion.getFecha());

            // Intentar crear la notificación
            Notificaciones notificacionCreada = notificacionService.crearNotificacion(notificacion);

            if (notificacionCreada != null && notificacionCreada.getId() != null) {
                logger.info("✅ Notificación creada exitosamente con ID: {} para mensaje ID: {}",
                        notificacionCreada.getId(), mensaje.getId());
                return true;
            } else {
                logger.error("❌ El servicio devolvió null o sin ID para mensaje ID: {}", mensaje.getId());
                return false;
            }

        } catch (Exception e) {
            logger.error("❌ Error al crear notificación para mensaje ID {}: {}",
                    mensaje.getId(), e.getMessage(), e);

            // Log adicional para debugging
            logger.error("❌ Tipo de error: {}", e.getClass().getSimpleName());
            if (e.getCause() != null) {
                logger.error("❌ Causa raíz: {}", e.getCause().getMessage());
            }

            return false;
        }
    }
}