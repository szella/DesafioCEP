package br.com.szella.desafiocep.strategy.impl;

import br.com.szella.desafiocep.domain.dto.EnderecoDto;
import br.com.szella.desafiocep.domain.input.EnderecoInput;
import br.com.szella.desafiocep.domain.mapper.EnderecoMapper;
import br.com.szella.desafiocep.exception.DependencyUnavailableException;
import br.com.szella.desafiocep.repository.EnderecoRepository;
import br.com.szella.desafiocep.strategy.BuscaEnderecoStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Order(1)
@Component("BuscaEnderecoBD")
@RequiredArgsConstructor
public class BuscaEnderecoBDStrategy implements BuscaEnderecoStrategy {

    private final EnderecoRepository repository;
    private final EnderecoMapper mapper;

    @Override
    public Optional<EnderecoDto> buscaPorCEP(String cep) {
        try {
            var endereco = repository.findByCep(EnderecoInput.padronizarCep(cep));
            endereco.ifPresent(ignored -> log.info("Endereco encontrado no banco."));
            return endereco.map(mapper::enderecoEntityParaDto);
        } catch (DataAccessException e) {
            log.error("Falha ao consultar endereço no banco de dados.", e);
            throw new DependencyUnavailableException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "O serviço de persistência está temporariamente indisponível.",
                    e);
        }
    }

    @Override
    public Optional<List<EnderecoDto>> buscaPorLogradouro(String uf, String cidade, String logradouro) {
        try {
            var entities = repository.findByUfAndLocalidadeAndLogradouro(uf, cidade, logradouro);
            if (entities.isEmpty()) {
                return Optional.empty();
            }
            log.info("Endereco por logradouro encontrado no banco.");
            return Optional.of(mapper.enderecosListEntityParaListDto(entities));
        } catch (DataAccessException e) {
            log.error("Falha ao consultar logradouro no banco de dados.", e);
            throw new DependencyUnavailableException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "O serviço de persistência está temporariamente indisponível.",
                    e);
        }
    }
}
