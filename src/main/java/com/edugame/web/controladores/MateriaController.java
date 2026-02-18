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

        // Lógica de Redação: se não foi marcado, a nota é limpa para evitar lixo no banco
        if (Boolean.FALSE.equals(sessao.getTeveRedacao())) {
            sessao.setNotaRedacao(null);
        }

        if (materia != null) {
            int qAnteriores = materia.getTotalQuestoesRespondidas() != null ? materia.getTotalQuestoesRespondidas() : 0;
            int aAnteriores = materia.getTotalAcertos() != null ? materia.getTotalAcertos() : 0;
            
            materia.setTotalQuestoesRespondidas(qAnteriores + sessao.getQuestoesFeitas());
            materia.setTotalAcertos(aAnteriores + sessao.getQuestoesAcertos());
            materiaRepo.save(materia);
        }

        sessaoRepo.save(sessao);
        return "redirect:/dashboard";
    }
}