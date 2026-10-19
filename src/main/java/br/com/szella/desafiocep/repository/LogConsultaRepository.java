package br.com.szella.desafiocep.repository;

import br.com.szella.desafiocep.domain.entity.LogConsultaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface LogConsultaRepository extends JpaRepository<LogConsultaEntity, UUID> {

    @Modifying
    @Transactional
    @Query("DELETE FROM LogConsultaEntity log WHERE log.dataCriacao < :cutoff")
    long deleteExpiredBefore(@Param("cutoff") LocalDateTime cutoff);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO logs_consultas (id, caminho, entrada, saida, data_criacao)
            VALUES (:id, :caminho, :entrada, :saida, CURRENT_TIMESTAMP)
            ON CONFLICT (id) DO NOTHING
            """, nativeQuery = true)
    void insertIfAbsent(
            @Param("id") UUID id,
            @Param("caminho") String caminho,
            @Param("entrada") String entrada,
            @Param("saida") String saida);
}
