package com.cesde.library.Controlador;

import com.cesde.library.Modelo.Rol;
import com.cesde.library.Modelo.Usuario;
import com.cesde.library.Servicios.UsuarioService;
import com.cesde.library.Security.JwtTokenProvider;
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

@CrossOrigin(origins = "http://localhost:2007")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder; // ✅ AÑADIR ESTO

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario usuario){
        try {
            // ✅ Encriptar contraseña antes de guardar
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

            Usuario nuevoUsuario = usuarioService.guardarUsuario(usuario);
            return ResponseEntity.ok(nuevoUsuario);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String correo = loginData.get("correo");
        String password = loginData.get("password");

        try {
            // Autenticar las credenciales con el AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(correo, password)
            );

            // Generar el token JWT (solo el token puro)
            String token = jwtTokenProvider.generateToken(authentication);

            // Obtener datos del usuario
            Optional<Usuario> usuarioOpt = usuarioService.obtenerUsuarioPorCorreo(correo);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("usuario", Map.of(
                    "id", usuarioOpt.map(Usuario::getId).orElse(0L),
                    "nombre", usuarioOpt.map(Usuario::getNombre).orElse(""),
                    "correo", usuarioOpt.map(Usuario::getCorreo).orElse(""),
                    "rol", usuarioOpt.map(u -> u.getRol() != null ? u.getRol().toString() : "SIN_ROL").orElse("SIN_ROL")
            ));

            // *** Aquí NO agregamos "Bearer ", solo el token puro ***
            response.put("token", token);

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Credenciales inválidas");
            return ResponseEntity.badRequest().body(response);
        }
    }


    @GetMapping("/me")
    public ResponseEntity<?> verificarSesion(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                    .body(Map.of("error", "Token no proporcionado"));
        }

        String token = authHeader.substring(7);

        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                    .body(Map.of("error", "Token inválido"));
        }

        String username = jwtTokenProvider.getUsernameFromJWT(token);
        Optional<Usuario> usuarioOpt = usuarioService.obtenerUsuarioPorCorreo(username);

        if (usuarioOpt.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("usuario", Map.of(
                    "id", usuarioOpt.get().getId(),
                    "nombre", usuarioOpt.get().getNombre(),
                    "correo", usuarioOpt.get().getCorreo(),
                    "rol", usuarioOpt.get().getRol()
            ));
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                .body(Map.of("error", "Usuario no encontrado"));
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
