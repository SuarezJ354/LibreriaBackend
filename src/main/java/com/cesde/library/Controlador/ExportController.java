package com.cesde.library.Controlador;

import io.undertow.server.handlers.resource.Resource;
import org.springframework.core.io.InputStreamResource;
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

            ProcessBuilder pb = new ProcessBuilder("python3",
                    "src/main/resources/scripts/Export_csv.py", tableName);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor();

            File file = new File(tableName + ".csv");
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body((Resource) resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
