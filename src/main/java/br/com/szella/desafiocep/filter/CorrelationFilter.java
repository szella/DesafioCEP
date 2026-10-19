package br.com.szella.desafiocep.filter;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CorrelationFilter {

    public static final String MDC_KEY = "correlationId";
    public static final String HTTP_HEADER = "x-correlation-id";
    public static final String KAFKA_HEADER = "x-correlation-id";

    public void adicionar(String correlationId) {
        if (correlationId == null || !correlationId.matches("[A-Za-z0-9._-]{1,128}")) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_KEY, correlationId);
    }

    public void remover() {
        MDC.remove(MDC_KEY);
    }

    public String getCorrelationId() {
        return MDC.get(MDC_KEY);
    }
}
