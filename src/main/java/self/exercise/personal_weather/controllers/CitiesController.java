package self.exercise.personal_weather.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import self.exercise.personal_weather.models.CityForm;
import self.exercise.personal_weather.services.OpenWeatherService;

@Controller
@RequiredArgsConstructor
public class CitiesController {

    private final OpenWeatherService openWeatherService;

    @GetMapping("/cities")
    public String getCityForm() {
        return "weather/newCity";
    }

    @PostMapping("/cities")
    public String listCities(Model model, @ModelAttribute CityForm cityForm) {
        var cities = openWeatherService.getGeolocations(cityForm.getCity(),
                java.util.Optional.ofNullable(cityForm.getState()),
                java.util.Optional.ofNullable(cityForm.getCountry()));
        model.addAttribute("cities", cities);
        return "weather/newCity";
    }
}
