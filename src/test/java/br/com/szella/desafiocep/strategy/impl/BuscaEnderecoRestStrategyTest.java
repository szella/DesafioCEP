package br.com.szella.desafiocep.strategy.impl;

import br.com.szella.desafiocep.domain.mapper.EnderecoMapper;
import br.com.szella.desafiocep.domain.input.EnderecoInput;
import br.com.szella.desafiocep.exception.DependencyUnavailableException;
import br.com.szella.desafiocep.service.producer.EnderecoProducerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscaEnderecoRestStrategyTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private EnderecoProducerService producer;

    @Test
    void reportsUnavailableExternalApiInsteadOfReturningNotFound() {
        var strategy = strategy();
        when(restTemplate.exchange(
                any(java.net.URI.class),
                eq(HttpMethod.GET),
                isNull(),
                eq(EnderecoInput.class)))
                .thenThrow(new ResourceAccessException("timeout"));

        assertThatThrownBy(() -> strategy.buscaPorCEP("01001000"))
                .isInstanceOf(DependencyUnavailableException.class)
                .satisfies(exception -> {
                    var unavailable = (DependencyUnavailableException) exception;
                    org.assertj.core.api.Assertions.assertThat(unavailable.getStatus())
                            .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                });
    }

    @Test
    void encodesSearchParametersAsUriSegments() {
        var strategy = strategy();
        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<List<EnderecoInput>>>any()))
                .thenThrow(new ResourceAccessException("timeout"));

        assertThatThrownBy(() -> strategy.buscaPorLogradouro("SP", "São Paulo", "Rua A & B"))
                .isInstanceOf(DependencyUnavailableException.class);

        var uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).exchange(
                uriCaptor.capture(),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<List<EnderecoInput>>>any());
        assertThat(uriCaptor.getValue().getRawPath())
                .contains("S%C3%A3o%20Paulo")
                .contains("Rua%20A%20%26%20B");
    }

    @Test
    void mapsAndPublishesAddressReturnedByExternalApi() {
        var strategy = strategy();
        var input = new EnderecoInput(
                "01001000", "Praça da Sé", null, null, null,
                "São Paulo", "SP", null, null, "11");
        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                isNull(),
                eq(EnderecoInput.class)))
                .thenReturn(ResponseEntity.ok(input));

        var result = strategy.buscaPorCEP("01001000");

        assertThat(result).isPresent().hasValueSatisfying(address -> {
            assertThat(address.cep()).isEqualTo("01001000");
            assertThat(address.localidade()).isEqualTo("São Paulo");
        });
        verify(producer).postarEndereco(input);
    }

    @Test
    void returnsEmptyWhenExternalCepApiHasNoAddress() {
        var strategy = strategy();
        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                isNull(),
                eq(EnderecoInput.class)))
                .thenReturn(ResponseEntity.ok(null));

        assertThat(strategy.buscaPorCEP("01001000")).isEmpty();
    }

    @Test
    void mapsAndPublishesStreetSearchResults() {
        var strategy = strategy();
        var inputs = List.of(new EnderecoInput(
                "01001000", "Praça da Sé", null, null, null,
                "São Paulo", "SP", null, null, "11"));
        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<List<EnderecoInput>>>any()))
                .thenReturn(ResponseEntity.ok(inputs));

        var result = strategy.buscaPorLogradouro("SP", "São Paulo", "Praça da Sé");

        assertThat(result).isPresent().hasValueSatisfying(addresses ->
                assertThat(addresses).hasSize(1).first()
                        .satisfies(address -> assertThat(address.cep()).isEqualTo("01001000")));
        verify(producer).postarEnderecos(inputs);
    }

    @Test
    void returnsEmptyWhenExternalStreetSearchHasNoResults() {
        var strategy = strategy();
        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<List<EnderecoInput>>>any()))
                .thenReturn(ResponseEntity.ok(List.of()));

        assertThat(strategy.buscaPorLogradouro("SP", "São Paulo", "Praça da Sé")).isEmpty();
    }

    @Test
    void mapsHttpFailureToBadGateway() {
        var strategy = strategy();
        var failure = new RestClientResponseException(
                "upstream failure",
                HttpStatus.BAD_GATEWAY,
                "Bad Gateway",
                HttpHeaders.EMPTY,
                new byte[0],
                StandardCharsets.UTF_8);
        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                isNull(),
                eq(EnderecoInput.class)))
                .thenThrow(failure);

        assertThatThrownBy(() -> strategy.buscaPorCEP("01001000"))
                .isInstanceOf(DependencyUnavailableException.class)
                .satisfies(exception -> assertThat(
                        ((DependencyUnavailableException) exception).getStatus())
                        .isEqualTo(HttpStatus.BAD_GATEWAY));
    }

    @Test
    void mapsOtherRestClientFailuresToBadGateway() {
        var strategy = strategy();
        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                isNull(),
                eq(EnderecoInput.class)))
                .thenThrow(new RestClientException("invalid response"));

        assertThatThrownBy(() -> strategy.buscaPorCEP("01001000"))
                .isInstanceOf(DependencyUnavailableException.class)
                .satisfies(exception -> assertThat(
                        ((DependencyUnavailableException) exception).getStatus())
                        .isEqualTo(HttpStatus.BAD_GATEWAY));
    }

    private BuscaEnderecoRestStrategy strategy() {
        var strategy = new BuscaEnderecoRestStrategy(
                restTemplate,
                producer,
                Mappers.getMapper(EnderecoMapper.class));
        ReflectionTestUtils.setField(strategy, "urlBuscaCEP", "https://viacep.example/ws/{CEP}/json");
        ReflectionTestUtils.setField(
                strategy,
                "urlBuscaLogradouro",
                "https://viacep.example/ws/{UF}/{CIDADE}/{LOGRADOURO}/json");
        return strategy;
    }
}
