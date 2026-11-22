# Сервис получения метеорологических данных

Консольное приложение на Java 17 и Maven. Запрашивает прогноз Яндекс Погоды для Москвы (55.75, 37.62) на три дня, выводит полный JSON, текущую температуру и среднее арифметическое дневных температур.

## Запуск в PowerShell

```powershell
$env:YANDEX_WEATHER_KEY = 'ваш_ключ'
mvn compile exec:java "-Dexec.mainClass=ru.weather.WeatherApp"
```

Ключ передаётся через заголовок `X-Yandex-Weather-Key`. Среднее считается по `forecasts[*].parts.day.temp_avg` за первые `limit` суток. Текущая температура берётся из `fact.temp`.

[Документация REST v2](https://yandex.ru/dev/weather/doc/ru/concepts/forecast-rest) · [Тарифы](https://yandex.ru/dev/weather/doc/ru/concepts/pricing)
