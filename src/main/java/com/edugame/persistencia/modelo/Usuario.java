package com.edugame.persistencia.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "O nome é obrigatório.")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
    private String nome;

    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Insira um e-mail válido.")
    private String email;

    @Column(nullable = false, length = 255)
    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 3, message = "A senha deve ter pelo menos 3 caracteres.")
    private String senha;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDate dataCadastro;

    // ==========================================
    // 🛡️ SISTEMA DE VERIFICAÇÃO DE E-MAIL
    // ==========================================
    @Column(name = "conta_ativa", nullable = false)
    private boolean contaAtiva = false; 

    @Column(name = "codigo_verificacao", length = 64)
    private String codigoVerificacao; 

    // ==========================================
    // ⚔️ SISTEMA DE RPG (GAMIFICAÇÃO)
    // ==========================================

    @Column(nullable = false)
    private Integer nivel = 1; 

    @Column(name = "exp_atual", nullable = false)
    private Integer expAtual = 0; 

    @Column(name = "exp_prox_nivel", nullable = false)
    private Integer expParaProximoNivel = 1000; 

    @Column(name = "rank_geral", length = 2)
    private String rankGeral = "F"; 

    public void ganharExp(int expGanha) {
        this.expAtual += expGanha;
        verificarLevelUp();
    }

    private void verificarLevelUp() {
        while (this.expAtual >= this.expParaProximoNivel) {
            this.expAtual -= this.expParaProximoNivel; 
            this.nivel++;
            this.expParaProximoNivel = (int) (this.expParaProximoNivel * 1.2); 
        }
    }

    public void removerExp(int expPerdida) {
        this.expAtual -= expPerdida;
        
        while (this.expAtual < 0 && this.nivel > 1) {
            this.nivel--;
            this.expParaProximoNivel = (int) (this.expParaProximoNivel / 1.2); 
            this.expAtual += this.expParaProximoNivel;
        }
        
        if (this.nivel == 1 && this.expAtual < 0) {
            this.expAtual = 0;
        }
    }
}