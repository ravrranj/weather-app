package com.ravi.weatherapp.weatherapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller  // ✅ Use @Controller, not @RestController, since we are returning a view
public class WeatherController {

    private final RestTemplate restTemplate;

    // ✅ Constructor injection
    public WeatherController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ✅ Securely inject your API key
    @Value("${weather.api.key}")
    private String apiKey;

    @GetMapping("/weather-ui")
    public String getWeatherUi(@RequestParam(required = false) String city, Model model) {
        if (city == null || city.isEmpty()) {
            model.addAttribute("message", "Enter a city name to get weather details!");
            return "weather"; // ✅ Matches weather.html (or weather-ui.html if that's what you named it)
        }

        String url = String.format(
                "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric",
                city, apiKey);

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = new ObjectMapper().readTree(response);

            double temp = root.path("main").path("temp").asDouble();
            String description = root.path("weather").get(0).path("description").asText();
            String iconCode = root.path("weather").get(0).path("icon").asText();
            String iconUrl = "http://openweathermap.org/img/wn/" + iconCode + "@2x.png";

            model.addAttribute("temperature", String.format("%.1f", temp));
            model.addAttribute("description", description);
            model.addAttribute("city", city);
            model.addAttribute("iconUrl", iconUrl);

        } catch (Exception e) {
            model.addAttribute("error", "Could not get weather data for " + city + ". Please try again.");
        }

        return "weather"; // ✅ Make sure weather.html is inside src/main/resources/templates/
    }
}
