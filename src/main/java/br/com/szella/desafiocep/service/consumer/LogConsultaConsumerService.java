package br.com.szella.desafiocep.service.consumer;

import br.com.szella.desafiocep.domain.dto.LogConsultaDto;
import br.com.szella.desafiocep.domain.mapper.LogConsultaMapper;
import br.com.szella.desafiocep.filter.CorrelationFilter;
import br.com.szella.desafiocep.repository.LogConsultaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogConsultaConsumerService {

    private final LogConsultaRepository repository;
    private final CorrelationFilter correlationFilter;
    private final LogConsultaMapper mapper;

    @KafkaListener(topics = "${kafka.topic.log-consulta}", groupId = "${spring.kafka.consumer.group-id}")
    public void processarLogConsulta(
            @Payload LogConsultaDto dto,
            @Header(name = CorrelationFilter.KAFKA_HEADER, required = false) String correlationId) {
        try {
            correlationFilter.adicionar(correlationId);

            log.info("Consumindo mensagem de auditoria.");

            var entity = mapper.logConsultaDtoParaEntity(dto);

            repository.insertIfAbsent(
                    entity.getId(),
                    entity.getCaminho(),
                    entity.getEntrada(),
                    entity.getSaida());

            log.info("Mensagem de auditoria processada.");
        } finally {
            correlationFilter.remover();
        }
    }
}
