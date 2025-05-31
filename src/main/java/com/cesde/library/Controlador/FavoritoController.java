package com.cesde.library.Controlador;

import com.cesde.library.Modelo.Favorito;
import com.cesde.library.Modelo.Usuario;
import com.cesde.library.Servicios.FavoritoService;
import com.cesde.library.Servicios.UsuarioService;
import com.cesde.library.Utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favoritos")
public class FavoritoController {

    private final FavoritoService favoritoService;
    private final UsuarioService usuarioService;
    private final JwtUtils jwtUtils;

    public FavoritoController(FavoritoService favoritoService, UsuarioService usuarioService, JwtUtils jwtUtils) {
        this.favoritoService = favoritoService;
        this.usuarioService = usuarioService;
        this.jwtUtils = jwtUtils;
    }

    private Usuario extraerUsuarioDesdeToken(HttpServletRequest request) {
        String token = jwtUtils.extractTokenFromRequest(request);
        String correo = jwtUtils.extractUsername(token);
        return usuarioService.obtenerUsuarioPorCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @PostMapping("/{libroId}")
    public ResponseEntity<Favorito> agregarFavorito(@PathVariable Long libroId, HttpServletRequest request) {
        Usuario usuario = extraerUsuarioDesdeToken(request);
        return ResponseEntity.ok(favoritoService.agregarFavorito(usuario, libroId));
    }

    @DeleteMapping("/{libroId}")
    public ResponseEntity<Void> eliminarFavorito(@PathVariable Long libroId, HttpServletRequest request) {
        Usuario usuario = extraerUsuarioDesdeToken(request);
        favoritoService.eliminarFavorito(usuario, libroId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Favorito>> obtenerFavoritos(HttpServletRequest request) {
        Usuario usuario = extraerUsuarioDesdeToken(request);
        return ResponseEntity.ok(favoritoService.obtenerFavoritosDeUsuario(usuario));
    }
}
