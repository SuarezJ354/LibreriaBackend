package com.cesde.library.Security;

import com.cesde.library.Modelo.Usuario;
import com.cesde.library.Servicios.UsuarioService;
import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioService usuarioService;

    public CustomUserDetailsService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioService.obtenerUsuarioPorCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con correo: " + correo));

        // Crea una autoridad basada en el rol del usuario
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name());

        return new org.springframework.security.core.userdetails.User(
                usuario.getCorreo(),
                usuario.getPassword(), // aquí la contraseña debe estar encriptada o usar {noop}
                Collections.singletonList(authority)
        );
    }
}
