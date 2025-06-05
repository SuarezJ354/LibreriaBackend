package com.cesde.library.Controlador;

import com.cesde.library.dto.LibroDTO;
import com.cesde.library.Modelo.Capitulo;
import com.cesde.library.Modelo.Categoria;
import com.cesde.library.Modelo.Libro;
import com.cesde.library.Repositorio.CategoriaRepository;
import com.cesde.library.Servicios.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:2007")
@RestController
@RequestMapping("/libros")
public class LibroController {

    @Autowired
    private LibroService libroService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private final String PDF_DIR = System.getenv("UPLOAD_DIR") != null
            ? System.getenv("UPLOAD_DIR")
            : "/tmp/library-uploads/pdf/";

    // 📌 Registrar un nuevo libro con archivo y JSON
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registrarLibro(
            @RequestPart("libro") LibroDTO libroDTO,
            @RequestPart(value = "archivo", required = false) MultipartFile archivoPdf
    ) {
        try {
            // 🔍 DEBUG: Mostrar la ruta que se está usando
            System.out.println("🔍 PDF_DIR actual: " + PDF_DIR);
            System.out.println("🔍 Directorio completo: " + new File(PDF_DIR).getAbsolutePath());

            Libro libro = new Libro();
            libro.setTitulo(libroDTO.getTitulo());
            libro.setAutor(libroDTO.getAutor());
            libro.setAnioPublicacion(libroDTO.getAnioPublicacion());
            libro.setDescripcion(libroDTO.getDescripcion());
            libro.setImagenPortada(libroDTO.getImagenPortada());
            libro.setEsGratuito(libroDTO.getEsGratuito());
            libro.setPrecioDescarga(libroDTO.getPrecioDescarga());
            libro.setCapitulosGratis(libroDTO.getCapitulosGratis());
            libro.setTotalCapitulos(libroDTO.getTotalCapitulos());
            libro.setEstado(Libro.EstadoLibro.valueOf(libroDTO.getEstado()));

            Optional<Categoria> categoria = categoriaRepository.findById(libroDTO.getCategoriaId());
            if (categoria.isEmpty()) {
                return ResponseEntity.badRequest().body("Categoría no encontrada");
            }
            libro.setCategoria(categoria.get());

            if (archivoPdf != null && !archivoPdf.isEmpty()) {
                System.out.println("📁 Procesando archivo: " + archivoPdf.getOriginalFilename());

                String extension = StringUtils.getFilenameExtension(archivoPdf.getOriginalFilename());
                if (extension == null || !extension.toLowerCase().equals("pdf")) {
                    return ResponseEntity.badRequest().body("Solo se permiten archivos PDF");
                }

                String nuevoNombre = UUID.randomUUID() + "." + extension;
                File directorioDestino = new File(PDF_DIR);

                System.out.println("📂 Directorio destino: " + directorioDestino.getAbsolutePath());
                System.out.println("📂 ¿Existe el directorio?: " + directorioDestino.exists());

                // Crear directorio si no existe
                if (!directorioDestino.exists()) {
                    boolean creado = directorioDestino.mkdirs();
                    System.out.println("📂 ¿Se creó el directorio?: " + creado);
                    if (!creado) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error: No se pudo crear el directorio de uploads en: " + directorioDestino.getAbsolutePath());
                    }
                }

                File destino = new File(PDF_DIR + nuevoNombre);
                System.out.println("📄 Archivo destino: " + destino.getAbsolutePath());

                try {
                    archivoPdf.transferTo(destino);
                    libro.setArchivoPdf(PDF_DIR + nuevoNombre);
                    System.out.println("✅ Archivo guardado exitosamente en: " + destino.getAbsolutePath());
                } catch (Exception e) {
                    System.err.println("❌ Error al transferir archivo: " + e.getMessage());
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error al guardar el archivo: " + e.getMessage());
                }
            }

            Libro nuevoLibro = libroService.guardarLibro(libro);
            System.out.println("✅ Libro guardado con ID: " + nuevoLibro.getId());
            return ResponseEntity.ok(nuevoLibro);

        } catch (Exception e) {
            System.err.println("❌ Error general: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar el libro: " + e.getMessage());
        }
    }
    // Obtener todos los libros
    @GetMapping
    public ResponseEntity<?> listarLibros() {
        try {
            System.out.println("🔍 Iniciando GET /libros");

            List<Libro> libros = libroService.listarLibros();

            System.out.println("✅ Libros obtenidos: " + (libros != null ? libros.size() : 0));

            return ResponseEntity.ok(libros);

        } catch (Exception e) {
            System.err.println("❌ ERROR en GET /libros: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener libros: " + e.getMessage());
        }
    }

    // Obtener libro por ID
    @GetMapping("/{id}")
    public ResponseEntity<Libro> obtenerLibro(@PathVariable Long id) {
        Optional<Libro> libro = libroService.obtenerLibroPorId(id);
        return libro.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    //Eliminar libro por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarLibro(@PathVariable Long id) {
        libroService.eliminarLibro(id);
        return ResponseEntity.ok("Libro eliminado correctamente");
    }

    // Listar solo libros activos
    @GetMapping("/activos")
    public ResponseEntity<List<Libro>> listarLibrosActivos() {
        return ResponseEntity.ok(libroService.listarLibrosActivos());
    }

    // Obtener capítulos accesibles según permisos
    @GetMapping("/{id}/capitulos")
    public ResponseEntity<List<Capitulo>> obtenerCapitulos(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean usuarioRegistrado,
            @RequestParam(defaultValue = "false") boolean haPagado
    ) {
        if (libroService.puedeLeerTodoElLibro(id, usuarioRegistrado, haPagado)) {
            return ResponseEntity.ok(libroService.obtenerTodosLosCapitulos(id));
        } else {
            return ResponseEntity.ok(libroService.obtenerCapitulosGratuitos(id));
        }
    }

    //Obtener un capítulo si tiene permiso
    @GetMapping("/{id}/capitulos/{numero}")
    public ResponseEntity<Capitulo> obtenerCapitulo(
            @PathVariable Long id,
            @PathVariable Integer numero,
            @RequestParam(defaultValue = "false") boolean usuarioRegistrado,
            @RequestParam(defaultValue = "false") boolean haPagado
    ) {
        if (!libroService.puedeAccederCapitulo(id, numero, usuarioRegistrado, haPagado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return libroService.obtenerCapitulo(id, numero)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //  Descargar PDF si el usuario tiene permiso
    @GetMapping("/{id}/descargar")
    public ResponseEntity<byte[]> descargarPdf(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean usuarioRegistrado,
            @RequestParam(defaultValue = "false") boolean haPagado
    ) {
        if (!libroService.puedeDescargarPDF(id, usuarioRegistrado, haPagado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Libro> libroOpt = libroService.obtenerLibroPorId(id);
        if (libroOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Libro libro = libroOpt.get();
        try {
            Path path = Paths.get(libro.getArchivoPdf());
            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }

            byte[] pdfBytes = Files.readAllBytes(path);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(path.getFileName().toString()).build());
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/categoria/{nombre}")
    public ResponseEntity<List<Libro>> obtenerLibrosPorCategoria(@PathVariable String nombre) {
        List<Libro> libros = libroService.obtenerLibrosPorCategoriaNombre(nombre);
        return ResponseEntity.ok(libros);
    }

    // Agregar este método al LibroController

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> actualizarLibro(
            @PathVariable Long id,
            @RequestPart("libro") LibroDTO libroDTO,
            @RequestPart(value = "archivo", required = false) MultipartFile archivoPdf
    ) {
        try {
            // Verificar si el libro existe
            Optional<Libro> libroExistente = libroService.obtenerLibroPorId(id);
            if (libroExistente.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Libro libro = libroExistente.get();

            // Actualizar los campos
            libro.setTitulo(libroDTO.getTitulo());
            libro.setAutor(libroDTO.getAutor());
            libro.setAnioPublicacion(libroDTO.getAnioPublicacion());
            libro.setDescripcion(libroDTO.getDescripcion());
            libro.setImagenPortada(libroDTO.getImagenPortada());
            libro.setEsGratuito(libroDTO.getEsGratuito());
            libro.setPrecioDescarga(libroDTO.getPrecioDescarga());
            libro.setCapitulosGratis(libroDTO.getCapitulosGratis());
            libro.setTotalCapitulos(libroDTO.getTotalCapitulos());
            libro.setEstado(Libro.EstadoLibro.valueOf(libroDTO.getEstado()));

            // Actualizar categoría si cambió
            Optional<Categoria> categoria = categoriaRepository.findById(libroDTO.getCategoriaId());
            if (categoria.isEmpty()) {
                return ResponseEntity.badRequest().body("Categoría no encontrada");
            }
            libro.setCategoria(categoria.get());

            // Si se proporciona un nuevo archivo PDF
            if (archivoPdf != null && !archivoPdf.isEmpty()) {
                System.out.println("📁 Actualizando archivo: " + archivoPdf.getOriginalFilename());

                String extension = StringUtils.getFilenameExtension(archivoPdf.getOriginalFilename());
                if (extension == null || !extension.toLowerCase().equals("pdf")) {
                    return ResponseEntity.badRequest().body("Solo se permiten archivos PDF");
                }

                // Eliminar archivo anterior si existe
                if (libro.getArchivoPdf() != null) {
                    File archivoAnterior = new File(libro.getArchivoPdf());
                    if (archivoAnterior.exists()) {
                        archivoAnterior.delete();
                        System.out.println("🗑️ Archivo anterior eliminado: " + archivoAnterior.getName());
                    }
                }

                // Guardar nuevo archivo
                String nuevoNombre = UUID.randomUUID() + "." + extension;
                File directorioDestino = new File(PDF_DIR);

                if (!directorioDestino.exists()) {
                    directorioDestino.mkdirs();
                }

                File destino = new File(PDF_DIR + nuevoNombre);
                archivoPdf.transferTo(destino);
                libro.setArchivoPdf(PDF_DIR + nuevoNombre);
                System.out.println("✅ Nuevo archivo guardado: " + destino.getAbsolutePath());
            }

            Libro libroActualizado = libroService.guardarLibro(libro);
            System.out.println("✅ Libro actualizado con ID: " + libroActualizado.getId());
            return ResponseEntity.ok(libroActualizado);

        } catch (Exception e) {
            System.err.println("❌ Error al actualizar libro: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar el libro: " + e.getMessage());
        }
    }

}