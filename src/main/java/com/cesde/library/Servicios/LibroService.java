package com.cesde.library.Servicios;

import com.cesde.library.Modelo.Libro;
import com.cesde.library.Modelo.Capitulo;
import com.cesde.library.Repositorio.LibroRepository;
import com.cesde.library.Repositorio.CapituloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LibroService {

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private CapituloRepository capituloRepository;

    public List<Libro> listarLibros(){
        return libroRepository.findAll();
    }

    public List<Libro> listarLibrosActivos(){
        return libroRepository.findByEstado(Libro.EstadoLibro.Disponible);
    }

    public Optional<Libro> obtenerLibroPorId(Long id){
        return libroRepository.findById(id);
    }

    public Libro guardarLibro(Libro libro){
        return libroRepository.save(libro);
    }

    public void eliminarLibro(long id){
        libroRepository.deleteById(id);
    }

    // Métodos específicos para la funcionalidad de lectura
    public List<Capitulo> obtenerCapitulosGratuitos(Long libroId) {
        Optional<Libro> libro = obtenerLibroPorId(libroId);
        if (libro.isPresent()) {
            return capituloRepository.findByLibroIdAndEsGratuitoTrue(libroId);
        }
        return List.of();
    }

    public List<Capitulo> obtenerTodosLosCapitulos(Long libroId) {
        return capituloRepository.findByLibroIdOrderByOrden(libroId);
    }

    public Optional<Capitulo> obtenerCapitulo(Long libroId, Integer numeroCapitulo) {
        return capituloRepository.findByLibroIdAndNumeroCapitulo(libroId, numeroCapitulo);
    }

    public boolean puedeAccederCapitulo(Long libroId, Integer numeroCapitulo, boolean usuarioRegistrado, boolean haPagado) {
        Optional<Libro> libro = obtenerLibroPorId(libroId);
        Optional<Capitulo> capitulo = obtenerCapitulo(libroId, numeroCapitulo);

        if (libro.isEmpty() || capitulo.isEmpty()) {
            return false;
        }

        // Si el libro es completamente gratuito
        if (libro.get().getEsGratuito()) {
            return true;
        }

        // Si el capítulo específico es gratuito
        if (capitulo.get().getEsGratuito()) {
            return true;
        }

        // Si está dentro de los capítulos gratuitos
        if (numeroCapitulo <= libro.get().getCapitulosGratis()) {
            return true;
        }

        // Si el usuario ha pagado por el libro
        if (haPagado) {
            return true;
        }

        return false;
    }

    public boolean puedeDescargarPDF(Long libroId, boolean usuarioRegistrado, boolean haPagado) {
        Optional<Libro> libro = obtenerLibroPorId(libroId);

        if (libro.isEmpty()) {
            return false;
        }

        // Si el libro es completamente gratuito
        if (libro.get().getEsGratuito()) {
            return usuarioRegistrado; // Requiere registro aunque sea gratis
        }

        // Si ha pagado por el libro
        return haPagado;
    }
    public boolean puedeLeerTodoElLibro(Long libroId, boolean usuarioRegistrado, boolean haPagado) {
        Optional<Libro> libro = obtenerLibroPorId(libroId);
        if (libro.isEmpty()) {
            return false;
        }
        if (libro.get().getEsGratuito() && usuarioRegistrado) {
            return true;
        }
        if (haPagado) {
            return true;
        }
        return false;
    }
    public List<Libro> obtenerLibrosPorCategoriaNombre(String nombre) {
        return libroRepository.findByCategoriaNombreIgnoreCase(nombre.replace("-", " "));
    }


}