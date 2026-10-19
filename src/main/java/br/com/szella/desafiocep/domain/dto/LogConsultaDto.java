package br.com.szella.desafiocep.domain.dto;

import java.util.UUID;

public record LogConsultaDto(
        UUID id,
        String caminho,
        String entrada,
        String response
) {
    public LogConsultaDto {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
