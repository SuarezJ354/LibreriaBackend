package com.cesde.library.Controlador;

import com.cesde.library.Modelo.Prestamo;
import com.cesde.library.Servicios.PrestamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prestamos")
public class PrestamoController {

    @Autowired
    private PrestamoService prestamoService;

    @PostMapping
    public ResponseEntity<Prestamo> registrarPrestamo(@RequestBody Prestamo prestamo) {
        Prestamo nuevoPrestamo = prestamoService.registrarPrestamo(prestamo);
        return ResponseEntity.ok(nuevoPrestamo);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Prestamo>> obtenerPrestamosPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(prestamoService.obtenerPrestamosPorUsuario(usuarioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarPrestamo(@PathVariable Long id) {
        prestamoService.eliminarPrestamo(id);
        return ResponseEntity.ok("Préstamo eliminado correctamente");
    }
}

