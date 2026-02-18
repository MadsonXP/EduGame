package com.edugame.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Libera o acesso à tela de login, cadastro e arquivos visuais (CSS, imagens)
                .requestMatchers("/login", "/cadastro", "/css/**", "/js/**", "/img/**").permitAll()
                // Libera o painel do banco de dados temporário (H2)
                .requestMatchers("/h2-console/**").permitAll()
                // Tranca TODO o resto do jogo. Só entra se tiver logado!
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                // Diz onde fica a nossa tela de login personalizada
                .loginPage("/login")
                // Para onde o jogador vai depois de acertar a senha
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout") // Volta pro login avisando que deslogou
                .permitAll()
            )
            // Configurações necessárias para o painel do H2 funcionar no navegador
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    // O Criptografador de Senhas (ninguém vai ver sua senha no banco de dados, nem você mesmo!)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}