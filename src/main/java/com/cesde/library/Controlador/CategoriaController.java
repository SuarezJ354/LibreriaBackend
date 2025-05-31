    package com.cesde.library.Controlador;

    import com.cesde.library.Modelo.Categoria;
    import com.cesde.library.Servicios.CategoriaService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;
    import java.util.Optional;

    @RestController
    @RequestMapping("/categorias")
    public class CategoriaController {

        @Autowired
        private CategoriaService categoriaService;

        @PostMapping
        public ResponseEntity<Categoria> registrarCategoria(@RequestBody Categoria categoria) {
            Categoria nuevaCategoria = categoriaService.guardarCategoria(categoria);
            return ResponseEntity.ok(nuevaCategoria);
        }

        @GetMapping
        public ResponseEntity<List<Categoria>> listarCategorias() {
            return ResponseEntity.ok(categoriaService.listarCategorias());
        }

        @GetMapping("/{id}")
        public ResponseEntity<Categoria> obtenerCategoria(@PathVariable Long id) {
            Optional<Categoria> categoria = categoriaService.obtenerCategoriaPorId(id);
            return categoria.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<String> eliminarCategoria(@PathVariable Long id) {
            categoriaService.eliminarCategoria(id);
            return ResponseEntity.ok("Categoría eliminada correctamente");
        }
    }
