package com.cesde.library.Controlador;

import com.cesde.library.Modelo.Mensajes;
import com.cesde.library.Modelo.Notificaciones;
import com.cesde.library.Servicios.NotificacionService;
import com.cesde.library.Servicios.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;
import com.cesde.library.Servicios.MensajeService;
import org.springframework.security.core.Authentication;
import com.cesde.library.Modelo.Usuario;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/notificaciones")
@CrossOrigin(origins = "*") //
public class NotificacionController {
    @Autowired
    private MensajeService mensajeService;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private UsuarioService usuarioService; // ✅ Agregar esta línea

    @GetMapping
    public ResponseEntity<List<Notificaciones>> listarNotificaciones() {
        try {
            List<Notificaciones> notificaciones = notificacionService.listarTodasNotificaciones();
            return ResponseEntity.ok(notificaciones);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/no-leidas")
    public ResponseEntity<List<Notificaciones>> listarNotificacionesNoLeidas() {
        try {
            List<Notificaciones> notificaciones = notificacionService.listarNotificacionesNoLeidas();
            return ResponseEntity.ok(notificaciones);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<Notificaciones> crearNotificacion(@RequestBody Notificaciones notificacion) {
        try {
            Notificaciones notificacionGuardada = notificacionService.crearNotificacion(notificacion);
            return ResponseEntity.ok(notificacionGuardada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{mensajeId}/leer")
    public ResponseEntity<String> marcarComoLeida(@PathVariable Long mensajeId) {
        try {
            boolean marcado = notificacionService.marcarNotificacionesComoLeidas(mensajeId);
            if (marcado) {
                return ResponseEntity.ok("Notificaciones marcadas como leídas");
            }
            return ResponseEntity.badRequest().body("Error al marcar notificaciones como leídas");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error interno del servidor");
        }
    }

    @PutMapping("/notificacion/{id}/leer")
    public ResponseEntity<String> marcarNotificacionComoLeida(@PathVariable Long id) {
        try {
            boolean marcado = notificacionService.marcarNotificacionComoLeida(id);
            if (marcado) {
                return ResponseEntity.ok("Notificación marcada como leída");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al marcar notificación como leída");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarNotificacion(@PathVariable Long id) {
        try {
            boolean eliminado = notificacionService.eliminarNotificacion(id);
            if (eliminado) {
                return ResponseEntity.ok("Notificación eliminada");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al eliminar notificación");
        }
    }

    @GetMapping("/count/no-leidas")
    public ResponseEntity<Long> contarNotificacionesNoLeidas() {
        try {
            Long count = notificacionService.contarNotificacionesNoLeidas();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Notificaciones>> obtenerNotificacionesPorTipo(@PathVariable String tipo) {
        try {
            List<Notificaciones> notificaciones = notificacionService.obtenerNotificacionesPorTipo(tipo);
            return ResponseEntity.ok(notificaciones);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/mensaje/{mensajeId}")
    public ResponseEntity<List<Notificaciones>> obtenerNotificacionesPorMensaje(@PathVariable Long mensajeId) {
        try {
            List<Notificaciones> notificaciones = notificacionService.obtenerNotificacionesPorMensaje(mensajeId);
            return ResponseEntity.ok(notificaciones);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/leidas")
    public ResponseEntity<String> eliminarNotificacionesLeidas() {
        try {
            int eliminadas = notificacionService.eliminarNotificacionesLeidas();
            return ResponseEntity.ok("Se eliminaron " + eliminadas + " notificaciones leídas");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al eliminar notificaciones leídas");
        }
    }

    @PutMapping("/marcar-todas-leidas")
    public ResponseEntity<String> marcarTodasComoLeidas() {
        try {
            int marcadas = notificacionService.marcarTodasComoLeidas();
            return ResponseEntity.ok("Se marcaron " + marcadas + " notificaciones como leídas");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al marcar todas las notificaciones como leídas");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notificaciones> obtenerNotificacionPorId(@PathVariable Long id) {
        try {
            Optional<Notificaciones> notificacion = notificacionService.obtenerNotificacionPorId(id);
            if (notificacion.isPresent()) {
                return ResponseEntity.ok(notificacion.get());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/respuesta")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
    public ResponseEntity<String> responderNotificacion(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) { // ✅ Agregar Authentication

        System.out.println("🔵 Iniciando respuesta a notificación ID: " + id);

        try {
            String mensaje = request.get("mensaje");
            System.out.println("📝 Mensaje recibido: " + mensaje);

            if (mensaje == null || mensaje.trim().isEmpty()) {
                System.out.println("❌ Mensaje vacío");
                return ResponseEntity.badRequest().body("El mensaje es obligatorio");
            }

            // ✅ Obtener usuario por correo (que viene del JWT)
            String correoUsuario = authentication.getName();
            System.out.println("👤 Correo del usuario autenticado: " + correoUsuario);

            Optional<Usuario> usuarioOpt = usuarioService.obtenerUsuarioPorCorreo(correoUsuario);
            if (!usuarioOpt.isPresent()) {
                System.out.println("❌ Usuario no encontrado con correo: " + correoUsuario);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no encontrado");
            }

            Usuario usuario = usuarioOpt.get();
            System.out.println("✅ Usuario encontrado - ID: " + usuario.getId() + ", Nombre: " + usuario.getNombre());

            // Resto de tu código para obtener la notificación...
            Optional<Notificaciones> notificacionOpt = notificacionService.obtenerNotificacionPorId(id);
            if (!notificacionOpt.isPresent()) {
                System.out.println("❌ Notificación no encontrada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notificación no encontrada");
            }

            Notificaciones notificacion = notificacionOpt.get();
            System.out.println("✅ Notificación encontrada - MensajeID: " + notificacion.getMensajeId());

            if (notificacion.getMensajeId() == null) {
                System.out.println("❌ La notificación no tiene mensajeId");
                return ResponseEntity.badRequest().body("La notificación no tiene un mensaje asociado");
            }

            // Verificar mensaje padre
            Optional<Mensajes> mensajePadreOpt = mensajeService.obtenerMensajePorId(notificacion.getMensajeId());
            if (!mensajePadreOpt.isPresent()) {
                System.out.println("❌ Mensaje padre no encontrado");
                return ResponseEntity.badRequest().body("El mensaje original no existe");
            }

            // ✅ Crear respuesta CON el usuario
            System.out.println("📝 Creando mensaje de respuesta...");
            Mensajes respuesta = new Mensajes();
            respuesta.setContenido(mensaje.trim());
            respuesta.setAutor(usuario.getNombre()); // Usar el nombre real del usuario
            respuesta.setEsRespuesta(true);
            respuesta.setMensajePadreId(notificacion.getMensajeId());
            respuesta.setFecha(LocalDateTime.now());
            respuesta.setUsuarioId(usuario.getId());

            System.out.println("📋 Datos de respuesta:");
            System.out.println("  - Usuario ID: " + usuario.getId());
            System.out.println("  - Usuario: " + usuario.getNombre());
            System.out.println("  - Contenido: " + respuesta.getContenido());

            // Guardar
            try {
                Mensajes respuestaGuardada = mensajeService.guardarMensaje(respuesta);
                System.out.println("✅ Mensaje guardado con ID: " + respuestaGuardada.getId());
            } catch (Exception e) {
                System.out.println("❌ Error al guardar mensaje: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.internalServerError().body("Error al guardar respuesta: " + e.getMessage());
            }

            // Marcar como leída
            notificacionService.marcarNotificacionComoLeida(id);
            System.out.println("🎉 Proceso completado exitosamente");
            return ResponseEntity.ok("Respuesta enviada correctamente");

        } catch (Exception e) {
            System.out.println("💥 Error general: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error interno: " + e.getMessage());
        }
    }
}