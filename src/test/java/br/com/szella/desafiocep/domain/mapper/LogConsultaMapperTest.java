package br.com.szella.desafiocep.domain.mapper;

import br.com.szella.desafiocep.domain.dto.LogConsultaDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LogConsultaMapperTest {

    private final LogConsultaMapper mapper = Mappers.getMapper(LogConsultaMapper.class);

    @Test
    void preservesEventIdForIdempotentAuditPersistence() {
        var eventId = UUID.randomUUID();
        var dto = new LogConsultaDto(
                eventId,
                "/api/buscar/por-cep",
                "{\"cep\":\"01001000\"}",
                "{\"encontrado\":true}");

        var entity = mapper.logConsultaDtoParaEntity(dto);

        assertThat(entity.getId()).isEqualTo(eventId);
        assertThat(entity.getCaminho()).isEqualTo(dto.caminho());
        assertThat(entity.getEntrada()).isEqualTo(dto.entrada());
        assertThat(entity.getSaida()).isEqualTo(dto.response());
    }
}
