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

import java.time.LocalDateTime;
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
                                @AuthenticationPrincipal UserDetails userDetails) {
        
        Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername()).orElse(null);
        Materia materia = materiaRepo.findById(materiaId).orElse(null);

        sessao.setUsuario(usuario);
        sessao.setMateria(materia);
        sessao.setDataSessao(LocalDateTime.now());

        if (Boolean.FALSE.equals(sessao.getTeveRedacao())) {
            sessao.setNotaRedacao(null);
        }

        // Prevenção de Null: Garante que os valores sejam no mínimo 0
        int tempo = sessao.getTempoMinutos() != null ? sessao.getTempoMinutos() : 0;
        int qFeitas = sessao.getQuestoesFeitas() != null ? sessao.getQuestoesFeitas() : 0;
        int qAcertos = sessao.getQuestoesAcertos() != null ? sessao.getQuestoesAcertos() : 0;

        // Atualiza status da matéria
        if (materia != null) {
            int qAnteriores = materia.getTotalQuestoesRespondidas() != null ? materia.getTotalQuestoesRespondidas() : 0;
            int aAnteriores = materia.getTotalAcertos() != null ? materia.getTotalAcertos() : 0;
            
            materia.setTotalQuestoesRespondidas(qAnteriores + qFeitas);
            materia.setTotalAcertos(aAnteriores + qAcertos);
            materiaRepo.save(materia);
        }
        
        sessaoRepo.save(sessao);

        // ==========================================
        // ⚔️ SISTEMA DE RPG: DISTRIBUIÇÃO DE EXP E RANK
        // ==========================================
        if (usuario != null) {
            int expGanha = 0;
            expGanha += tempo * 2;    // 2 EXP por minuto de foco
            expGanha += qFeitas * 5;  // 5 EXP por questão resolvida
            expGanha += qAcertos * 10; // 10 de Bônus por acerto

            if (Boolean.TRUE.equals(sessao.getTeveRedacao()) && sessao.getNotaRedacao() != null) {
                expGanha += (int) (sessao.getNotaRedacao() / 2); // Ex: Nota 900 dá 450 EXP
            }

            usuario.ganharExp(expGanha);
            
            // Aqui calculamos o Rank Global do Caçador
            List<Materia> todasMaterias = materiaRepo.findByUsuarioId(usuario.getId());
            int totalQ = todasMaterias.stream().mapToInt(m -> m.getTotalQuestoesRespondidas() != null ? m.getTotalQuestoesRespondidas() : 0).sum();
            int totalA = todasMaterias.stream().mapToInt(m -> m.getTotalAcertos() != null ? m.getTotalAcertos() : 0).sum();

            if (totalQ > 0) {
                double aprov = ((double) totalA / totalQ) * 100;
                if (aprov >= 95) usuario.setRankGeral("SS");
                else if (aprov >= 85) usuario.setRankGeral("S");
                else if (aprov >= 75) usuario.setRankGeral("A");
                else if (aprov >= 65) usuario.setRankGeral("B");
                else if (aprov >= 50) usuario.setRankGeral("C");
                else if (aprov >= 40) usuario.setRankGeral("D");
                else if (aprov >= 25) usuario.setRankGeral("E");
                else usuario.setRankGeral("F");
            }
            usuarioRepo.save(usuario);
        }

        return "redirect:/dashboard";
    }
}