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

        LocalDate data = LocalDate.parse(dataEstudo);
        LocalTime inicio = LocalTime.parse(horaInicio);
        LocalTime fim = LocalTime.parse(horaFim);

        LocalDateTime dataSessaoFinal = LocalDateTime.of(data, fim);

        if (dataSessaoFinal.isAfter(LocalDateTime.now())) {
            dataSessaoFinal = LocalDateTime.now();
        }
        sessao.setDataSessao(dataSessaoFinal);

        long minutos = Duration.between(inicio, fim).toMinutes();
        if (minutos <= 0) minutos = 1; 
        sessao.setTempoMinutos((int) minutos);

        if (Boolean.FALSE.equals(sessao.getTeveRedacao())) {
            sessao.setNotaRedacao(null);
        }

        int tempo = sessao.getTempoMinutos() != null ? sessao.getTempoMinutos() : 0;
        int qFeitas = sessao.getQuestoesFeitas() != null ? sessao.getQuestoesFeitas() : 0;
        int qAcertos = sessao.getQuestoesAcertos() != null ? sessao.getQuestoesAcertos() : 0;

        if (qAcertos > qFeitas) {
            qAcertos = qFeitas;
            sessao.setQuestoesAcertos(qAcertos);
        }

        if (Boolean.TRUE.equals(sessao.getTeveRedacao()) && sessao.getNotaRedacao() != null) {
            int pesoRedacao = 10; 
            int acertosRedacao = (int) (sessao.getNotaRedacao() / 100); 
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

        if (usuario != null) {
            int expGanha = (tempo * 2) + (qFeitas * 5) + (qAcertos * 10); 
            if (Boolean.TRUE.equals(sessao.getTeveRedacao()) && sessao.getNotaRedacao() != null) {
                expGanha += (int) (sessao.getNotaRedacao() / 2); 
            }
            usuario.ganharExp(expGanha);
            atualizarRankGlobal(usuario);
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/historico/deletar")
    public String deletarSessao(@RequestParam Long sessaoId, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername()).orElse(null);
        SessaoEstudo sessao = sessaoRepo.findById(sessaoId).orElse(null);

        if (usuario != null && sessao != null && sessao.getUsuario().getId().equals(usuario.getId())) {
            Materia materia = sessao.getMateria();

            int tempo = sessao.getTempoMinutos() != null ? sessao.getTempoMinutos() : 0;
            int qFeitas = sessao.getQuestoesFeitas() != null ? sessao.getQuestoesFeitas() : 0;
            int qAcertos = sessao.getQuestoesAcertos() != null ? sessao.getQuestoesAcertos() : 0;
            int expRemover = (tempo * 2) + (qFeitas * 5) + (qAcertos * 10);

            if (Boolean.TRUE.equals(sessao.getTeveRedacao()) && sessao.getNotaRedacao() != null) {
                qFeitas += 10;
                qAcertos += (int) (sessao.getNotaRedacao() / 100);
                expRemover += (int) (sessao.getNotaRedacao() / 2);
            }

            if (materia != null) {
                materia.setTotalQuestoesRespondidas(Math.max(0, materia.getTotalQuestoesRespondidas() - qFeitas));
                materia.setTotalAcertos(Math.max(0, materia.getTotalAcertos() - qAcertos));
                materiaRepo.save(materia);
            }

            usuario.removerExp(expRemover);
            sessaoRepo.delete(sessao);
            atualizarRankGlobal(usuario);
        }
        return "redirect:/materias/historico";
    }

    // ==========================================
    // ✏️ SISTEMA DE EDIÇÃO DE SESSÃO (CORRIGIDO)
    // ==========================================
    @GetMapping("/historico/editar/{id}")
    public String exibirFormularioEdicao(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername()).orElse(null);
        SessaoEstudo sessao = sessaoRepo.findById(id).orElse(null);

        if (usuario == null || sessao == null || !sessao.getUsuario().getId().equals(usuario.getId())) {
            return "redirect:/materias/historico";
        }

        // Matemática para descobrir a Hora de Início original
        LocalTime horaFim = sessao.getDataSessao().toLocalTime();
        int duracao = sessao.getTempoMinutos() != null ? sessao.getTempoMinutos() : 0;
        LocalTime horaInicio = horaFim.minusMinutes(duracao);

        model.addAttribute("sessao", sessao);
        model.addAttribute("materias", materiaRepo.findByUsuarioId(usuario.getId()));
        
        // Passa os horários formatados para o Thymeleaf preencher o ecrã
        model.addAttribute("horaInicioFmt", String.format("%02d:%02d", horaInicio.getHour(), horaInicio.getMinute()));
        model.addAttribute("horaFimFmt", String.format("%02d:%02d", horaFim.getHour(), horaFim.getMinute()));

        return "materias/editar-sessao";
    }

    @PostMapping("/historico/editar")
    public String editarSessao(@ModelAttribute("sessao") SessaoEstudo sessaoAtualizada,
                               @RequestParam Long materiaId,
                               @RequestParam String dataEstudoStr,
                               @RequestParam String horaInicioStr,
                               @RequestParam String horaFimStr,
                               @AuthenticationPrincipal UserDetails userDetails) {
        
        Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername()).orElse(null);
        SessaoEstudo sessaoAntiga = sessaoRepo.findById(sessaoAtualizada.getId()).orElse(null);

        if (usuario != null && sessaoAntiga != null && sessaoAntiga.getUsuario().getId().equals(usuario.getId())) {

            // --- 1. REVERTER OS STATUS ANTIGOS ---
            Materia materiaAntiga = sessaoAntiga.getMateria();
            int tempoAntigo = sessaoAntiga.getTempoMinutos() != null ? sessaoAntiga.getTempoMinutos() : 0;
            int qFeitasAntigo = sessaoAntiga.getQuestoesFeitas() != null ? sessaoAntiga.getQuestoesFeitas() : 0;
            int qAcertosAntigo = sessaoAntiga.getQuestoesAcertos() != null ? sessaoAntiga.getQuestoesAcertos() : 0;
            int expRemover = (tempoAntigo * 2) + (qFeitasAntigo * 5) + (qAcertosAntigo * 10);

            if (Boolean.TRUE.equals(sessaoAntiga.getTeveRedacao()) && sessaoAntiga.getNotaRedacao() != null) {
                qFeitasAntigo += 10;
                qAcertosAntigo += (int) (sessaoAntiga.getNotaRedacao() / 100);
                expRemover += (int) (sessaoAntiga.getNotaRedacao() / 2);
            }

            if (materiaAntiga != null) {
                materiaAntiga.setTotalQuestoesRespondidas(Math.max(0, materiaAntiga.getTotalQuestoesRespondidas() - qFeitasAntigo));
                materiaAntiga.setTotalAcertos(Math.max(0, materiaAntiga.getTotalAcertos() - qAcertosAntigo));
                materiaRepo.save(materiaAntiga);
            }
            usuario.removerExp(expRemover);

            // --- 2. APLICAR OS NOVOS STATUS EDITADOS ---
            Materia materiaNova = materiaRepo.findById(materiaId).orElse(null);
            sessaoAntiga.setMateria(materiaNova);
            sessaoAntiga.setAssunto(sessaoAtualizada.getAssunto());

            LocalDate data = LocalDate.parse(dataEstudoStr);
            LocalTime inicio = LocalTime.parse(horaInicioStr);
            LocalTime fim = LocalTime.parse(horaFimStr);
            
            LocalDateTime dataSessaoFinal = LocalDateTime.of(data, fim);
            if (dataSessaoFinal.isAfter(LocalDateTime.now())) dataSessaoFinal = LocalDateTime.now();
            sessaoAntiga.setDataSessao(dataSessaoFinal);

            // Calcula a duração nova (Início -> Fim)
            long minutos = Duration.between(inicio, fim).toMinutes();
            if (minutos <= 0) minutos = 1;
            int tempoNovo = (int) minutos;
            sessaoAntiga.setTempoMinutos(tempoNovo);

            sessaoAntiga.setTeveRedacao(sessaoAtualizada.getTeveRedacao());
            if (Boolean.FALSE.equals(sessaoAntiga.getTeveRedacao())) {
                sessaoAntiga.setNotaRedacao(null);
            } else {
                sessaoAntiga.setNotaRedacao(sessaoAtualizada.getNotaRedacao());
            }

            int qFeitasNovo = sessaoAtualizada.getQuestoesFeitas() != null ? sessaoAtualizada.getQuestoesFeitas() : 0;
            int qAcertosNovo = sessaoAtualizada.getQuestoesAcertos() != null ? sessaoAtualizada.getQuestoesAcertos() : 0;
            if (qAcertosNovo > qFeitasNovo) qAcertosNovo = qFeitasNovo; 
            
            sessaoAntiga.setQuestoesFeitas(qFeitasNovo);
            sessaoAntiga.setQuestoesAcertos(qAcertosNovo);

            int pesoRedacaoNovo = 0;
            int acertosRedacaoNovo = 0;
            if (Boolean.TRUE.equals(sessaoAntiga.getTeveRedacao()) && sessaoAntiga.getNotaRedacao() != null) {
                pesoRedacaoNovo = 10;
                acertosRedacaoNovo = (int) (sessaoAntiga.getNotaRedacao() / 100);
            }

            if (materiaNova != null) {
                materiaNova.setTotalQuestoesRespondidas(materiaNova.getTotalQuestoesRespondidas() + qFeitasNovo + pesoRedacaoNovo);
                materiaNova.setTotalAcertos(materiaNova.getTotalAcertos() + qAcertosNovo + acertosRedacaoNovo);
                materiaRepo.save(materiaNova);
            }

            int expGanha = (tempoNovo * 2) + (qFeitasNovo * 5) + (qAcertosNovo * 10);
            if (Boolean.TRUE.equals(sessaoAntiga.getTeveRedacao()) && sessaoAntiga.getNotaRedacao() != null) {
                expGanha += (int) (sessaoAntiga.getNotaRedacao() / 2);
            }
            usuario.ganharExp(expGanha); 
            
            sessaoRepo.save(sessaoAntiga);
            atualizarRankGlobal(usuario);
        }

        return "redirect:/materias/historico";
    }
// ==========================================
    // 🏷️ SISTEMA DE EDIÇÃO DE MATÉRIA
    // ==========================================
    @GetMapping("/editar/{id}")
    public String exibirFormularioEdicaoMateria(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername()).orElse(null);
        Materia materia = materiaRepo.findById(id).orElse(null);

        // Segurança: Só permite aceder se a matéria for do próprio utilizador
        if (usuario == null || materia == null || !materia.getUsuario().getId().equals(usuario.getId())) {
            return "redirect:/materias";
        }

        model.addAttribute("materia", materia);
        return "materias/editar-materia";
    }

    @PostMapping("/editar")
    public String editarMateria(@ModelAttribute("materia") Materia materiaAtualizada, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername()).orElse(null);
        Materia materiaAntiga = materiaRepo.findById(materiaAtualizada.getId()).orElse(null);

        // Verifica se a matéria pertence a este utilizador antes de alterar
        if (usuario != null && materiaAntiga != null && materiaAntiga.getUsuario().getId().equals(usuario.getId())) {
            materiaAntiga.setNome(materiaAtualizada.getNome());
            materiaAntiga.setCorHex(materiaAtualizada.getCorHex()); // Também podes mudar a cor da aura!
            
            materiaRepo.save(materiaAntiga);
        }
        
        return "redirect:/materias";
    }
    private void atualizarRankGlobal(Usuario usuario) {
        List<Materia> todasMaterias = materiaRepo.findByUsuarioId(usuario.getId());
        double somaAproveitamentos = 0;
        int materiasComBatalha = 0;
        int totalQuestoesGlobal = 0;

        for (Materia m : todasMaterias) {
            int q = m.getTotalQuestoesRespondidas() != null ? m.getTotalQuestoesRespondidas() : 0;
            int a = m.getTotalAcertos() != null ? m.getTotalAcertos() : 0;
            totalQuestoesGlobal += q;
            if (q > 0) {
                somaAproveitamentos += ((double) a / q) * 100;
                materiasComBatalha++;
            }
        }

        if (materiasComBatalha > 0) {
            double mediaGeral = somaAproveitamentos / materiasComBatalha;
            if (mediaGeral >= 95 && totalQuestoesGlobal >= 1000) usuario.setRankGeral("SS");
            else if (mediaGeral >= 90 && totalQuestoesGlobal >= 500) usuario.setRankGeral("S");
            else if (mediaGeral >= 80 && totalQuestoesGlobal >= 300) usuario.setRankGeral("A");
            else if (mediaGeral >= 70 && totalQuestoesGlobal >= 150) usuario.setRankGeral("B");
            else if (mediaGeral >= 60 && totalQuestoesGlobal >= 80) usuario.setRankGeral("C");
            else if (mediaGeral >= 50 && totalQuestoesGlobal >= 40) usuario.setRankGeral("D");
            else if (mediaGeral >= 40 && totalQuestoesGlobal >= 20) usuario.setRankGeral("E");
            else usuario.setRankGeral("F");
        } else {
            usuario.setRankGeral("F");
        }
        usuarioRepo.save(usuario);
    }
}