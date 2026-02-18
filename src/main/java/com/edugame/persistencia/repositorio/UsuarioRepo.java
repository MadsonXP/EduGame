package com.edugame.persistencia.repositorio;

import com.edugame.persistencia.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepo extends JpaRepository<Usuario, Long> {
    // Busca o caçador pelo e-mail na hora do Login
    Optional<Usuario> findByEmail(String email);
}