package com.cesde.library.Servicios;


import com.cesde.library.Exceptions.ResourceNotFoundException;
import com.cesde.library.Modelo.Highlight;
import com.cesde.library.Repositorio.HighlightRepository;
import com.cesde.library.dto.HighlightCreateDto;
import com.cesde.library.dto.HighlightResponseDto;
import com.cesde.library.dto.HighlightUpdateDto;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HighlightService {

    private final HighlightRepository highlightRepository;

    // Obtener todos los resaltados de un usuario para un capítulo
    @Transactional(readOnly = true)
    public List<HighlightResponseDto> getHighlightsByCapitulo(Long usuarioId, Long capituloId) {
        log.info("Obteniendo resaltados para usuario {} en capítulo {}", usuarioId, capituloId);

        List<Highlight> highlights = highlightRepository
                .findByUsuarioIdAndCapituloIdOrderByStartOffsetAsc(usuarioId, capituloId);

        return highlights.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    // Crear un nuevo resaltado
    public HighlightResponseDto createHighlight(Long usuarioId, HighlightCreateDto createDto) {
        log.info("Creando resaltado para usuario {} en capítulo {}", usuarioId, createDto.getCapituloId());

        // Validaciones
        validateHighlightData(createDto);

        // Verificar superposición (opcional - puedes comentar si permites superposición)
        List<Highlight> overlapping = highlightRepository.findOverlappingHighlights(
                usuarioId, createDto.getCapituloId(),
                createDto.getStartOffset(), createDto.getEndOffset()
        );

        if (!overlapping.isEmpty()) {
            log.warn("Se encontraron resaltados superpuestos para usuario {}", usuarioId);
            // Puedes decidir si lanzar excepción o permitir la superposición
            // throw new ValidationException("Ya existe un resaltado en esta posición");
        }

        Highlight highlight = new Highlight();
        highlight.setUsuarioId(usuarioId);
        highlight.setCapituloId(createDto.getCapituloId());
        highlight.setTexto(createDto.getTexto());
        highlight.setStartOffset(createDto.getStartOffset());
        highlight.setEndOffset(createDto.getEndOffset());
        highlight.setColor(createDto.getColor() != null ? createDto.getColor() : "#ffeb3b");

        Highlight savedHighlight = highlightRepository.save(highlight);
        log.info("Resaltado creado con ID: {}", savedHighlight.getId());

        return convertToResponseDto(savedHighlight);
    }

    // Actualizar color de un resaltado
    public HighlightResponseDto updateHighlightColor(Long usuarioId, Long highlightId, HighlightUpdateDto updateDto) {
        log.info("Actualizando color del resaltado {} para usuario {}", highlightId, usuarioId);

        Highlight highlight = highlightRepository.findByIdAndUsuarioId(highlightId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Resaltado no encontrado"));

        if (updateDto.getColor() != null && !updateDto.getColor().trim().isEmpty()) {
            highlight.setColor(updateDto.getColor().trim());
        }

        Highlight updatedHighlight = highlightRepository.save(highlight);
        log.info("Color del resaltado {} actualizado", highlightId);

        return convertToResponseDto(updatedHighlight);
    }

    // Eliminar un resaltado
    public void deleteHighlight(Long usuarioId, Long highlightId) {
        log.info("Eliminando resaltado {} del usuario {}", highlightId, usuarioId);

        Highlight highlight = highlightRepository.findByIdAndUsuarioId(highlightId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Resaltado no encontrado"));

        highlightRepository.delete(highlight);
        log.info("Resaltado {} eliminado exitosamente", highlightId);
    }

    // Obtener todos los resaltados de un usuario
    @Transactional(readOnly = true)
    public List<HighlightResponseDto> getAllUserHighlights(Long usuarioId) {
        log.info("Obteniendo todos los resaltados del usuario {}", usuarioId);

        List<Highlight> highlights = highlightRepository.findByUsuarioIdOrderByCreatedAtDesc(usuarioId);

        return highlights.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    // Obtener estadísticas de resaltados
    @Transactional(readOnly = true)
    public Long getHighlightCount(Long usuarioId, Long capituloId) {
        return highlightRepository.countByUsuarioIdAndCapituloId(usuarioId, capituloId);
    }

    // Validaciones privadas
    private void validateHighlightData(HighlightCreateDto createDto) {
        if (createDto.getStartOffset() >= createDto.getEndOffset()) {
            throw new ValidationException("El offset inicial debe ser menor que el offset final");
        }

        if (createDto.getTexto().trim().isEmpty()) {
            throw new ValidationException("El texto del resaltado no puede estar vacío");
        }

        if (createDto.getTexto().length() > 1000) {
            throw new ValidationException("El texto del resaltado no puede exceder 1000 caracteres");
        }
    }

    // Convertir entidad a DTO
    private HighlightResponseDto convertToResponseDto(Highlight highlight) {
        HighlightResponseDto dto = new HighlightResponseDto();
        dto.setId(highlight.getId());
        dto.setCapituloId(highlight.getCapituloId());
        dto.setTexto(highlight.getTexto());
        dto.setStartOffset(highlight.getStartOffset());
        dto.setEndOffset(highlight.getEndOffset());
        dto.setColor(highlight.getColor());
        dto.setCreatedAt(highlight.getCreatedAt());
        dto.setUpdatedAt(highlight.getUpdatedAt());
        return dto;
    }
}