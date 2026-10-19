package br.com.szella.desafiocep.service.producer;

import br.com.szella.desafiocep.domain.dto.LogConsultaDto;
import br.com.szella.desafiocep.filter.CorrelationFilter;
import br.com.szella.desafiocep.utils.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogConsultaProducerService {

    private final KafkaTemplate<String, LogConsultaDto> kafkaTemplate;
    private final ObjectMapperUtil objectMapper;
    private final CorrelationFilter correlationFilter;

    @Value("${kafka.topic.log-consulta}")
    private String topic;

    public void gravarLogConsulta(
            String caminho,
            Map<String, String> entrada,
            Object response
    ) {
        var logConsultaDto = new LogConsultaDto(
                UUID.randomUUID(),
                caminho,
                objectMapper.converterParaJson(entrada),
                objectMapper.converterParaJson(response)
        );

        var record = new ProducerRecord<String, LogConsultaDto>(topic, logConsultaDto);
        var correlationId = correlationFilter.getCorrelationId();
        if (correlationId != null) {
            record.headers().add(new RecordHeader(
                    CorrelationFilter.KAFKA_HEADER, correlationId.getBytes(StandardCharsets.UTF_8)));
        }

        try {
            kafkaTemplate.send(record).whenComplete((result, error) -> {
                if (error != null) {
                    log.error("Falha ao publicar evento de auditoria no Kafka.", error);
                } else {
                    log.debug("Evento de auditoria confirmado pelo Kafka.");
                }
            });
        } catch (RuntimeException e) {
            log.error("Falha ao iniciar publicação do evento de auditoria no Kafka.", e);
        }
    }
}
