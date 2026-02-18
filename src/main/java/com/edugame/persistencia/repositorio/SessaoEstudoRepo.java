package com.edugame.persistencia.repositorio;

import com.edugame.persistencia.modelo.SessaoEstudo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SessaoEstudoRepo extends JpaRepository<SessaoEstudo, Long> {
    // Busca o histórico de batalhas de um jogador específico
    List<SessaoEstudo> findByUsuarioId(Long usuarioId);
}