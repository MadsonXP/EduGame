package com.edugame.web.controladores;

import com.edugame.persistencia.modelo.Materia;
import com.edugame.persistencia.modelo.SessaoEstudo;
import com.edugame.persistencia.modelo.Usuario;
import com.edugame.persistencia.repositorio.MateriaRepo;
import com.edugame.persistencia.repositorio.SessaoEstudoRepo;
import com.edugame.persistencia.repositorio.UsuarioRepo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/materias")
public class MateriaController {

    private final MateriaRepo materiaRepo;
    private final UsuarioRepo usuarioRepo;
    private final SessaoEstudoRepo sessaoRepo;

    public MateriaController(MateriaRepo materiaRepo, UsuarioRepo usuarioRepo, SessaoEstudoRepo sessaoRepo) {
        this.materiaRepo = materiaRepo;
        this.usuarioRepo = usuarioRepo;
        this.sessaoRepo = sessaoRepo;
    }

    @GetMapping
    public String exibirLaboratorio(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername()).orElse(null);
        List<Materia> materias = materiaRepo.findByUsuarioId(usuario.getId());
        
        model.addAttribute("materias", materias);
        model.addAttribute("novaMateria", new Materia());
        model.addAttribute("novaSessao", new SessaoEstudo());
        
        return "materias/lista"; 
    }

    @GetMapping("/historico")
    public String exibirHistorico(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername()).orElse(null);
        List<SessaoEstudo> sessoes = sessaoRepo.findByUsuarioId(usuario.getId());
        
        model.addAttribute("sessoes", sessoes);
        return "materias/historico"; 
    }

    @PostMapping("/cadastrar")
    public String cadastrarMateria(@ModelAttribute("novaMateria") Materia materia, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername()).orElse(null);
        materia.setUsuario(usuario);
        materia.setTotalQuestoesRespondidas(0);
        materia.setTotalAcertos(0);
        materiaRepo.save(materia);
        return "redirect:/materias";
    }

    @PostMapping("/registrar-sessao")
    public String registrarSessao(@ModelAttribute("novaSessao") SessaoEstudo sessao, 
                                @RequestParam Long materiaId, 
                                @RequestParam String dataEstudo,
                                @RequestParam String horaInicio,
                                @RequestParam String horaFim,
                                @AuthenticationPrincipal UserDetails userDetails) {
        
        Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername()).orElse(null);
        Materia materia = materiaRepo.findById(materiaId).orElse(null);

        sessao.setUsuario(usuario);
        sessao.setMateria(materia);

        // ==========================================
        // 🕒 SISTEMA DE TEMPO (INÍCIO, FIM E DATA)
        // ==========================================
        LocalDate data = LocalDate.parse(dataEstudo);
        LocalTime inicio = LocalTime.parse(horaInicio);
        LocalTime fim = LocalTime.parse(horaFim);

        LocalDateTime dataSessaoFinal = LocalDateTime.of(data, fim);

        // Bloqueia tentativas de registar no futuro
        if (dataSessaoFinal.isAfter(LocalDateTime.now())) {
            dataSessaoFinal = LocalDateTime.now();
        }
        sessao.setDataSessao(dataSessaoFinal);

        // Cálculo de duração em minutos
        long minutos = Duration.between(inicio, fim).toMinutes();
        if (minutos <= 0) minutos = 1; 
        sessao.setTempoMinutos((int) minutos);

        if (Boolean.FALSE.equals(sessao.getTeveRedacao())) {
            sessao.setNotaRedacao(null);
        }

        int tempo = sessao.getTempoMinutos() != null ? sessao.getTempoMinutos() : 0;
        int qFeitas = sessao.getQuestoesFeitas() != null ? sessao.getQuestoesFeitas() : 0;
        int qAcertos = sessao.getQuestoesAcertos() != null ? sessao.getQuestoesAcertos() : 0;

        // ==========================================
        // 🛡️ SISTEMA ANTI-CHEAT 
        // ==========================================
        if (qAcertos > qFeitas) {
            qAcertos = qFeitas;
            sessao.setQuestoesAcertos(qAcertos);
        }

        // ==========================================
        // 🐉 SISTEMA DE BOSS: CONVERSÃO DE REDAÇÃO
        // ==========================================
        if (Boolean.TRUE.equals(sessao.getTeveRedacao()) && sessao.getNotaRedacao() != null) {
            int pesoRedacao = 10; // O "peso" em volume de 1 redação
            
            // CORREÇÃO APLICADA AQUI COM O (int)
            int acertosRedacao = (int) (sessao.getNotaRedacao() / 100); 
            
            // Soma o esforço da redação nas questões da sessão antes de guardar na matéria
            qFeitas += pesoRedacao;
            qAcertos += acertosRedacao;
        }

        if (materia != null) {
            int qAnteriores = materia.getTotalQuestoesRespondidas() != null ? materia.getTotalQuestoesRespondidas() : 0;
            int aAnteriores = materia.getTotalAcertos() != null ? materia.getTotalAcertos() : 0;
            
            materia.setTotalQuestoesRespondidas(qAnteriores + qFeitas);
            materia.setTotalAcertos(aAnteriores + qAcertos);
            materiaRepo.save(materia);
        }
        
        sessaoRepo.save(sessao);

        // ==========================================
        // ⚔️ SISTEMA DE RPG: DISTRIBUIÇÃO DE EXP E RANK GLOBAL
        // ==========================================
        if (usuario != null) {
            int expGanha = 0;
            expGanha += tempo * 2;    
            expGanha += qFeitas * 5;  
            expGanha += qAcertos * 10; 

            if (Boolean.TRUE.equals(sessao.getTeveRedacao()) && sessao.getNotaRedacao() != null) {
                expGanha += (int) (sessao.getNotaRedacao() / 2); 
            }

            usuario.ganharExp(expGanha);
            
            // --- NOVO SISTEMA DE RANK GERAL (MÉDIA DAS MATÉRIAS COM TRAVA DE VOLUME) ---
            List<Materia> todasMaterias = materiaRepo.findByUsuarioId(usuario.getId());
            
            double somaAproveitamentos = 0;
            int materiasComBatalha = 0;
            int totalQuestoesGlobal = 0;

            for (Materia m : todasMaterias) {
                int q = m.getTotalQuestoesRespondidas() != null ? m.getTotalQuestoesRespondidas() : 0;
                int a = m.getTotalAcertos() != null ? m.getTotalAcertos() : 0;
                
                totalQuestoesGlobal += q;

                // Apenas as disciplinas em que já batalhaste entram para a média
                if (q > 0) {
                    somaAproveitamentos += ((double) a / q) * 100;
                    materiasComBatalha++;
                }
            }

            if (materiasComBatalha > 0) {
                double mediaGeral = somaAproveitamentos / materiasComBatalha;
                
                if (mediaGeral >= 90 && totalQuestoesGlobal >= 500) usuario.setRankGeral("SS");
                else if (mediaGeral >= 80 && totalQuestoesGlobal >= 250) usuario.setRankGeral("S");
                else if (mediaGeral >= 75 && totalQuestoesGlobal >= 100) usuario.setRankGeral("A");
                else if (mediaGeral >= 65 && totalQuestoesGlobal >= 50) usuario.setRankGeral("B");
                else if (mediaGeral >= 50 && totalQuestoesGlobal >= 20) usuario.setRankGeral("C");
                else if (mediaGeral >= 40) usuario.setRankGeral("D");
                else if (mediaGeral >= 25) usuario.setRankGeral("E");
                else usuario.setRankGeral("F");
            }
            usuarioRepo.save(usuario);
        }

        return "redirect:/dashboard";
    }
}