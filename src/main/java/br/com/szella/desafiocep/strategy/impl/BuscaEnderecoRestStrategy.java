package br.com.szella.desafiocep.strategy.impl;

import br.com.szella.desafiocep.domain.dto.EnderecoDto;
import br.com.szella.desafiocep.domain.input.EnderecoInput;
import br.com.szella.desafiocep.domain.mapper.EnderecoMapper;
import br.com.szella.desafiocep.exception.DependencyUnavailableException;
import br.com.szella.desafiocep.service.producer.EnderecoProducerService;
import br.com.szella.desafiocep.strategy.BuscaEnderecoStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Order(2)
@Component("BuscaEnderecoRest")
@RequiredArgsConstructor
public class BuscaEnderecoRestStrategy implements BuscaEnderecoStrategy {

    @Value("${api.url.busca.cep}")
    private String urlBuscaCEP;

    @Value("${api.url.busca.logradouro}")
    private String urlBuscaLogradouro;

    private final RestTemplate restTemplate;

    private final EnderecoProducerService producer;
    private final EnderecoMapper mapper;

    @Override
    public Optional<EnderecoDto> buscaPorCEP(String cep) {
        try {
            var url = UriComponentsBuilder.fromUriString(urlBuscaCEP)
                    .encode()
                    .buildAndExpand(Map.of("CEP", cep))
                    .toUri();
            var responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, EnderecoInput.class);
            var responseEntityBody = responseEntity.getBody();

            if (responseEntityBody == null || responseEntityBody.cep() == null) {
                return Optional.empty();
            }

            producer.postarEndereco(responseEntityBody);
            var enderecoDto = mapper.enderecoInputParaDto(responseEntityBody);

            log();
            return Optional.of(enderecoDto);
        } catch (ResourceAccessException e) {
            log.error("Timeout ou falha de conexão ao consultar a API de CEP.", e);
            throw new DependencyUnavailableException(
                    HttpStatus.SERVICE_UNAVAILABLE, "A API de CEP está temporariamente indisponível.", e);
        } catch (RestClientResponseException e) {
            log.error("A API de CEP retornou HTTP {}.", e.getStatusCode().value(), e);
            throw new DependencyUnavailableException(
                    HttpStatus.BAD_GATEWAY, "A API de CEP retornou uma resposta inválida.", e);
        } catch (RestClientException e) {
            log.error("Falha ao consultar a API de CEP.", e);
            throw new DependencyUnavailableException(
                    HttpStatus.BAD_GATEWAY, "Não foi possível consultar a API de CEP.", e);
        }
    }

    @Override
    public Optional<List<EnderecoDto>> buscaPorLogradouro(String estado, String cidade, String logradouro) {
        try {
            var url = UriComponentsBuilder.fromUriString(urlBuscaLogradouro)
                    .encode()
                    .buildAndExpand(Map.of(
                            "UF", estado,
                            "CIDADE", cidade,
                            "LOGRADOURO", logradouro))
                    .toUri();
            var responseEntity = restTemplate.exchange(url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<EnderecoInput>>() {
                    });
            var responseEntityBody = responseEntity.getBody();

            if (responseEntityBody == null || responseEntityBody.isEmpty()) {
                return Optional.empty();
            }

            producer.postarEnderecos(responseEntityBody);
            var enderecosDto = mapper.enderecosListInputParaListDto(responseEntityBody);

            log();
            return Optional.of(enderecosDto);
        } catch (ResourceAccessException e) {
            log.error("Timeout ou falha de conexão ao consultar a API de logradouro.", e);
            throw new DependencyUnavailableException(
                    HttpStatus.SERVICE_UNAVAILABLE, "A API de endereço está temporariamente indisponível.", e);
        } catch (RestClientResponseException e) {
            log.error("A API de logradouro retornou HTTP {}.", e.getStatusCode().value(), e);
            throw new DependencyUnavailableException(
                    HttpStatus.BAD_GATEWAY, "A API de endereço retornou uma resposta inválida.", e);
        } catch (RestClientException e) {
            log.error("Falha ao consultar a API de logradouro.", e);
            throw new DependencyUnavailableException(
                    HttpStatus.BAD_GATEWAY, "Não foi possível consultar a API de endereço.", e);
        }
    }

    private void log() {
        log.info("Busca de endereco realizada via API.");
    }
}
