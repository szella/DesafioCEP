package br.com.szella.desafiocep.service.consumer;

import br.com.szella.desafiocep.domain.input.EnderecoInput;
import br.com.szella.desafiocep.domain.mapper.EnderecoMapper;
import br.com.szella.desafiocep.filter.CorrelationFilter;
import br.com.szella.desafiocep.repository.EnderecoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class EnderecoConsumerServiceTest {

    @Mock
    private EnderecoRepository repository;

    @Test
    void upsertsNormalizedCepForIdempotentConsumption() {
        var correlationFilter = new CorrelationFilter();
        var mapper = Mappers.getMapper(EnderecoMapper.class);
        var consumer = new EnderecoConsumerService(repository, correlationFilter, mapper);
        var input = new EnderecoInput(
                "01001-000",
                "Praca da Se",
                null,
                null,
                null,
                "Sao Paulo",
                "SP",
                null,
                null,
                "11");

        consumer.processarEndereco(input, "request-123");

        verify(repository).upsertByCep(
                any(),
                eq("01001000"),
                eq("Praca da Se"),
                isNull(),
                isNull(),
                isNull(),
                eq("Sao Paulo"),
                eq("SP"),
                isNull(),
                isNull(),
                eq("11"));
    }

    @Test
    void rejectsInvalidCepInsteadOfPersistingBadMessage() {
        var correlationFilter = new CorrelationFilter();
        var consumer = new EnderecoConsumerService(
                repository,
                correlationFilter,
                Mappers.getMapper(EnderecoMapper.class));
        var input = new EnderecoInput("123", null, null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> consumer.processarEndereco(input, null))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repository);
    }
}
