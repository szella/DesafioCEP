package br.com.szella.desafiocep.controller;

import br.com.szella.desafiocep.domain.dto.EnderecoDto;
import br.com.szella.desafiocep.domain.mapper.EnderecoMapper;
import br.com.szella.desafiocep.domain.response.EnderecoResponse;
import br.com.szella.desafiocep.exception.DependencyUnavailableException;
import br.com.szella.desafiocep.filter.CorrelationFilter;
import br.com.szella.desafiocep.service.BuscaEnderecoService;
import br.com.szella.desafiocep.service.producer.LogConsultaProducerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BuscaController.class)
@Import(CorrelationFilter.class)
class BuscaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BuscaEnderecoService buscaEnderecoService;

    @MockitoBean
    private LogConsultaProducerService logConsultaProducerService;

    @MockitoBean
    private EnderecoMapper enderecoMapper;

    @Test
    void keepsLegacyCepRouteAvailable() throws Exception {
        var dto = new EnderecoDto(
                "01001000", "Praca da Se", null, null, null,
                "Sao Paulo", "SP", null, null, "11");
        var response = new EnderecoResponse(
                "01001000", "Praca da Se", null, null, null,
                "Sao Paulo", "SP", null, null, "11");
        when(buscaEnderecoService.buscaPorCEP("01001-000")).thenReturn(dto);
        when(enderecoMapper.enderecoDtoParaResponse(dto)).thenReturn(response);

        mockMvc.perform(get("/api/buscar/por-cep/01001-000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("01001000"));
    }

    @Test
    void rejectsMalformedCepWithStandardErrorResponse() throws Exception {
        mockMvc.perform(get("/api/buscar/por-cep/123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/buscar/por-cep/123"));

        verifyNoInteractions(buscaEnderecoService, enderecoMapper, logConsultaProducerService);
    }

    @Test
    void reportsUnavailableDependencyWith503AndRequestPath() throws Exception {
        when(buscaEnderecoService.buscaPorCEP("01001000"))
                .thenThrow(new DependencyUnavailableException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "A API de CEP está temporariamente indisponível.",
                        new IllegalStateException("timeout")));

        mockMvc.perform(get("/api/buscar/por-cep/01001000"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.path").value("/api/buscar/por-cep/01001000"));
    }

    @Test
    void searchesStreetAndReturnsMappedAddresses() throws Exception {
        var address = new EnderecoDto(
                "01001000", "Rua A", null, null, null,
                "Sao Paulo", "SP", null, null, "11");
        var response = new EnderecoResponse(
                "01001000", "Rua A", null, null, null,
                "Sao Paulo", "SP", null, null, "11");
        var responses = List.of(response);
        when(buscaEnderecoService.buscaPorLogradouro("SP", "Sao Paulo", "Rua A"))
                .thenReturn(List.of(address));
        when(enderecoMapper.enderecosListDtoParaListResponse(List.of(address)))
                .thenReturn(responses);

        mockMvc.perform(get("/api/buscar/por-logradouro")
                        .param("uf", "SP")
                        .param("cidade", "Sao Paulo")
                        .param("logradouro", "Rua A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cep").value("01001000"))
                .andExpect(jsonPath("$[0].logradouro").value("Rua A"));

        verify(logConsultaProducerService).gravarLogConsulta(
                "/api/buscar/por-logradouro",
                Map.of("uf", "SP", "cidade", "Sao Paulo", "logradouro", "Rua A"),
                responses);
    }

    @Test
    void rejectsInvalidStreetSearchParameters() throws Exception {
        mockMvc.perform(get("/api/buscar/por-logradouro")
                        .param("uf", "Sao Paulo")
                        .param("cidade", "")
                        .param("logradouro", "Rua A"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/buscar/por-logradouro"));

        verifyNoInteractions(buscaEnderecoService, enderecoMapper, logConsultaProducerService);
    }
}
