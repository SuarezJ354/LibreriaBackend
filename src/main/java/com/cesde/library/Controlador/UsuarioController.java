package com.cesde.library.Controlador;

import com.cesde.library.Modelo.Rol;
import com.cesde.library.Modelo.Usuario;
import com.cesde.library.Servicios.UsuarioService;
import com.cesde.library.Security.JwtTokenProvider;
import com.cesde.library.Utils.JwtUtils;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtils jwtUtils; // ✅ Cambiar a JwtUtils

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String correo = loginData.get("correo");
        String password = loginData.get("password");

        try {
            // Autenticar las credenciales
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(correo, password)
            );

            // Obtener datos del usuario ANTES de generar el token
            Optional<Usuario> usuarioOpt = usuarioService.obtenerUsuarioPorCorreo(correo);
            if (!usuarioOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuario no encontrado"));
            }

            Usuario usuario = usuarioOpt.get();

            // ✅ Generar token con userId usando JwtUtils
            String token = jwtUtils.generateToken(correo, usuario.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("usuario", Map.of(
                    "id", usuario.getId(),
                    "nombre", usuario.getNombre(),
                    "correo", usuario.getCorreo(),
                    "rol", usuario.getRol() != null ? usuario.getRol().toString() : "SIN_ROL"
            ));
            response.put("token", token);

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "mensaje", "Credenciales inválidas"));
        }
    }


    @GetMapping("/me")
    public ResponseEntity<?> verificarSesion(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                    .body(Map.of("error", "Token no proporcionado"));
        }

        String token = authHeader.substring(7);

        try {
            // ✅ Usar JwtUtils para validar
            if (!jwtUtils.isTokenValid(token)) {
                return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                        .body(Map.of("error", "Token inválido"));
            }

            String username = jwtUtils.extractUsername(token);
            Long userId = jwtUtils.extractUserId(token); // ✅ Ahora sí tendrá userId

            Optional<Usuario> usuarioOpt = usuarioService.obtenerUsuarioPorCorreo(username);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                Map<String, Object> response = new HashMap<>();
                response.put("usuario", Map.of(
                        "id", usuario.getId(),
                        "nombre", usuario.getNombre(),
                        "correo", usuario.getCorreo(),
                        "rol", usuario.getRol()
                ));
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                    .body(Map.of("error", "Token inválido: " + e.getMessage()));
        }
    }

    // Otros endpoints (listar, actualizar, eliminar) se mantienen igual...
    @GetMapping("/")
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        List<Usuario> usuarios = usuarioService.obtenerTodosUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{correo}")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable String correo){
        Optional<Usuario> usuario = usuarioService.obtenerUsuarioPorCorreo(correo);
        return usuario.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuarioActualizado) {
        Optional<Usuario> usuarioExistente = usuarioService.obtenerUsuarioPorId(id);

        if (usuarioExistente.isPresent()) {
            Usuario usuario = usuarioExistente.get();
            usuario.setNombre(usuarioActualizado.getNombre());
            usuario.setCorreo(usuarioActualizado.getCorreo());
            usuario.setRol(usuarioActualizado.getRol());

            usuarioService.guardarUsuario(usuario);
            return ResponseEntity.ok(usuario);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarUsuario(@PathVariable Long id){
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }
}
