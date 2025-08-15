package br.com.galsystem.construction.finance.controller.home;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        // O Spring procura em src/main/resources/templates/index.html (quando usa Thymeleaf)
        // ou em src/main/resources/static se for direto (mas nesse caso, sem retorno do Controller)
        return "index";
    }
}
