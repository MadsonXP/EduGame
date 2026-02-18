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

    // Método mágico: Calcula o Rank da matéria em tempo real para a sua ficha
    public String getRankAtual() {
        if (totalQuestoesRespondidas == 0) return "F"; // Sem batalhas registadas
        
        double aproveitamento = ((double) totalAcertos / totalQuestoesRespondidas) * 100;
        
        if (aproveitamento >= 95) return "SS"; // Divino
        if (aproveitamento >= 85) return "S";  // Mestre
        if (aproveitamento >= 75) return "A";  // Elite
        if (aproveitamento >= 65) return "B";  // Veterano
        if (aproveitamento >= 50) return "C";  // Aventureiro
        if (aproveitamento >= 40) return "D";  // Iniciante
        if (aproveitamento >= 25) return "E";  // Aprendiz
        return "F";                            // Novato
    }
}