package com.edugame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EduGameApplication {

	public static void main(String[] args) {
		SpringApplication.run(EduGameApplication.class, args);
		System.out.println("🛡️ Servidor do EduGame iniciado com sucesso! A guilda está aberta.");
	}
}