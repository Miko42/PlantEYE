# Planteye 🌿 - Aplikacja mobilna do rozpoznawania roślin

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-purple?logo=kotlin) ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?logo=android) ![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Repository-green) ![DI](https://img.shields.io/badge/DI-Hilt-orange)

Zaawansowana aplikacja mobilna na Androida, umożliwiająca rozpoznawanie roślin, katalogowanie ich oraz wizualizację znalezisk na mapie. Projekt napisany w 100% w **Kotlin** z użyciem **Jetpack Compose**, demonstrujący integrację sprzętową (CameraX, GPS) oraz obsługę map (OpenStreetMap).

---

## 📱 Przegląd Aplikacji

Aplikacja pozwala użytkownikowi na zrobienie zdjęcia rośliny, otrzymanie natychmiastowej identyfikacji (wraz ze zdjęciami podobnymi) oraz zapisanie znaleziska w lokalnej bazie danych z oznaczeniem lokalizacji.

https://github.com/user-attachments/assets/aaa89505-f5fd-4eea-b4b5-bb0dc3a67bb6




---

## 🛠️ Tech Stack & Biblioteki

* **UI:** Jetpack Compose (Material3) – w pełni deklaratywny interfejs.
* **Architektura:** MVVM (Model-View-ViewModel) z Repository Pattern.
* **Dependency Injection:** Hilt (Dagger) – zarządzanie zależnościami.
* **Hardware Integration:**
    * **CameraX:** Niestandardowa implementacja aparatu wewnątrz Compose (`LifecycleCameraController`).
    * **GPS:** Google Play Services Location – pobieranie geotagów dla znalezisk.
* **Mapy:** **OSMDroid** – implementacja OpenStreetMap z wykorzystaniem `AndroidView` do renderowania mapy w środowisku Compose.
* **Baza Danych:** Room Database + Kotlin Coroutines (Flow) do reaktywnego wyświetlania biblioteki.
* **Networking:** OkHttp + Kotlinx Serialization.
* **Obrazy:** Coil – asynchroniczne ładowanie zdjęć.

---

## 🌐 Integracja z API i Warstwa Sieciowa

Aplikacja komunikuje się z zewnętrznym API **Plant.id (v3)** w celu identyfikacji roślin. Warstwa sieciowa została zaprojektowana z naciskiem na wydajność i bezpieczeństwo.

* **Klient HTTP:** OkHttp.
* **Concurrency:** Wszystkie operacje sieciowe wykonywane są na wątku `Dispatchers.IO` przy użyciu **Kotlin Coroutines**, co zapewnia płynność UI.
* **Przetwarzanie danych:**
    * **Image Preprocessing:** Przed wysłaniem, obrazy z URI są konwertowane do formatu **Base64**.
    * **Smart Context:** Zapytanie do API jest wzbogacane o **lokalizację GPS** użytkownika (dla zawężenia gatunków występujących w danym rejonie) oraz **ustawienia językowe** urządzenia (`Locale.getDefault()`), aby otrzymać opis w odpowiednim języku.
* **Parsowanie:** `Kotlinx Serialization` do bezpiecznego typowania odpowiedzi JSON.
* **Bezpieczeństwo:** Wrażliwe dane (API Key) są zarządzane poprzez plik **.env** i wstrzykiwane do aplikacji w czasie budowania (`BuildConfig`). Zapewnia to bezpieczeństwo kluczy i separację konfiguracji od kodu.

---

## 🚀 Główne Funkcjonalności

* **Skaner:** Wykonywanie zdjęć, obsługa galerii, podgląd na żywo.
* **Analiza:** Wyświetlanie rozpoznanej rośliny, nazwy łacińskiej, opisu oraz galerii podobnych obrazów.
* **Biblioteka:** Lokalny zapis historii skanowania z możliwością usuwania wpisów.
* **Mapa:** Wizualizacja wszystkich odkrytych roślin na mapie z customowymi markerami.

---

## 💻 Jak uruchomić projekt

Wymagania: Android Studio, JDK 17, urządzenie z Androidem (min. SDK 26).

1.  Sklonuj repozytorium:
    ```bash
    git clone https://github.com/Miko42/PlantEYE.git
    ```
2.  **Konfiguracja środowiska:**
    Projekt korzysta z pliku `.env` do przechowywania klucza API.
    * Utwórz plik o nazwie `.env` w głównym katalogu projektu.
    * Dodaj w nim swój klucz do API Plant.id:
      ```properties
      API_KEY=twoj_klucz_api_tutaj
      ```
    
    Plant.id udostępnia za darmo klucz testowy umożliwiający wykonanie do 100 zapytań.
    
3.  Zbuduj projekt i uruchom na urządzeniu.

---

## 📫 Kontakt

Projekt stworzony przez **Mikołaja Politowskiego**.
Znajdź mnie na [LinkedIn](https://www.linkedin.com/in/miko%C5%82aj-politowski-76a919249/) lub napisz: [mikolaj.politowski@gmail.com].
