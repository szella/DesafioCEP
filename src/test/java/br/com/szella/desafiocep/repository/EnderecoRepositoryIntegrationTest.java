package br.com.szella.desafiocep.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EnderecoRepositoryIntegrationTest {

    @Autowired
    private EnderecoRepository repository;

    @Autowired
    private LogConsultaRepository logConsultaRepository;

    @Test
    void upsertByCepIsIdempotentAndUpdatesCachedAddress() {
        var cep = String.format("%08d", Math.floorMod(System.nanoTime(), 100_000_000));

        repository.upsertByCep(
                UUID.randomUUID(), cep, "Rua Antiga", null, null, null,
                "Sao Paulo", "SP", null, null, "11");
        repository.upsertByCep(
                UUID.randomUUID(), cep, "Rua Atualizada", null, null, null,
                "Sao Paulo", "SP", null, null, "11");

        var saved = repository.findByCep(cep).orElseThrow();
        assertThat(saved.getCep()).isEqualTo(cep);
        assertThat(saved.getLogradouro()).isEqualTo("Rua Atualizada");
    }

    @Test
    void savingAuditEventAgainWithSameIdDoesNotCreateDuplicate() {
        var auditCountBefore = logConsultaRepository.count();
        var eventId = UUID.randomUUID();
        logConsultaRepository.insertIfAbsent(
                eventId,
                "/api/buscar/por-cep/{cep}",
                "{\"cep\":\"01001000\"}",
                "{\"encontrado\":true}");
        logConsultaRepository.insertIfAbsent(
                eventId,
                "/api/buscar/por-cep/{cep}",
                "{\"cep\":\"01001000\"}",
                "{\"encontrado\":true}");

        assertThat(logConsultaRepository.findById(eventId)).isPresent();
        assertThat(logConsultaRepository.count()).isEqualTo(auditCountBefore + 1);
    }
}
