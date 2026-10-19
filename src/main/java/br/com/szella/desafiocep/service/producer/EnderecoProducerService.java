package br.com.szella.desafiocep.service.producer;

import br.com.szella.desafiocep.domain.input.EnderecoInput;
import br.com.szella.desafiocep.filter.CorrelationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnderecoProducerService {

    private final KafkaTemplate<String, EnderecoInput> kafkaTemplate;
    private final CorrelationFilter correlationFilter;

    @Value("${kafka.topic.endereco}")
    private String topic;

    public void postarEndereco(EnderecoInput endereco) {
        var record = new ProducerRecord<String, EnderecoInput>(topic, endereco);
        var correlationId = correlationFilter.getCorrelationId();
        if (correlationId != null) {
            record.headers().add(new RecordHeader(
                    CorrelationFilter.KAFKA_HEADER, correlationId.getBytes(StandardCharsets.UTF_8)));
        }

        try {
            kafkaTemplate.send(record).whenComplete((result, error) -> {
                if (error != null) {
                    log.error("Falha ao publicar evento de endereço no Kafka.", error);
                } else {
                    log.debug("Evento de endereço confirmado pelo Kafka.");
                }
            });
        } catch (RuntimeException e) {
            log.error("Falha ao iniciar publicação de evento de endereço no Kafka.", e);
        }
    }

    public void postarEnderecos(List<EnderecoInput> enderecos) {
        enderecos.forEach(this::postarEndereco);
    }
}
