// ========== MensajeController.java (Actualizado) ==========
package com.cesde.library.Controlador;

import com.cesde.library.Modelo.Mensajes;
import com.cesde.library.Servicios.MensajeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/mensajes")
@CrossOrigin(origins = {"http://localhost:2007", "https://tu-frontend-url.com"})
public class MensajeController {

    @Autowired
    private MensajeService mensajeService;

    @GetMapping
    public ResponseEntity<List<Mensajes>> listarMensajes() {
        try {
            List<Mensajes> mensajes = mensajeService.listarTodosMensajes();
            return ResponseEntity.ok(mensajes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<Mensajes> guardarMensaje(@RequestBody Mensajes mensaje) {
        try {
            Mensajes mensajeGuardado = mensajeService.guardarMensaje(mensaje);
            return ResponseEntity.ok(mensajeGuardado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/respuestas")
    public ResponseEntity<List<Mensajes>> obtenerRespuestas(@PathVariable Long id) {
        try {
            List<Mensajes> respuestas = mensajeService.obtenerRespuestasPorMensaje(id);
            return ResponseEntity.ok(respuestas);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mensajes> obtenerMensajePorId(@PathVariable Long id) {
        try {
            Optional<Mensajes> mensaje = mensajeService.obtenerMensajePorId(id);
            if (mensaje.isPresent()) {
                return ResponseEntity.ok(mensaje.get());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/autor/{autor}")
    public ResponseEntity<List<Mensajes>> obtenerMensajesPorAutor(@PathVariable String autor) {
        try {
            List<Mensajes> mensajes = mensajeService.obtenerMensajesPorAutor(autor);
            return ResponseEntity.ok(mensajes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mensajes> actualizarMensaje(@PathVariable Long id, @RequestBody Mensajes mensaje) {
        try {
            Mensajes mensajeActualizado = mensajeService.actualizarMensaje(id, mensaje);
            return ResponseEntity.ok(mensajeActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarMensaje(@PathVariable Long id) {
        try {
            boolean eliminado = mensajeService.eliminarMensaje(id);
            if (eliminado) {
                return ResponseEntity.ok("Mensaje eliminado correctamente");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al eliminar el mensaje");
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> contarMensajes() {
        try {
            long count = mensajeService.contarMensajes();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/principales")
    public ResponseEntity<List<Mensajes>> obtenerMensajesPrincipales() {
        try {
            List<Mensajes> mensajes = mensajeService.obtenerMensajesPrincipales();
            return ResponseEntity.ok(mensajes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}