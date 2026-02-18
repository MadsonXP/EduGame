package com.edugame.persistencia.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessoes_estudo")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SessaoEstudo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quem foi o Herói que lutou?
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Qual foi o Monstro (Matéria) enfrentado?
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    @Column(length = 200)
    private String assunto; // Ex: "Derivadas", "Leis de Newton"

    @Column(name = "tempo_minutos", nullable = false)
    private Integer tempoMinutos; // O tempo que o cronómetro marcou

    @Column(name = "questoes_feitas", nullable = false)
    private Integer questoesFeitas;

    @Column(name = "questoes_acertos", nullable = false)
    private Integer questoesAcertos;

    // O "Boss Fights" (Redações)
    @Column(name = "teve_redacao", nullable = false)
    private Boolean teveRedacao = false;

    @Column(name = "nota_redacao")
    private Double notaRedacao; // Pode ficar vazio se não for dia de redação

    @Column(name = "data_sessao", nullable = false)
    private LocalDateTime dataSessao;

    // Garante que a data seja preenchida automaticamente com o momento atual se você esquecer
    @PrePersist
    public void prePersist() {
        if (dataSessao == null) {
            dataSessao = LocalDateTime.now();
        }
    }
}