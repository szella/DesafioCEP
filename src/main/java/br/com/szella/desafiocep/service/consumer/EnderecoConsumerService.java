package br.com.szella.desafiocep.service.consumer;

import br.com.szella.desafiocep.domain.input.EnderecoInput;
import br.com.szella.desafiocep.domain.mapper.EnderecoMapper;
import br.com.szella.desafiocep.filter.CorrelationFilter;
import br.com.szella.desafiocep.repository.EnderecoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnderecoConsumerService {

    private final EnderecoRepository repository;
    private final CorrelationFilter correlationFilter;
    private final EnderecoMapper mapper;

    @KafkaListener(topics = "${kafka.topic.endereco}", groupId = "${spring.kafka.consumer.group-id}")
    public void processarEndereco(
            @Payload EnderecoInput enderecoInput,
            @Header(name = CorrelationFilter.KAFKA_HEADER, required = false) String correlationId) {
        try {
            correlationFilter.adicionar(correlationId);

            log.info("Consumindo mensagem de endereco.");

            var normalizedCep = EnderecoInput.padronizarCep(enderecoInput.cep());
            if (!StringUtils.hasText(normalizedCep) || !normalizedCep.matches("\\d{8}")) {
                throw new IllegalArgumentException("Mensagem contém CEP inválido");
            }

            var entity = mapper.enderecoInputParaEntity(enderecoInput);
            repository.upsertByCep(
                    UUID.randomUUID(),
                    normalizedCep,
                    entity.getLogradouro(),
                    entity.getComplemento(),
                    entity.getUnidade(),
                    entity.getBairro(),
                    entity.getLocalidade(),
                    entity.getUf(),
                    entity.getEstado(),
                    entity.getRegiao(),
                    entity.getDdd());

            log.info("Mensagem de endereco processada.");
        } finally {
            correlationFilter.remover();
        }
    }
}
