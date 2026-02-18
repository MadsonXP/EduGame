package com.edugame.persistencia.repositorio;

import com.edugame.persistencia.modelo.SessaoEstudo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SessaoEstudoRepo extends JpaRepository<SessaoEstudo, Long> {
    List<SessaoEstudo> findByUsuarioId(Long usuarioId);
}