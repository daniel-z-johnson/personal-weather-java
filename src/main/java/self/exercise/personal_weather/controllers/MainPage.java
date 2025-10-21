package self.exercise.personal_weather.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import self.exercise.personal_weather.repository.GeolocationRepo;

@RequiredArgsConstructor
@Controller
public class MainPage {

    private final GeolocationRepo geolocationRepo;

    @GetMapping("/")
    public String mainPage(Model model){
        model.addAttribute("title", "Personal Weather page");
        var locs = geolocationRepo.getLocations();
        model.addAttribute("locs", locs);
        return "home";
    }
}
