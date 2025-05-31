package com.cesde.library.Servicios;

import com.cesde.library.Modelo.Libro;
import com.cesde.library.Modelo.Usuario;
import com.cesde.library.Modelo.Favorito;
import com.cesde.library.Repositorio.FavoritoRepository;
import com.cesde.library.Repositorio.LibroRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final LibroRepository libroRepository;

    public FavoritoService(FavoritoRepository favoritoRepository, LibroRepository libroRepository) {
        this.favoritoRepository = favoritoRepository;
        this.libroRepository = libroRepository;
    }

    public Favorito agregarFavorito(Usuario usuario, Long libroId) {
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        Optional<Favorito> existente = favoritoRepository.findByUsuarioAndLibro(usuario, libro);
        if (existente.isPresent()) {
            return existente.get(); // Ya es favorito
        }

        Favorito favorito = new Favorito();
        favorito.setUsuario(usuario);
        favorito.setLibro(libro);
        return favoritoRepository.save(favorito);
    }


    public List<Favorito> obtenerFavoritosDeUsuario(Usuario usuario) {
        return favoritoRepository.findByUsuario(usuario);
    }

    @Transactional
    public void eliminarFavorito(Usuario usuario, Long libroId) {
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        favoritoRepository.deleteByUsuarioAndLibro(usuario, libro);
    }
}
