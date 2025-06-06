package com.cesde.library.Controlador;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource; // Import correcto
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/export")
public class ExportController {

    @GetMapping("/csv/{tableName}")
    public ResponseEntity<Resource> exportTable(@PathVariable String tableName) {
        try {
            // Validación básica (evitar SQL injection)
            if (!tableName.matches("^[a-zA-Z0-9_]+$")) {
                return ResponseEntity.badRequest().build();
            }

            // Crear directorio temporal si no existe
            Path tempDir = Paths.get("temp");
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }

            // Ruta completa del archivo
            String fileName = tableName + ".csv";
            Path filePath = tempDir.resolve(fileName);

            // Ejecutar el script Python
            ProcessBuilder pb = new ProcessBuilder("python3",
                    "src/main/resources/scripts/Export_csv.py",
                    tableName,
                    filePath.toString()); // Pasar la ruta completa al script
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();

            // Verificar si el proceso terminó correctamente
            if (exitCode != 0) {
                System.err.println("Error executing Python script. Exit code: " + exitCode);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            File file = filePath.toFile();
            if (!file.exists()) {
                System.err.println("File not found: " + filePath);
                return ResponseEntity.notFound().build();
            }

            // Crear el recurso correctamente
            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

            // Obtener el tamaño del archivo para el header Content-Length
            long fileSize = file.length();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .contentLength(fileSize)
                    .body(resource); // Sin cast, InputStreamResource implementa Resource

        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (InterruptedException e) {
            System.err.println("Process interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}