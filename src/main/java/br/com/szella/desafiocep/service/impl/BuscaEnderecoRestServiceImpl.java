package br.com.szella.desafiocep.service.impl;

import br.com.szella.desafiocep.domain.dto.EnderecoDto;
import br.com.szella.desafiocep.exception.DependencyUnavailableException;
import br.com.szella.desafiocep.exception.ResourceNotFoundException;
import br.com.szella.desafiocep.service.BuscaEnderecoService;
import br.com.szella.desafiocep.strategy.BuscaEnderecoStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuscaEnderecoRestServiceImpl implements BuscaEnderecoService {

    private final List<BuscaEnderecoStrategy> estrategias;

    @Override
    public EnderecoDto buscaPorCEP(String cep) {
        log.info("Buscar Endereco por CEP.");

        var failures = new ArrayList<DependencyUnavailableException>();
        var result = estrategias.stream()
                .map(strategy -> {
                    try {
                        return strategy.buscaPorCEP(cep);
                    } catch (DependencyUnavailableException e) {
                        failures.add(e);
                        return Optional.<EnderecoDto>empty();
                    }
                })
                .flatMap(Optional::stream)
                .findFirst();

        var enderecoDto = result.orElseGet(() -> {
            if (!failures.isEmpty()) {
                throw failures.getFirst();
            }
            throw new ResourceNotFoundException("Endereço não encontrado");
        });
        log.info("Buscar Endereco por CEP com sucesso.");
        return enderecoDto;
    }

    @Override
    public List<EnderecoDto> buscaPorLogradouro(String uf, String cidade, String logradouro) {
        log.info("Buscar Endereco por Logradouro.");

        var failures = new ArrayList<DependencyUnavailableException>();
        var result = estrategias.stream()
                .map(strategy -> {
                    try {
                        return strategy.buscaPorLogradouro(uf, cidade, logradouro);
                    } catch (DependencyUnavailableException e) {
                        failures.add(e);
                        return Optional.<List<EnderecoDto>>empty();
                    }
                })
                .flatMap(Optional::stream)
                .findFirst();

        var enderecosDto = result.orElseGet(() -> {
            if (!failures.isEmpty()) {
                throw failures.getFirst();
            }
            throw new ResourceNotFoundException("Endereço não encontrado");
        });
        log.info("Buscar Endereco por Logradouro com sucesso.");
        return enderecosDto;
    }
}
