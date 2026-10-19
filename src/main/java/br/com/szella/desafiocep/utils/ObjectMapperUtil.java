package br.com.szella.desafiocep.utils;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ObjectMapperUtil {

    private final ObjectMapper objectMapper;

    public ObjectMapperUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String converterParaJson(Object objeto) {
        try {
            return objectMapper.writeValueAsString(objeto);
        } catch (JacksonException e) {
            log.error("Falha ao serializar evento de auditoria.", e);
            throw new IllegalStateException("Falha ao serializar evento de auditoria.", e);
        }
    }
}
