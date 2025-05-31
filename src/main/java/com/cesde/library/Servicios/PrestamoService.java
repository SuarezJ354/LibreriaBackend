package com.cesde.library.Servicios;

import com.cesde.library.Modelo.Prestamo;
import com.cesde.library.Repositorio.PrestamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrestamoService {

    @Autowired
    private PrestamoRepository prestamoRepository;

    public List<Prestamo> obtenerPrestamosPorUsuario(long usuarioId){
        return prestamoRepository.findByUsuarioId(usuarioId);
    }
    public Prestamo registrarPrestamo(Prestamo prestamo){
        return prestamoRepository.save(prestamo);
    }
    public void eliminarPrestamo(Long id){
        prestamoRepository.deleteById(id);
    }

}
