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
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final UsuarioRepo usuarioRepo;
    private final MateriaRepo materiaRepo;
    private final SessaoEstudoRepo sessaoRepo;

    public DashboardController(UsuarioRepo usuarioRepo, MateriaRepo materiaRepo, SessaoEstudoRepo sessaoRepo) {
        this.usuarioRepo = usuarioRepo;
        this.materiaRepo = materiaRepo;
        this.sessaoRepo = sessaoRepo;
    }

    @GetMapping("/dashboard")
    public String exibirDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername()).orElse(null);
        if (usuario == null) return "redirect:/login";

        List<Materia> materias = materiaRepo.findByUsuarioId(usuario.getId());
        List<SessaoEstudo> sessoes = sessaoRepo.findByUsuarioId(usuario.getId());

        // CORREÇÃO: Usando tempoMinutos, questoesFeitas e teveRedacao do seu modelo
        int totalQuestoes = sessoes.stream().mapToInt(s -> s.getQuestoesFeitas() != null ? s.getQuestoesFeitas() : 0).sum();
        int totalMinutos = sessoes.stream().mapToInt(s -> s.getTempoMinutos() != null ? s.getTempoMinutos() : 0).sum();
        long totalRedacoes = sessoes.stream().filter(s -> Boolean.TRUE.equals(s.getTeveRedacao())).count();
        
        double totalHoras = totalMinutos / 60.0;

        // Dados para os Gráficos
        List<String> nomesMaterias = materias.stream().map(Materia::getNome).collect(Collectors.toList());
        List<Integer> questoesPorMateria = materias.stream().map(Materia::getTotalQuestoesRespondidas).collect(Collectors.toList());

        model.addAttribute("usuario", usuario);
        model.addAttribute("totalQuestoes", totalQuestoes);
        model.addAttribute("totalHoras", String.format("%.1f", totalHoras));
        model.addAttribute("totalRedacoes", totalRedacoes);
        
        model.addAttribute("labelsMaterias", nomesMaterias);
        model.addAttribute("dadosQuestoes", questoesPorMateria);

        return "dashboard/index";
    }
}