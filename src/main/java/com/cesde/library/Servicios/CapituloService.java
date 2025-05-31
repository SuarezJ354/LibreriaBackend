package com.cesde.library.Servicios;

import com.cesde.library.Modelo.Capitulo;
import com.cesde.library.Repositorio.CapituloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CapituloService {

    @Autowired
    private CapituloRepository capituloRepository;

    @Transactional(readOnly = true)
    public List<Capitulo> obtenerCapitulosPorLibro(Long libroId) {
        return capituloRepository.findByLibroIdOrderByOrden(libroId);
    }

    @Transactional(readOnly = true)
    public List<Capitulo> obtenerCapitulosGratuitos(Long libroId) {
        return capituloRepository.findByLibroIdAndEsGratuitoTrue(libroId);
    }

    @Transactional(readOnly = true)
    public Optional<Capitulo> obtenerCapituloPorNumero(Long libroId, Integer numeroCapitulo) {
        return capituloRepository.findByLibroIdAndNumeroCapitulo(libroId, numeroCapitulo);
    }

    @Transactional
    public Capitulo guardarCapitulo(Capitulo capitulo) {
        return capituloRepository.save(capitulo);
    }

    @Transactional
    public void eliminarCapitulo(Long id) {
        capituloRepository.deleteById(id);
    }
}