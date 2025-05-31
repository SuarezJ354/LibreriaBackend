package com.cesde.library.Repositorio;

import com.cesde.library.Modelo.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {
    List<Libro> findByAutor(String autor); //Buscar libro por autor

    List<Libro> findByEstado(Libro.EstadoLibro estado);

    List<Libro> findByTituloContainingIgnoreCase(String titulo);

    List<Libro> findByAutorContainingIgnoreCase(String autor);

    List<Libro> findByCategoriaId(Long categoriaId);

    List<Libro> findByCategoriaNombreIgnoreCase(String nombre);

}
