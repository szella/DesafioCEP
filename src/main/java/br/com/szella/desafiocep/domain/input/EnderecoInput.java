package br.com.szella.desafiocep.domain.input;

public record EnderecoInput(
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
    public EnderecoInput {
        cep = padronizarCep(cep);
    }

    public static String padronizarCep(String cep) {
        if (cep == null || cep.isBlank()) {
            return null;
        }
        String apenasNumeros = cep.replaceAll("\\D", "");

        return apenasNumeros;
    }
}
