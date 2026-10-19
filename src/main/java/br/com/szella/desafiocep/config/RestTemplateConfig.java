package br.com.szella.desafiocep.config;

import br.com.szella.desafiocep.filter.CorrelationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(
            RestTemplateBuilder builder,
            CorrelationFilter correlationFilter,
            @Value("${api.http.connect-timeout}") Duration connectTimeout,
            @Value("${api.http.read-timeout}") Duration readTimeout) {
        return builder
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .additionalInterceptors((request, body, execution) -> {
                    var correlationId = correlationFilter.getCorrelationId();
                    if (correlationId != null) {
                        request.getHeaders().set(CorrelationFilter.HTTP_HEADER, correlationId);
                    }
                    return execution.execute(request, body);
                })
                .build();
    }
}
