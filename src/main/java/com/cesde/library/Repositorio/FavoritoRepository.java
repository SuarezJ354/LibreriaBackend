package com.cesde.library.Repositorio;

import com.cesde.library.Modelo.Favorito;
import com.cesde.library.Modelo.Libro;
import com.cesde.library.Modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {
    List<Favorito> findByUsuario(Usuario usuario);
    Optional<Favorito> findByUsuarioAndLibro(Usuario usuario, Libro libro);
    void deleteByUsuarioAndLibro(Usuario usuario, Libro libro);

}
