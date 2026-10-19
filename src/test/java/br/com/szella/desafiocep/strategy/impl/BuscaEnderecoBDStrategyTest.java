package br.com.szella.desafiocep.strategy.impl;

import br.com.szella.desafiocep.domain.entity.EnderecoEntity;
import br.com.szella.desafiocep.domain.dto.EnderecoDto;
import br.com.szella.desafiocep.domain.mapper.EnderecoMapper;
import br.com.szella.desafiocep.exception.DependencyUnavailableException;
import br.com.szella.desafiocep.repository.EnderecoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscaEnderecoBDStrategyTest {

    @Mock
    private EnderecoRepository repository;

    @Test
    void reportsDatabaseFailureInsteadOfTreatingItAsCacheMiss() {
        when(repository.findByCep("01001000"))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));
        var strategy = new BuscaEnderecoBDStrategy(
                repository,
                Mappers.getMapper(EnderecoMapper.class));

        assertThatThrownBy(() -> strategy.buscaPorCEP("01001-000"))
                .isInstanceOf(DependencyUnavailableException.class)
                .satisfies(exception -> {
                    var unavailable = (DependencyUnavailableException) exception;
                    org.assertj.core.api.Assertions.assertThat(unavailable.getStatus())
                            .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                });
    }

    @Test
    void returnsEmptyForCepCacheMissAndNormalizesCepBeforeLookup() {
        when(repository.findByCep("01001000")).thenReturn(Optional.empty());
        var strategy = strategy();

        assertThat(strategy.buscaPorCEP("01001-000")).isEmpty();
        verify(repository).findByCep("01001000");
    }

    @Test
    void mapsCachedAddressToDto() {
        var entity = EnderecoEntity.builder()
                .cep("01001000")
                .logradouro("Praça da Sé")
                .localidade("São Paulo")
                .uf("SP")
                .build();
        when(repository.findByCep("01001000")).thenReturn(Optional.of(entity));

        var result = strategy().buscaPorCEP("01001000");

        assertThat(result).contains(new EnderecoDto(
                "01001000", "Praça da Sé", null, null, null, "São Paulo", "SP", null, null, null));
    }

    @Test
    void returnsEmptyForStreetCacheMiss() {
        when(repository.findByUfAndLocalidadeAndLogradouro("SP", "Sao Paulo", "Rua A"))
                .thenReturn(List.of());

        assertThat(strategy().buscaPorLogradouro("SP", "Sao Paulo", "Rua A")).isEmpty();
    }

    @Test
    void mapsCachedStreetResults() {
        var entity = EnderecoEntity.builder()
                .cep("01001000")
                .logradouro("Rua A")
                .localidade("Sao Paulo")
                .uf("SP")
                .build();
        when(repository.findByUfAndLocalidadeAndLogradouro("SP", "Sao Paulo", "Rua A"))
                .thenReturn(List.of(entity));

        var result = strategy().buscaPorLogradouro("SP", "Sao Paulo", "Rua A");

        assertThat(result).isPresent().hasValueSatisfying(addresses ->
                assertThat(addresses).hasSize(1).extracting(EnderecoDto::cep).containsExactly("01001000"));
    }

    @Test
    void reportsDatabaseFailureForStreetSearch() {
        when(repository.findByUfAndLocalidadeAndLogradouro(eq("SP"), eq("Sao Paulo"), eq("Rua A")))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));

        assertThatThrownBy(() -> strategy().buscaPorLogradouro("SP", "Sao Paulo", "Rua A"))
                .isInstanceOf(DependencyUnavailableException.class);
    }

    private BuscaEnderecoBDStrategy strategy() {
        return new BuscaEnderecoBDStrategy(repository, Mappers.getMapper(EnderecoMapper.class));
    }
}
