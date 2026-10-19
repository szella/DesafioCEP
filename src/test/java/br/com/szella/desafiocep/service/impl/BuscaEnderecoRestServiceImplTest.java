package br.com.szella.desafiocep.service.impl;

import br.com.szella.desafiocep.domain.dto.EnderecoDto;
import br.com.szella.desafiocep.exception.DependencyUnavailableException;
import br.com.szella.desafiocep.exception.ResourceNotFoundException;
import br.com.szella.desafiocep.strategy.BuscaEnderecoStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscaEnderecoRestServiceImplTest {

    @Mock
    private BuscaEnderecoStrategy databaseStrategy;

    @Mock
    private BuscaEnderecoStrategy externalStrategy;

    @Test
    void fallsBackToNextStrategyWhenFirstHasNoResult() {
        var endereco = new EnderecoDto(
                "01001000", "Praca da Se", null, null, null,
                "Sao Paulo", "SP", null, null, "11");
        when(databaseStrategy.buscaPorCEP("01001-000")).thenReturn(Optional.empty());
        when(externalStrategy.buscaPorCEP("01001-000")).thenReturn(Optional.of(endereco));
        var service = new BuscaEnderecoRestServiceImpl(List.of(databaseStrategy, externalStrategy));

        var result = service.buscaPorCEP("01001-000");

        assertThat(result).isEqualTo(endereco);
        verify(databaseStrategy).buscaPorCEP("01001-000");
        verify(externalStrategy).buscaPorCEP("01001-000");
    }

    @Test
    void reportsNotFoundOnlyWhenStrategiesReturnNoResult() {
        when(databaseStrategy.buscaPorCEP("01001-000")).thenReturn(Optional.empty());
        when(externalStrategy.buscaPorCEP("01001-000")).thenReturn(Optional.empty());
        var service = new BuscaEnderecoRestServiceImpl(List.of(databaseStrategy, externalStrategy));

        assertThatThrownBy(() -> service.buscaPorCEP("01001-000"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void fallsBackToExternalStrategyWhenDatabaseIsUnavailable() {
        var unavailable = new DependencyUnavailableException(
                HttpStatus.SERVICE_UNAVAILABLE, "database unavailable", new IllegalStateException());
        when(databaseStrategy.buscaPorCEP("01001-000")).thenThrow(unavailable);
        var endereco = new EnderecoDto(
                "01001000", "Praca da Se", null, null, null,
                "Sao Paulo", "SP", null, null, "11");
        when(externalStrategy.buscaPorCEP("01001-000")).thenReturn(Optional.of(endereco));
        var service = new BuscaEnderecoRestServiceImpl(List.of(databaseStrategy, externalStrategy));

        assertThat(service.buscaPorCEP("01001-000")).isEqualTo(endereco);
        verify(externalStrategy).buscaPorCEP("01001-000");
    }

    @Test
    void reportsDependencyFailureIfAllStrategiesFailOrMiss() {
        var unavailable = new DependencyUnavailableException(
                HttpStatus.SERVICE_UNAVAILABLE, "database unavailable", new IllegalStateException());
        when(databaseStrategy.buscaPorCEP("01001-000")).thenThrow(unavailable);
        when(externalStrategy.buscaPorCEP("01001-000")).thenReturn(Optional.empty());
        var service = new BuscaEnderecoRestServiceImpl(List.of(databaseStrategy, externalStrategy));

        assertThatThrownBy(() -> service.buscaPorCEP("01001-000"))
                .isSameAs(unavailable);
        verify(externalStrategy).buscaPorCEP("01001-000");
    }

    @Test
    void fallsBackToExternalStrategyWhenStreetIsNotInDatabase() {
        var address = new EnderecoDto(
                "01001000", "Rua A", null, null, null,
                "Sao Paulo", "SP", null, null, "11");
        when(databaseStrategy.buscaPorLogradouro("SP", "Sao Paulo", "Rua A"))
                .thenReturn(Optional.empty());
        when(externalStrategy.buscaPorLogradouro("SP", "Sao Paulo", "Rua A"))
                .thenReturn(Optional.of(List.of(address)));
        var service = new BuscaEnderecoRestServiceImpl(List.of(databaseStrategy, externalStrategy));

        var result = service.buscaPorLogradouro("SP", "Sao Paulo", "Rua A");

        assertThat(result).containsExactly(address);
        verify(databaseStrategy).buscaPorLogradouro("SP", "Sao Paulo", "Rua A");
        verify(externalStrategy).buscaPorLogradouro("SP", "Sao Paulo", "Rua A");
    }

    @Test
    void reportsNotFoundWhenStreetIsAbsentFromAllStrategies() {
        when(databaseStrategy.buscaPorLogradouro("SP", "Sao Paulo", "Rua A"))
                .thenReturn(Optional.empty());
        when(externalStrategy.buscaPorLogradouro("SP", "Sao Paulo", "Rua A"))
                .thenReturn(Optional.empty());
        var service = new BuscaEnderecoRestServiceImpl(List.of(databaseStrategy, externalStrategy));

        assertThatThrownBy(() -> service.buscaPorLogradouro("SP", "Sao Paulo", "Rua A"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(externalStrategy).buscaPorLogradouro("SP", "Sao Paulo", "Rua A");
    }

    @Test
    void fallsBackToExternalStreetSearchWhenDatabaseIsUnavailable() {
        var unavailable = new DependencyUnavailableException(
                HttpStatus.SERVICE_UNAVAILABLE, "database unavailable", new IllegalStateException());
        var address = new EnderecoDto(
                "01001000", "Rua A", null, null, null,
                "Sao Paulo", "SP", null, null, "11");
        when(databaseStrategy.buscaPorLogradouro("SP", "Sao Paulo", "Rua A"))
                .thenThrow(unavailable);
        when(externalStrategy.buscaPorLogradouro("SP", "Sao Paulo", "Rua A"))
                .thenReturn(Optional.of(List.of(address)));
        var service = new BuscaEnderecoRestServiceImpl(List.of(databaseStrategy, externalStrategy));

        assertThat(service.buscaPorLogradouro("SP", "Sao Paulo", "Rua A"))
                .containsExactly(address);
    }

    @Test
    void reportsDependencyFailureWhenStreetSearchMissesAfterDependencyFailure() {
        var unavailable = new DependencyUnavailableException(
                HttpStatus.SERVICE_UNAVAILABLE, "database unavailable", new IllegalStateException());
        when(databaseStrategy.buscaPorLogradouro("SP", "Sao Paulo", "Rua A"))
                .thenThrow(unavailable);
        when(externalStrategy.buscaPorLogradouro("SP", "Sao Paulo", "Rua A"))
                .thenReturn(Optional.empty());
        var service = new BuscaEnderecoRestServiceImpl(List.of(databaseStrategy, externalStrategy));

        assertThatThrownBy(() -> service.buscaPorLogradouro("SP", "Sao Paulo", "Rua A"))
                .isSameAs(unavailable);
    }
}
