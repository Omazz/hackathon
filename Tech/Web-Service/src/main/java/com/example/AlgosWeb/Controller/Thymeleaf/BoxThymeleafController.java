package com.example.AlgosWeb.Controller.Thymeleaf;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/box")
public class BoxThymeleafController {
    @GetMapping
    public String mainPage(){
        return "addNewToDB";
    }
}
