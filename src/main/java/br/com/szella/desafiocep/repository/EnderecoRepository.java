package br.com.szella.desafiocep.repository;

import br.com.szella.desafiocep.domain.entity.EnderecoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnderecoRepository extends JpaRepository<EnderecoEntity, UUID> {

    Optional<EnderecoEntity> findByCep(String cep);

    @Query("""
            SELECT e FROM EnderecoEntity e
            WHERE LOWER(e.uf) = LOWER(:uf)
              AND LOWER(e.localidade) = LOWER(:localidade)
              AND LOWER(e.logradouro) = LOWER(:logradouro)
            """)
    List<EnderecoEntity> findByUfAndLocalidadeAndLogradouro(
            @Param("uf") String uf,
            @Param("localidade") String localidade,
            @Param("logradouro") String logradouro);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO enderecos
                (id, cep, logradouro, complemento, unidade, bairro, localidade, uf, estado, regiao, ddd)
            VALUES
                (:id, :cep, :logradouro, :complemento, :unidade, :bairro, :localidade, :uf, :estado, :regiao, :ddd)
            ON CONFLICT (cep) DO UPDATE SET
                logradouro = EXCLUDED.logradouro,
                complemento = EXCLUDED.complemento,
                unidade = EXCLUDED.unidade,
                bairro = EXCLUDED.bairro,
                localidade = EXCLUDED.localidade,
                uf = EXCLUDED.uf,
                estado = EXCLUDED.estado,
                regiao = EXCLUDED.regiao,
                ddd = EXCLUDED.ddd
            """, nativeQuery = true)
    void upsertByCep(
            @Param("id") UUID id,
            @Param("cep") String cep,
            @Param("logradouro") String logradouro,
            @Param("complemento") String complemento,
            @Param("unidade") String unidade,
            @Param("bairro") String bairro,
            @Param("localidade") String localidade,
            @Param("uf") String uf,
            @Param("estado") String estado,
            @Param("regiao") String regiao,
            @Param("ddd") String ddd);
}
