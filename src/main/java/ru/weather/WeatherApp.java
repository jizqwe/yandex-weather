package ru.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class WeatherApp {
    private static final String API_URL = "https://api.weather.yandex.ru/v2/forecast";

    public static void main(String[] args) {
        String apiKey = System.getenv("YANDEX_WEATHER_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Ошибка: не задана переменная YANDEX_WEATHER_KEY");
            return;
        }

        double lat = 55.75;
        double lon = 37.62;
        int limit = 3;
        URI uri = URI.create(API_URL + "?lat=" + lat + "&lon=" + lon + "&limit=" + limit);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(20))
                .header("X-Yandex-Weather-Key", apiKey)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("HTTP-код ответа: " + response.statusCode());
            System.out.println(response.body());
            if (response.statusCode() != 200) {
                return;
            }
            printTemperatures(response.body(), limit);
        } catch (IOException e) {
            System.err.println("Ошибка при получении или обработке ответа: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Запрос прерван");
        }
    }

    static void printTemperatures(String json, int limit) throws IOException {
        JsonNode root = new ObjectMapper().readTree(json);
        JsonNode current = root.path("fact").path("temp");
        if (current.isNumber()) {
            System.out.printf("Текущая температура fact.temp: %s °C%n", current.asText());
        } else {
            System.err.println("В ответе отсутствует числовое поле fact.temp");
        }

        JsonNode forecasts = root.path("forecasts");
        if (!forecasts.isArray() || forecasts.size() < limit) {
            System.err.printf("API вернул недостаточно данных для прогноза на %d суток%n", limit);
            return;
        }

        double sum = 0;
        for (int i = 0; i < limit; i++) {
            JsonNode forecast = forecasts.get(i);
            JsonNode daily = forecast.path("parts").path("day").path("temp_avg");
            if (!daily.isNumber()) {
                System.err.printf("В прогнозе на день %d отсутствует числовое поле temp_avg%n", i + 1);
                return;
            }
            sum += daily.asDouble();
            System.out.printf("День %d (%s): %s °C%n", i + 1, forecast.path("date").asText(), daily.asText());
        }
        System.out.printf("Средняя температура за %d суток: %.2f °C%n", limit, sum / limit);
    }
}
