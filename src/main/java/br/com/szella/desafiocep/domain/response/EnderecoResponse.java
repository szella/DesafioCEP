package br.com.szella.desafiocep.domain.response;

public record EnderecoResponse(
        String cep,
        String logradouro,
        String complemento,
        String unidade,
        String bairro,
        String localidade,
        String uf,
        String estado,
        String regiao,
        String ddd
) {
}
