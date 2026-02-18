package com.edugame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.edugame")
@EnableJpaRepositories(basePackages = "com.edugame.persistencia.repositorio")
@EntityScan(basePackages = "com.edugame.persistencia.modelo")
public class EduGameApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduGameApplication.class, args);
        System.out.println("\n🛡️  -------------------------------------------------");
        System.out.println("🛡️  EduGame iniciado com sucesso! A guilda está aberta.");
        System.out.println("🛡️  Acesse: http://localhost:8080/login");
        System.out.println("🛡️  -------------------------------------------------\n");
    }
}