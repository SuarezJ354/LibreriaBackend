package com.cesde.library.Controlador;

import com.cesde.library.Modelo.Mensajes;
import com.cesde.library.Repositorio.MensajeRepository;
import com.cesde.library.Utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mensajes")
@CrossOrigin(origins = "*")
public class MensajesController {

    @Autowired
    private MensajeRepository mensajesRepository;

    @Autowired
    private JwtUtils jwtUtils;

    // Obtener todos los mensajes del usuario autenticado
    @GetMapping
    public ResponseEntity<?> obtenerMensajesDelUsuario(HttpServletRequest request) {
        try {
            // Debug: verificar que el token esté presente
            String authHeader = request.getHeader("Authorization");
            System.out.println("Auth header: " + (authHeader != null ? authHeader.substring(0, Math.min(authHeader.length(), 30)) + "..." : "null"));

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body("Token de autorización faltante o inválido");
            }

            Long usuarioId = jwtUtils.extractUserIdFromRequest(request);
            System.out.println("Usuario ID extraído: " + usuarioId);

            if (usuarioId == null) {
                return ResponseEntity.badRequest().body("No se pudo extraer el ID del usuario del token");
            }

            List<Mensajes> mensajes = mensajesRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
            System.out.println("Mensajes encontrados: " + mensajes.size());

            return ResponseEntity.ok(mensajes);
        } catch (Exception e) {
            System.err.println("Error en GET /mensajes: " + e.getMessage());
            e.printStackTrace();

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
            System.out.println("=== DEBUG POST /mensajes ===");

            // Debug: verificar que el token esté presente
            String authHeader = request.getHeader("Authorization");
            System.out.println("Auth header presente: " + (authHeader != null));
            System.out.println("Auth header starts with Bearer: " + (authHeader != null && authHeader.startsWith("Bearer ")));

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("ERROR: Token faltante o inválido");
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Token de autorización faltante o inválido");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Debug: intentar extraer usuario ID
            System.out.println("Intentando extraer usuario ID...");
            Long usuarioId = jwtUtils.extractUserIdFromRequest(request);
            System.out.println("Usuario ID extraído: " + usuarioId);

            if (usuarioId == null) {
                System.out.println("ERROR: No se pudo extraer usuario ID");
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "No se pudo extraer el ID del usuario del token");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Debug: validar campos requeridos
            System.out.println("Datos recibidos: " + mensajeData);

            if (!mensajeData.containsKey("contenido") ||
                    mensajeData.get("contenido") == null ||
                    mensajeData.get("contenido").toString().trim().isEmpty()) {
                System.out.println("ERROR: Contenido faltante");
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "El contenido del mensaje es requerido");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (!mensajeData.containsKey("autor") ||
                    mensajeData.get("autor") == null ||
                    mensajeData.get("autor").toString().trim().isEmpty()) {
                System.out.println("ERROR: Autor faltante");
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "El autor del mensaje es requerido");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Crear el mensaje
            System.out.println("Creando mensaje...");
            Mensajes mensaje = new Mensajes();
            mensaje.setContenido((String) mensajeData.get("contenido"));
            mensaje.setAutor((String) mensajeData.get("autor"));
            mensaje.setUsuarioId(usuarioId);
            mensaje.setEsRespuesta((Boolean) mensajeData.getOrDefault("esRespuesta", false));

            if (mensajeData.containsKey("mensajePadreId") && mensajeData.get("mensajePadreId") != null) {
                mensaje.setMensajePadreId(Long.valueOf(mensajeData.get("mensajePadreId").toString()));
            }

            System.out.println("Guardando mensaje...");
            Mensajes mensajeGuardado = mensajesRepository.save(mensaje);
            System.out.println("Mensaje guardado con ID: " + mensajeGuardado.getId());
            System.out.println("========================");

            return ResponseEntity.ok(mensajeGuardado);

        } catch (Exception e) {
            System.err.println("ERROR en POST /mensajes: " + e.getMessage());
            e.printStackTrace();

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
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "No se pudo extraer el ID del usuario del token");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            return mensajesRepository.findByIdAndUsuarioId(id, usuarioId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error en GET /mensajes/{id}: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del servidor");
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
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "No se pudo extraer el ID del usuario del token");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (mensajesRepository.existsByIdAndUsuarioId(id, usuarioId)) {
                mensajesRepository.deleteById(id);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Error en DELETE /mensajes/{id}: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del servidor");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}