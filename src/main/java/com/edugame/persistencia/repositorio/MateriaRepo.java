package com.edugame.persistencia.repositorio;

import com.edugame.persistencia.modelo.Materia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MateriaRepo extends JpaRepository<Materia, Long> {
    List<Materia> findByUsuarioId(Long usuarioId);
}