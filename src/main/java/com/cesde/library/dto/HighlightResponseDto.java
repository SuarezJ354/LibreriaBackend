package com.cesde.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HighlightResponseDto {

    private Long id;
    private Long capituloId;
    private String texto;
    private Integer startOffset;
    private Integer endOffset;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

