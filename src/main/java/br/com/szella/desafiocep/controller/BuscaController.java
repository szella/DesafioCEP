package br.com.szella.desafiocep.controller;

import br.com.szella.desafiocep.domain.mapper.EnderecoMapper;
import br.com.szella.desafiocep.domain.response.EnderecoResponse;
import br.com.szella.desafiocep.exception.ErrorResponse;
import br.com.szella.desafiocep.service.BuscaEnderecoService;
import br.com.szella.desafiocep.service.producer.LogConsultaProducerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/buscar")
@Tag(name = "Buscar Endereço",
        description = "Endpoints para Buscar Endereço")
public class BuscaController {

    private final BuscaEnderecoService buscaEnderecoService;
    private final LogConsultaProducerService logConsultaProducerService;
    private final EnderecoMapper enderecoMapper;

    @GetMapping("/por-cep/{cep}")
    @Operation(summary = "Buscar endereço",
            description = "Buscar endereço por CEP.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca de endereço com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EnderecoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Endereço não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Falha na API externa",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Dependência indisponível",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EnderecoResponse> buscaPorCep(
            @PathVariable("cep") @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP deve conter 8 dígitos") String cep,
            HttpServletRequest request) {
        log.info("Iniciando busca de endereço por CEP.");

        var enderecoDto = buscaEnderecoService.buscaPorCEP(cep);
        var enderecoResponse = enderecoMapper.enderecoDtoParaResponse(enderecoDto);

        logConsultaProducerService.gravarLogConsulta(
                request.getRequestURI(),
                Map.of("cep", cep),
                enderecoResponse);

        log.info("Busca de endereço por CEP concluída.");
        return ResponseEntity.ok(enderecoResponse);
    }

    @GetMapping("/por-logradouro")
    @Operation(summary = "Buscar endereço",
            description = "Buscar endereço por logradouro.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca de endereço com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EnderecoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Endereço não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Falha na API externa",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Dependência indisponível",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<EnderecoResponse>> buscaPorLogradouro(
            @RequestParam @Pattern(regexp = "[A-Za-z]{2}", message = "UF deve conter duas letras") String uf,
            @RequestParam @NotBlank @Size(max = 100) String cidade,
            @RequestParam @NotBlank @Size(max = 150) String logradouro,
            HttpServletRequest request
    ) {
        log.info("Iniciando busca de endereço por logradouro.");

        var enderecosDto = buscaEnderecoService.buscaPorLogradouro(uf, cidade, logradouro);
        var enderecosResponse = enderecoMapper.enderecosListDtoParaListResponse(enderecosDto);

        logConsultaProducerService.gravarLogConsulta(
                request.getRequestURI(),
                Map.of("uf", uf, "cidade", cidade, "logradouro", logradouro),
                enderecosResponse);


        log.info("Busca de endereço por logradouro concluída.");
        return ResponseEntity.ok(enderecosResponse);
    }
}
