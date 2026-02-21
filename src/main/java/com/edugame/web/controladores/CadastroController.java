package com.edugame.web.controladores;

import com.edugame.persistencia.modelo.Usuario;
import com.edugame.persistencia.repositorio.UsuarioRepo;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.UUID;

@Controller
public class CadastroController {

    private final UsuarioRepo usuarioRepo;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender; 

    public CadastroController(UsuarioRepo usuarioRepo, PasswordEncoder passwordEncoder, JavaMailSender mailSender) {
        this.usuarioRepo = usuarioRepo;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @GetMapping("/cadastro")
    public String telaCadastro(Usuario usuario) {
        return "login/cadastro";
    }

    @PostMapping("/cadastro")
    public String registrarUsuario(Usuario usuario, Model model) {
        
        if (usuarioRepo.findByEmail(usuario.getEmail()).isPresent()) {
            model.addAttribute("erro", "Um caçador já registou este e-mail.");
            return "login/cadastro";
        }

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuario.setDataCadastro(LocalDate.now()); 
        
        // 1. Gera código e bloqueia a conta
        String codigoSorteado = UUID.randomUUID().toString();
        usuario.setCodigoVerificacao(codigoSorteado);
        usuario.setContaAtiva(false);

        usuarioRepo.save(usuario);

        // 2. Envia a carta coruja (E-mail)
        enviarEmailDeVerificacao(usuario, codigoSorteado);

        return "redirect:/login?registrado=true";
    }

    private void enviarEmailDeVerificacao(Usuario usuario, String codigo) {
        String urlVerificacao = "http://localhost:8080/verificar?codigo=" + codigo;

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setTo(usuario.getEmail());
        mensagem.setSubject("⚔️ EduGame - Verifica a tua conta de Caçador!");
        mensagem.setText("Saudações, Caçador " + usuario.getNome() + "!\n\n"
                + "Clica no link abaixo para ativar a tua conta:\n"
                + urlVerificacao + "\n\n"
                + "A Guilda espera por ti!");

        // --- DETETOR DE MENTIRAS DO JAVA (RASTREADOR) ---
        try {
            System.out.println("=====================================================");
            System.out.println("⏳ A TENTAR ENVIAR E-MAIL PARA: " + usuario.getEmail());
            mailSender.send(mensagem);
            System.out.println("✅ SUCESSO! O GOOGLE ACEITOU O E-MAIL.");
            System.out.println("=====================================================");
        } catch (Exception e) {
            System.out.println("=====================================================");
            System.out.println("❌ FALHA CRÍTICA AO ENVIAR O E-MAIL!");
            System.out.println("MOTIVO EXATO DO ERRO: " + e.getMessage());
            System.out.println("=====================================================");
        }
    }

    // A Rota mágica em que o utilizador clica no e-mail
    @GetMapping("/verificar")
    public String verificarConta(@RequestParam("codigo") String codigo, Model model) {
        Usuario usuario = usuarioRepo.findAll().stream()
                .filter(u -> codigo.equals(u.getCodigoVerificacao()))
                .findFirst()
                .orElse(null);

        if (usuario == null) {
            return "redirect:/login?erroAtivacao=true"; 
        }

        // Ativa o caçador e destrói o código para não ser usado de novo
        usuario.setContaAtiva(true);
        usuario.setCodigoVerificacao(null);
        usuarioRepo.save(usuario);

        return "redirect:/login?ativado=true";
    }
}