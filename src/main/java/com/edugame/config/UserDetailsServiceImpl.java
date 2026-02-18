package com.edugame.config;

import com.edugame.persistencia.modelo.Usuario;
import com.edugame.persistencia.repositorio.UsuarioRepo;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepo usuarioRepo;

    public UserDetailsServiceImpl(UsuarioRepo usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Busca o usuário pelo e-mail
        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Caçador não encontrado com o e-mail: " + email));

        // Transforma o nosso Usuario no formato que o Spring Security entende
        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getSenha())
                .roles("JOGADOR")
                .build();
    }
}