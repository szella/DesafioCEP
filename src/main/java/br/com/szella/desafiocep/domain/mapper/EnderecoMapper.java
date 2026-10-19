package br.com.szella.desafiocep.domain.mapper;

import br.com.szella.desafiocep.domain.dto.EnderecoDto;
import br.com.szella.desafiocep.domain.entity.EnderecoEntity;
import br.com.szella.desafiocep.domain.input.EnderecoInput;
import br.com.szella.desafiocep.domain.response.EnderecoResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EnderecoMapper {
    EnderecoResponse enderecoInputParaResponse(EnderecoInput input);

    EnderecoDto enderecoInputParaDto(EnderecoInput input);

    EnderecoDto enderecoEntityParaDto(EnderecoEntity entity);

    List<EnderecoDto> enderecosListInputParaListDto(List<EnderecoInput> input);

    List<EnderecoDto> enderecosListEntityParaListDto(List<EnderecoEntity> entity);

    List<EnderecoResponse> enderecosListDtoParaListResponse(List<EnderecoDto> Dto);

    EnderecoResponse enderecoDtoParaResponse(EnderecoDto Dto);

    EnderecoEntity enderecoInputParaEntity(EnderecoInput enderecoInput);
}
