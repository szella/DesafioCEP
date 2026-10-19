package br.com.szella.desafiocep.strategy;

import br.com.szella.desafiocep.domain.dto.EnderecoDto;

import java.util.List;
import java.util.Optional;

public interface BuscaEnderecoStrategy {

    Optional<EnderecoDto> buscaPorCEP(String CEP);

    Optional<List<EnderecoDto>> buscaPorLogradouro(String uf, String cidade, String logradouro);
}
