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

   // Método mágico: Calcula o Rank da matéria com balanceamento de RPG (Exige volume!)
    public String getRankAtual() {
        if (totalQuestoesRespondidas == null || totalQuestoesRespondidas == 0) return "F"; 
        
        double aproveitamento = ((double) totalAcertos / totalQuestoesRespondidas) * 100;
        
        // Para ser SS, tem de ter 90%+ de acerto E já ter feito pelo menos 200 questões na matéria
        if (aproveitamento >= 90 && totalQuestoesRespondidas >= 200) return "SS"; // Divino
        
        // Para ser S, 80%+ E pelo menos 100 questões
        if (aproveitamento >= 80 && totalQuestoesRespondidas >= 100) return "S";  // Mestre
        
        // Para ser A, 75%+ E pelo menos 50 questões
        if (aproveitamento >= 75 && totalQuestoesRespondidas >= 50) return "A";   // Elite
        
        // Ranks mais baixos exigem menos volume
        if (aproveitamento >= 65 && totalQuestoesRespondidas >= 30) return "B";   // Veterano
        if (aproveitamento >= 50 && totalQuestoesRespondidas >= 10) return "C";   // Aventureiro
        
        // Ranks base (apenas a percentagem conta)
        if (aproveitamento >= 40) return "D";  // Iniciante
        if (aproveitamento >= 25) return "E";  // Aprendiz
        return "F";                            // Novato
    }
}