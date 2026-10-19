package br.com.szella.desafiocep.service;

import br.com.szella.desafiocep.domain.dto.EnderecoDto;

import java.util.List;

public interface BuscaEnderecoService {
    EnderecoDto buscaPorCEP(String CEP);

    List<EnderecoDto> buscaPorLogradouro(String uf, String cidade, String logradouro);
}
