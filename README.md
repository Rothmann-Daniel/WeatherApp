# WeatherApp - Приложение прогноз погоды 🌤️

Мобильное приложение для просмотра актуальной погоды и прогноза на ближайшие дни в любом городе мира.

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue?logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-7.0%2B-green?logo=android)](https://www.android.com)
[![API](https://img.shields.io/badge/API-WeatherAPI-orange)](https://www.weatherapi.com)

---

## ✨ Возможности

- 🌍 **Поиск по городам** - получение погоды для любого города мира
- 🌡️ **Текущая погода** - температура, влажность, скорость ветра
- 📅 **Прогноз на неделю** - детальный прогноз на ближайшие дни
- 🖼️ **Визуализация** - иконки погодных условий
- 🔄 **Актуальные данные** - информация в режиме реального времени

## 🛠 Технологии

### Core
- **Kotlin** - основной язык разработки
- **XML + ViewBinding** - создание UI

### Network & Data
- **Volley** - сетевые запросы (альтернатива Retrofit для изучения)
- **Gson** - парсинг JSON
- **WeatherAPI** - источник данных о погоде

### UI & Images
- **Picasso** - загрузка и кэширование изображений
- **Material Design** - современный дизайн интерфейса

## 📱 Системные требования

- Android 7.0 (API 24) или выше
- Интернет-соединение для получения данных

## 🚀 Установка и запуск

### Клонирование репозитория

```bash
git clone https://github.com/Rothmann-Daniel/WeatherApp.git
cd WeatherApp
```

### Настройка API ключа

1. Зарегистрируйтесь на [WeatherAPI.com](https://www.weatherapi.com)
2. Получите API ключ
3. Добавьте ключ в проект (в соответствующий файл конфигурации)

### Запуск

1. Откройте проект в Android Studio
2. Синхронизируйте Gradle
3. Запустите приложение на эмуляторе или реальном устройстве

## 📸 Скриншоты

<details>
<summary>Посмотреть скриншоты</summary>

<table>
  <tr>
    <td><img width="250" alt="Главный экран" src="https://github.com/user-attachments/assets/c286fe44-c56d-450e-9036-7b4dcd54f14a" /></td>
    <td><img width="250" alt="Поиск города" src="https://github.com/user-attachments/assets/1b681b76-5d9b-47fd-b7a7-d2313fd86255" /></td>
  </tr>
  <tr>
    <td><img width="250" alt="Прогноз на день" src="https://github.com/user-attachments/assets/88309376-2769-49cc-9fd9-149e063e8964" /></td>
    <td><img width="250" alt="Детальная информация" src="https://github.com/user-attachments/assets/07402c0c-63fb-4ae1-ab33-c7d65a9b0f0e" /></td>
  </tr>
</table>

</details>

## 🔑 Основные компоненты

### Сетевой слой
- Использование **Volley** для HTTP запросов
- Обработка ответов в формате JSON
- Кэширование запросов

### Парсинг данных
- **Gson** для десериализации JSON в Kotlin объекты
- Типобезопасные модели данных

### Работа с изображениями
- **Picasso** для асинхронной загрузки иконок погоды
- Автоматическое кэширование изображений
- Плейсхолдеры при загрузке

## 📚 API Reference

Приложение использует [WeatherAPI](https://www.weatherapi.com/docs/):
- Current Weather API - текущая погода
- Forecast API - прогноз на несколько дней
- Search/Autocomplete API - поиск городов

## 🎯 Планы развития

- [ ] Добавить сохранение избранных городов
- [ ] Реализовать офлайн режим с кэшированием
- [ ] Добавить виджет на главный экран
- [ ] Внедрить уведомления о погоде
- [ ] Миграция на Retrofit для оптимизации

## 👨‍💻 Автор

**Данила Ротман** - Android Developer

- 📱 Telegram: [@danielrothmann](https://t.me/danielrothmann)
- 🌐 GitHub: [@Rothmann-Daniel](https://github.com/Rothmann-Daniel)

## 📝 Лицензия

MIT License - см. файл [LICENSE](LICENSE)

## 🙏 Благодарности

- [WeatherAPI.com](https://www.weatherapi.com) - за предоставление данных о погоде
- [Material Design](https://material.io) - за UI компоненты
- Android Community - за поддержку и библиотеки

---

## 🌐 English Version

# WeatherApp - Weather Forecast Application 🌤️

A mobile application for viewing current weather and forecast for the coming days in any city worldwide.

## ✨ Features

- 🌍 **City Search** - get weather for any city in the world
- 🌡️ **Current Weather** - temperature, humidity, wind speed
- 📅 **Weekly Forecast** - detailed forecast for upcoming days
- 🖼️ **Visualization** - weather condition icons
- 🔄 **Real-time Data** - up-to-date information

## 🛠 Tech Stack

### Core
- **Kotlin** - primary development language
- **XML + ViewBinding** - UI creation

### Network & Data
- **Volley** - network requests (Retrofit alternative for learning)
- **Gson** - JSON parsing
- **WeatherAPI** - weather data source

### UI & Images
- **Picasso** - image loading and caching
- **Material Design** - modern interface design

## 📱 Requirements

- Android 7.0 (API 24) or higher
- Internet connection for data retrieval
