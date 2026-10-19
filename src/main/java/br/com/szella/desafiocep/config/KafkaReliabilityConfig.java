package br.com.szella.desafiocep.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.time.Duration;

@Configuration
public class KafkaReliabilityConfig {

    @Bean
    public CommonErrorHandler kafkaErrorHandler(
            KafkaTemplate<Object, Object> kafkaTemplate,
            @Value("${kafka.topic.endereco}") String enderecoTopic,
            @Value("${kafka.topic.endereco-dlt}") String enderecoDlt,
            @Value("${kafka.topic.log-consulta}") String logConsultaTopic,
            @Value("${kafka.topic.log-consulta-dlt}") String logConsultaDlt,
            @Value("${kafka.dlt.send-result-timeout}") Duration sendResultTimeout,
            @Value("${kafka.consumer.retry-interval}") Duration retryInterval,
            @Value("${kafka.consumer.retry-attempts}") long retryAttempts) {
        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate, (record, exception) -> {
            var topic = record.topic().equals(enderecoTopic)
                    ? enderecoDlt
                    : record.topic().equals(logConsultaTopic)
                    ? logConsultaDlt
                    : record.topic() + ".DLT";
            return new TopicPartition(topic, record.partition());
        });
        recoverer.setFailIfSendResultIsError(true);
        recoverer.setWaitForSendResultTimeout(sendResultTimeout);
        return new DefaultErrorHandler(recoverer, new FixedBackOff(retryInterval.toMillis(), retryAttempts));
    }

    @Bean
    public NewTopic enderecoTopic(
            @Value("${kafka.topic.endereco}") String topic,
            @Value("${kafka.topic.partitions}") int partitions,
            @Value("${kafka.topic.replication-factor:1}") short replicationFactor) {
        return TopicBuilder.name(topic).partitions(partitions).replicas(replicationFactor).build();
    }

    @Bean
    public NewTopic enderecoDeadLetterTopic(
            @Value("${kafka.topic.endereco-dlt}") String topic,
            @Value("${kafka.topic.partitions}") int partitions,
            @Value("${kafka.topic.replication-factor:1}") short replicationFactor) {
        return TopicBuilder.name(topic).partitions(partitions).replicas(replicationFactor).build();
    }

    @Bean
    public NewTopic logConsultaTopic(
            @Value("${kafka.topic.log-consulta}") String topic,
            @Value("${kafka.topic.partitions}") int partitions,
            @Value("${kafka.topic.replication-factor:1}") short replicationFactor) {
        return TopicBuilder.name(topic).partitions(partitions).replicas(replicationFactor).build();
    }

    @Bean
    public NewTopic logConsultaDeadLetterTopic(
            @Value("${kafka.topic.log-consulta-dlt}") String topic,
            @Value("${kafka.topic.partitions}") int partitions,
            @Value("${kafka.topic.replication-factor:1}") short replicationFactor) {
        return TopicBuilder.name(topic).partitions(partitions).replicas(replicationFactor).build();
    }
}
