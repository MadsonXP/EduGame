package com.edugame.web.controladores;

import com.edugame.persistencia.modelo.Materia;
import com.edugame.persistencia.modelo.Usuario;
import com.edugame.persistencia.repositorio.MateriaRepo;
import com.edugame.persistencia.repositorio.UsuarioRepo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PerfilController {

    private final UsuarioRepo usuarioRepo;
    private final MateriaRepo materiaRepo;

    // Agora o Perfil também conhece as Matérias
    public PerfilController(UsuarioRepo usuarioRepo, MateriaRepo materiaRepo) {
        this.usuarioRepo = usuarioRepo;
        this.materiaRepo = materiaRepo;
    }

    @GetMapping("/perfil")
    public String exibirPerfil(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername()).orElse(null);
        
        if (usuario == null) {
            return "redirect:/login";
        }

        // Busca as disciplinas para ver o Rank de cada uma
        List<Materia> materias = materiaRepo.findByUsuarioId(usuario.getId());
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("materias", materias);
        
        return "perfil/perfil"; 
    }
}