package com.cesde.library.Controlador;

import com.cesde.library.Modelo.Mensajes;
import com.cesde.library.Repositorio.MensajeRepository;
import com.cesde.library.Utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<List<Mensajes>> obtenerMensajesDelUsuario(HttpServletRequest request) {
        try {
            Long usuarioId = jwtUtils.extractUserIdFromRequest(request);
            List<Mensajes> mensajes = mensajesRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
            return ResponseEntity.ok(mensajes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Crear nuevo mensaje
    @PostMapping
    public ResponseEntity<Mensajes> crearMensaje(@RequestBody Map<String, Object> mensajeData,
                                                 HttpServletRequest request) {
        try {
            Long usuarioId = jwtUtils.extractUserIdFromRequest(request);

            Mensajes mensaje = new Mensajes();
            mensaje.setContenido((String) mensajeData.get("contenido"));
            mensaje.setAutor((String) mensajeData.get("autor"));
            mensaje.setUsuarioId(usuarioId);
            mensaje.setEsRespuesta((Boolean) mensajeData.getOrDefault("esRespuesta", false));

            if (mensajeData.containsKey("mensajePadreId")) {
                mensaje.setMensajePadreId(Long.valueOf(mensajeData.get("mensajePadreId").toString()));
            }

            Mensajes mensajeGuardado = mensajesRepository.save(mensaje);
            return ResponseEntity.ok(mensajeGuardado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obtener mensaje específico (solo si pertenece al usuario)
    @GetMapping("/{id}")
    public ResponseEntity<Mensajes> obtenerMensaje(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long usuarioId = jwtUtils.extractUserIdFromRequest(request);

            return mensajesRepository.findByIdAndUsuarioId(id, usuarioId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Eliminar mensaje (solo si pertenece al usuario)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMensaje(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long usuarioId = jwtUtils.extractUserIdFromRequest(request);

            if (mensajesRepository.existsByIdAndUsuarioId(id, usuarioId)) {
                mensajesRepository.deleteById(id);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}