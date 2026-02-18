package com.edugame.web.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String abrirTelaLogin() {
        // Ele vai procurar o arquivo "login.html" dentro da pasta "templates/login/"
        return "login/login"; 
    }
}