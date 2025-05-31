package com.cesde.library.Servicios;

import com.cesde.library.Modelo.Rol;
import com.cesde.library.Modelo.Usuario;
import com.cesde.library.Repositorio.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Optional<Usuario> obtenerUsuarioPorCorreo(String correo){
        return usuarioRepository.findByCorreo(correo);
    }

    public Optional<Usuario> obtenerUsuarioPorId(Long id){
        return usuarioRepository.findById(id);
    }

    public List<Usuario> obtenerTodosUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario guardarUsuario(Usuario usuario){
        // Aquí se asume que si es nuevo (no tiene id) validamos correo único
        if (usuario.getId() == null) {
            Optional<Usuario> existente = usuarioRepository.findByCorreo(usuario.getCorreo());
            if (existente.isPresent()) {
                throw new RuntimeException("El correo ya está registrado.");
            }
            // Setear rol por defecto al crear usuario nuevo
            usuario.setRol(Rol.USUARIO); // o Rol.valueOf("USUARIO")
        } else {
            // Para actualización, no lanzamos error si el correo está repetido,
            // podrías agregar validaciones extra si quieres.
        }

        return usuarioRepository.save(usuario);
    }

    public void eliminarUsuario(Long id){
        usuarioRepository.deleteById(id);
    }
}
