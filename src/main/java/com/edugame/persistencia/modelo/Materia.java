package com.edugame.persistencia.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "materias")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Materia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento: Cada matéria pertence a um caçador (utilizador) específico
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "O nome da disciplina é obrigatório.")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
    private String nome;

    // Cor em formato Hexadecimal (ex: #FF5733) para pintar os gráficos do Dashboard
    @Column(name = "cor_hex", nullable = false, length = 7)
    private String corHex = "#000000"; 

    // ==========================================
    // 📊 ESTATÍSTICAS DE COMBATE (O Rank do Monstro)
    // ==========================================

    @Column(name = "total_questoes", nullable = false)
    private Integer totalQuestoesRespondidas = 0;

    @Column(name = "total_acertos", nullable = false)
    private Integer totalAcertos = 0;

    // Método mágico: Calcula o Rank da matéria (Sistema Hardcore)
    public String getRankAtual() {
        // Se tem menos de 10 questões resolvidas, é automaticamente F (Sem conversa!)
        if (totalQuestoesRespondidas == null || totalQuestoesRespondidas < 10) return "F"; 
        
        double aproveitamento = ((double) totalAcertos / totalQuestoesRespondidas) * 100;
        
        // Exigências brutais para o topo:
        if (aproveitamento >= 95 && totalQuestoesRespondidas >= 300) return "SS"; // Lendário
        if (aproveitamento >= 90 && totalQuestoesRespondidas >= 150) return "S";  // Mestre
        if (aproveitamento >= 80 && totalQuestoesRespondidas >= 100) return "A";  // Elite
        
        // O meio do caminho agora exige notas de corte altas:
        if (aproveitamento >= 70 && totalQuestoesRespondidas >= 50) return "B";   // Veterano
        if (aproveitamento >= 60 && totalQuestoesRespondidas >= 30) return "C";   // Aventureiro
        
        // Base mais punitiva:
        if (aproveitamento >= 50 && totalQuestoesRespondidas >= 20) return "D";   // Iniciante
        if (aproveitamento >= 40 && totalQuestoesRespondidas >= 10) return "E";   // Aprendiz
        
        // Se aproveitamento for menor que 40%, continua no fundo:
        return "F";                                                               // Novato
    }
}