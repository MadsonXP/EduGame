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

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        // Estatísticas Globais
        int totalQuestoes = sessoes.stream().mapToInt(s -> s.getQuestoesFeitas() != null ? s.getQuestoesFeitas() : 0).sum();
        int totalMinutos = sessoes.stream().mapToInt(s -> s.getTempoMinutos() != null ? s.getTempoMinutos() : 0).sum();
        long totalRedacoes = sessoes.stream().filter(s -> Boolean.TRUE.equals(s.getTeveRedacao())).count();
        double totalHoras = totalMinutos / 60.0;

        // 1. DADOS PARA O GRÁFICO DE ROSQUINHA (Horas por Matéria)
        List<String> nomesMaterias = materias.stream().map(Materia::getNome).collect(Collectors.toList());
        List<String> coresMaterias = materias.stream().map(Materia::getCorHex).collect(Collectors.toList());
        List<Double> horasPorMateria = materias.stream().map(m -> {
            int minutos = sessoes.stream()
                    .filter(s -> s.getMateria().getId().equals(m.getId()))
                    .mapToInt(s -> s.getTempoMinutos() != null ? s.getTempoMinutos() : 0).sum();
            return minutos / 60.0;
        }).collect(Collectors.toList());

        // 2. DADOS PARA O GRÁFICO DE QUESTÕES NO TEMPO
        List<Map<String, Object>> dadosSessoes = sessoes.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("data", s.getDataSessao().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            map.put("materia", s.getMateria().getNome());
            map.put("questoes", s.getQuestoesFeitas() != null ? s.getQuestoesFeitas() : 0);
            return map;
        }).collect(Collectors.toList());

        // 3. DADOS PARA O GRÁFICO DE REDAÇÕES (Evolução da Nota)
        List<Map<String, Object>> dadosRedacoes = sessoes.stream()
                .filter(s -> Boolean.TRUE.equals(s.getTeveRedacao()))
                .sorted(Comparator.comparing(SessaoEstudo::getDataSessao)) // Ordenar da mais antiga para a mais recente
                .map(s -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("data", s.getDataSessao().format(DateTimeFormatter.ofPattern("dd/MM"))); // Apenas dia e mês para ficar limpo
                    map.put("nota", s.getNotaRedacao() != null ? s.getNotaRedacao() : 0.0);
                    return map;
                }).collect(Collectors.toList());

        model.addAttribute("usuario", usuario);
        model.addAttribute("totalQuestoes", totalQuestoes);
        model.addAttribute("totalHoras", String.format(java.util.Locale.US, "%.1f", totalHoras));
        model.addAttribute("totalRedacoes", totalRedacoes);
        
        // Enviando os dados de gráficos para o HTML
        model.addAttribute("nomesMaterias", nomesMaterias);
        model.addAttribute("coresMaterias", coresMaterias);
        model.addAttribute("horasPorMateria", horasPorMateria);
        model.addAttribute("dadosSessoes", dadosSessoes);
        model.addAttribute("dadosRedacoes", dadosRedacoes);

        return "dashboard/index";
    }
}