package br.com.szella.desafiocep.service;

import br.com.szella.desafiocep.repository.LogConsultaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogConsultaRetentionService {

    private final LogConsultaRepository repository;

    @Value("${log.consultas.retention-days}")
    private int retentionDays;

    @Scheduled(cron = "${log.consultas.retention-cron}")
    @Transactional
    public void removerLogsExpirados() {
        var cutoff = LocalDateTime.now().minusDays(retentionDays);
        var removidos = repository.deleteExpiredBefore(cutoff);
        log.info("Expurgo de logs de consulta concluído; registros removidos: {}", removidos);
    }
}
