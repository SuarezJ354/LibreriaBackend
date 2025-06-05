package com.cesde.library.Controlador;

import com.cesde.library.Servicios.HighlightService;
import com.cesde.library.Utils.JwtUtils;
import com.cesde.library.dto.HighlightCreateDto;
import com.cesde.library.dto.HighlightUpdateDto;
import com.cesde.library.dto.HighlightResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/highlights")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class HighlightController {

    private final HighlightService highlightService;
    private final JwtUtils jwtUtils;

    @GetMapping("/{capituloId}")
    public ResponseEntity<List<HighlightResponseDto>> getHighlightsByCapitulo(
            @PathVariable Long capituloId,
            HttpServletRequest request
    ) {
        try {
            Long usuarioId = getUserIdFromRequest(request);
            List<HighlightResponseDto> highlights = highlightService.getHighlightsByCapitulo(usuarioId, capituloId);

            log.info("Devolviendo {} resaltados para usuario {} en capítulo {}",
                    highlights.size(), usuarioId, capituloId);

            return ResponseEntity.ok(highlights);
        } catch (Exception e) {
            log.error("Error al obtener resaltados: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Crear un nuevo resaltado
    @PostMapping
    public ResponseEntity<HighlightResponseDto> createHighlight(
            @Valid @RequestBody HighlightCreateDto createDto,
            HttpServletRequest request
    ) {
        try {
            Long usuarioId = getUserIdFromRequest(request);
            HighlightResponseDto highlight = highlightService.createHighlight(usuarioId, createDto);

            log.info("Resaltado creado exitosamente para usuario {}", usuarioId);

            return ResponseEntity.status(HttpStatus.CREATED).body(highlight);
        } catch (Exception e) {
            log.error("Error al crear resaltado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Actualizar color de un resaltado
    @PutMapping("/{highlightId}")
    public ResponseEntity<HighlightResponseDto> updateHighlightColor(
            @PathVariable Long highlightId,
            @Valid @RequestBody HighlightUpdateDto updateDto,
            HttpServletRequest request
    ) {
        try {
            Long usuarioId = getUserIdFromRequest(request);
            HighlightResponseDto highlight = highlightService.updateHighlightColor(usuarioId, highlightId, updateDto);

            log.info("Color del resaltado {} actualizado para usuario {}", highlightId, usuarioId);

            return ResponseEntity.ok(highlight);
        } catch (Exception e) {
            log.error("Error al actualizar resaltado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Eliminar un resaltado
    @DeleteMapping("/{highlightId}")
    public ResponseEntity<Map<String, String>> deleteHighlight(
            @PathVariable Long highlightId,
            HttpServletRequest request
    ) {
        try {
            Long usuarioId = getUserIdFromRequest(request);
            highlightService.deleteHighlight(usuarioId, highlightId);

            log.info("Resaltado {} eliminado para usuario {}", highlightId, usuarioId);

            return ResponseEntity.ok(Map.of(
                    "message", "Resaltado eliminado exitosamente",
                    "highlightId", highlightId.toString()
            ));
        } catch (Exception e) {
            log.error("Error al eliminar resaltado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Error al eliminar el resaltado"));
        }
    }

    // Obtener todos los resaltados del usuario
    @GetMapping("/user")
    public ResponseEntity<List<HighlightResponseDto>> getAllUserHighlights(
            HttpServletRequest request
    ) {
        try {
            Long usuarioId = getUserIdFromRequest(request);
            List<HighlightResponseDto> highlights = highlightService.getAllUserHighlights(usuarioId);

            log.info("Devolviendo {} resaltados para usuario {}", highlights.size(), usuarioId);

            return ResponseEntity.ok(highlights);
        } catch (Exception e) {
            log.error("Error al obtener resaltados del usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Obtener estadísticas de resaltados
    @GetMapping("/stats/{capituloId}")
    public ResponseEntity<Map<String, Object>> getHighlightStats(
            @PathVariable Long capituloId,
            HttpServletRequest request
    ) {
        try {
            Long usuarioId = getUserIdFromRequest(request);
            Long count = highlightService.getHighlightCount(usuarioId, capituloId);

            return ResponseEntity.ok(Map.of(
                    "capituloId", capituloId,
                    "highlightCount", count,
                    "usuarioId", usuarioId
            ));
        } catch (Exception e) {
            log.error("Error al obtener estadísticas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Método auxiliar para extraer el ID del usuario del token JWT
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtils.extractUserId(token);
        }
        throw new RuntimeException("Token de autorización requerido");
    }
}