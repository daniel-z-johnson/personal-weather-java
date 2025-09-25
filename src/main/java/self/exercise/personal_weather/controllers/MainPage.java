package self.exercise.personal_weather.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainPage {

    @GetMapping("/")
    public String mainPage(Model model){
        model.addAttribute("title", "Personal Weather page");
        return "home";
    }
}
