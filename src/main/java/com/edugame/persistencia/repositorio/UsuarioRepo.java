package com.edugame.persistencia.repositorio;

import com.edugame.persistencia.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepo extends JpaRepository<Usuario, Long> {
    // Busca automática por e-mail para o sistema de segurança
    Optional<Usuario> findByEmail(String email);
}