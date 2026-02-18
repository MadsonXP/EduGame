package com.edugame.persistencia.repositorio;

import com.edugame.persistencia.modelo.Materia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MateriaRepo extends JpaRepository<Materia, Long> {
    // Busca apenas as matérias que pertencem ao jogador logado
    List<Materia> findByUsuarioId(Long usuarioId);
}