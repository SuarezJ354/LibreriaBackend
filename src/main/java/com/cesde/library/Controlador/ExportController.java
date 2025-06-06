package com.cesde.library.Controlador;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/export")
public class ExportController {

    private final RestTemplate restTemplate = new RestTemplate();

    // URL base de tu microservicio FastAPI (ajusta la URL según deployment)
    @Value("${fastapi.url}")
    private String fastApiBaseUrl;

    @GetMapping("/csv/{tableName}")
    public ResponseEntity<Resource> exportTable(@PathVariable String tableName) {
        try {
            // Validación básica
            if (!tableName.matches("^[a-zA-Z0-9_]+$")) {
                return ResponseEntity.badRequest().build();
            }

            String url = fastApiBaseUrl + "/export/" + tableName;

            // Hacer GET al microservicio FastAPI
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    byte[].class);

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            byte[] csvData = response.getBody();

            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(csvData));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename(tableName + ".csv")
                    .build());
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentLength(csvData.length);

            return new ResponseEntity<>(resource, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
