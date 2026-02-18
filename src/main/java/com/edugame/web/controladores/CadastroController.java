package com.edugame.web.controladores;

import com.edugame.persistencia.modelo.Usuario;
import com.edugame.persistencia.repositorio.UsuarioRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import java.time.LocalDate;

@Controller
public class CadastroController {

    private final UsuarioRepo usuarioRepo;
    private final PasswordEncoder passwordEncoder;

    public CadastroController(UsuarioRepo usuarioRepo, PasswordEncoder passwordEncoder) {
        this.usuarioRepo = usuarioRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/cadastro")
    public String telaCadastro(Usuario usuario) {
        return "login/cadastro";
    }

    @PostMapping("/cadastro")
    public String registrarUsuario(Usuario usuario) {
        // Criptografa a senha para o guarda da muralha não ver o texto puro
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuario.setDataCadastro(LocalDate.now()); // Marca o dia da entrada na guilda
        
        usuarioRepo.save(usuario);
        // O segredo está aqui: envia o usuário de volta com um 'ticket' de sucesso
        return "redirect:/login?sucesso";
    }
}