package br.com.szella.desafiocep.domain.mapper;

import br.com.szella.desafiocep.domain.input.EnderecoInput;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class EnderecoMapperTest {

    private final EnderecoMapper enderecoMapper = Mappers.getMapper(EnderecoMapper.class);

    @Test
    void mapsEnderecoInputFieldsToResponse() {
        var input = new EnderecoInput(
                "01001-000",
                "Praca da Se",
                null,
                null,
                null,
                "Sao Paulo",
                "SP",
                null,
                null,
                "11");

        var response = enderecoMapper.enderecoInputParaResponse(input);

        assertThat(response.cep()).isEqualTo("01001000");
        assertThat(response.logradouro()).isEqualTo("Praca da Se");
        assertThat(response.localidade()).isEqualTo("Sao Paulo");
        assertThat(response.uf()).isEqualTo("SP");
    }
}
