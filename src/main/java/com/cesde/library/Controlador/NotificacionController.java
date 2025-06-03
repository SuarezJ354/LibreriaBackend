package com.cesde.library.Controlador;

import com.cesde.library.Modelo.Mensajes;
import com.cesde.library.Modelo.Notificaciones;
import com.cesde.library.Servicios.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;
import com.cesde.library.Servicios.MensajeService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/notificaciones")
@CrossOrigin(origins = {"http://localhost:2007"})
public class NotificacionController {
    @Autowired
    private MensajeService mensajeService;

    @Autowired
    private NotificacionService notificacionService;

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
    // Agregar este método a tu NotificacionController.java

    @PostMapping("/{id}/respuesta")
    public ResponseEntity<String> responderNotificacion(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String mensaje = request.get("mensaje");
            if (mensaje == null || mensaje.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El mensaje es obligatorio");
            }

            // Primero obtenemos la notificación
            Optional<Notificaciones> notificacionOpt = notificacionService.obtenerNotificacionPorId(id);
            if (!notificacionOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Notificaciones notificacion = notificacionOpt.get();

            // Creamos un nuevo mensaje como respuesta
            Mensajes respuesta = new Mensajes();
            respuesta.setContenido(mensaje.trim());
            respuesta.setAutor("Administrador"); // O el usuario que está respondiendo
            respuesta.setEsRespuesta(true);
            respuesta.setMensajePadreId(notificacion.getMensajeId()); // Usando mensajePadreId
            respuesta.setFecha(LocalDateTime.now()); // Usando LocalDateTime

            // Guardamos la respuesta usando el servicio de mensajes
            mensajeService.guardarMensaje(respuesta);

            // Marcamos la notificación como leída
            notificacionService.marcarNotificacionComoLeida(id);

            return ResponseEntity.ok("Respuesta enviada correctamente");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al enviar la respuesta");
        }
    }
}
