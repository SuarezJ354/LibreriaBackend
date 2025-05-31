package com.cesde.library.Controlador;

import com.cesde.library.Modelo.Capitulo;
import com.cesde.library.Servicios.CapituloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:2007") // Permitir acceso desde React
@RestController
@RequestMapping("/capitulos")
public class CapituloController {

    @Autowired
    private CapituloService capituloService;

    // 📌 Listar capítulos de un libro
    @GetMapping("/{libroId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Capitulo>> obtenerCapitulosPorLibro(@PathVariable Long libroId) {
        return ResponseEntity.ok(capituloService.obtenerCapitulosPorLibro(libroId));
    }

    // 📌 Listar capítulos gratuitos de un libro
    @GetMapping("/{libroId}/gratuitos")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Capitulo>> obtenerCapitulosGratuitos(@PathVariable Long libroId) {
        return ResponseEntity.ok(capituloService.obtenerCapitulosGratuitos(libroId));
    }

    // 📌 Obtener un capítulo específico por número
    @GetMapping("/{libroId}/{numeroCapitulo}")
    @Transactional(readOnly = true)
    public ResponseEntity<Capitulo> obtenerCapituloPorNumero(@PathVariable Long libroId, @PathVariable Integer numeroCapitulo) {
        Optional<Capitulo> capitulo = capituloService.obtenerCapituloPorNumero(libroId, numeroCapitulo);
        return capitulo.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 📌 Registrar un nuevo capítulo
    @PostMapping
    @Transactional
    public ResponseEntity<Capitulo> registrarCapitulo(@RequestBody Capitulo capitulo) {
        Capitulo nuevoCapitulo = capituloService.guardarCapitulo(capitulo);
        return ResponseEntity.ok(nuevoCapitulo);
    }

    // 📌 Eliminar capítulo por ID
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<String> eliminarCapitulo(@PathVariable Long id) {
        capituloService.eliminarCapitulo(id);
        return ResponseEntity.ok("Capítulo eliminado correctamente");
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Capitulo> actualizarCapitulo(@PathVariable Long id, @RequestBody Capitulo capitulo) {
        capitulo.setId(id); // Asegurar que el ID coincida
        Capitulo capituloActualizado = capituloService.guardarCapitulo(capitulo);
        return ResponseEntity.ok(capituloActualizado);
    }
}