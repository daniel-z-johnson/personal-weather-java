package self.exercise.personal_weather.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class Cities {

    private final Ci

    @GetMapping("/getcities")
    public String listCities(String city, String country, String state) {

    }
}
