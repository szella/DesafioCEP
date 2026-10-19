package br.com.szella.desafiocep.domain.mapper;

import br.com.szella.desafiocep.domain.dto.LogConsultaDto;
import br.com.szella.desafiocep.domain.entity.LogConsultaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LogConsultaMapper {
    @Mapping(target = "saida", source = "response")
    LogConsultaEntity logConsultaDtoParaEntity(LogConsultaDto logConsultaDto);
}
