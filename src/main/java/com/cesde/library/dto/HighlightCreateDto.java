package com.cesde.library.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

// DTO para crear un nuevo resaltado
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HighlightCreateDto {

    @NotNull(message = "El ID del capítulo es requerido")
    private Long capituloId;

    @NotBlank(message = "El texto del resaltado es requerido")
    private String texto;

    @NotNull(message = "El offset inicial es requerido")
    @Min(value = 0, message = "El offset inicial debe ser mayor o igual a 0")
    private Integer startOffset;

    @NotNull(message = "El offset final es requerido")
    @Min(value = 0, message = "El offset final debe ser mayor o igual a 0")
    private Integer endOffset;

    private String color = "#ffeb3b";
}