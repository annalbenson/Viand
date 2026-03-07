# Viand

An Android recipe discovery and cooking assistant app. Search for recipes, save favorites, create your own versions, and chat with Vivian — an AI cooking assistant — about what you can make with whatever's in your fridge.

## Features

- **Recipe search** — powered by the Spoonacular API; horizontal card browse with images, ingredients, and step-by-step instructions
- **Favorites** — save recipes to a local SQLite list and revisit them anytime
- **Create My Version** — customize any search result into your own recipe with editable title, ingredients, and instructions
- **Vivian AI** — chat with an AI cooking assistant (Google Gemini 2.0 Flash) that suggests recipes based on what you have in your pantry or fridge; full multi-turn conversation; clickable recipe suggestions open the full detail screen
- **User accounts** — local login with email and password; dietary preferences (Gluten Free, Vegetarian, Vegan, Kosher, Halal, Dairy Free, Nut Free) saved at signup
- **Remember me** — optional credential persistence so you skip the login screen on return visits
- **Personalized greeting** — "Hello, [name]" banner with sign-out on the main screen

## Tech Stack

- Java, Android SDK (min SDK 21, target SDK 34)
- AndroidX + Material Components 3
- SQLite via `SQLiteOpenHelper`
- Retrofit 2 + Gson for all API calls
- Glide for image loading
- Spoonacular API — recipe search and detail
- Google Gemini 2.0 Flash API — Vivian AI chat

## Project Structure

```
app/src/main/java/com/annabenson/viand/
├── activities/   All Activity classes
├── adapters/     RecyclerView adapters and ViewHolders
├── models/       POJO data models
├── data/         DatabaseHandler (SQLite)
└── network/      Retrofit clients, service interfaces, API models
```

## Setup

1. Clone the repo
2. Open in Android Studio
3. Add your API keys to `local.properties` in the project root:
   ```
   SPOONACULAR_KEY=your_key_here
   GEMINI_KEY=your_key_here
   ```
   - Spoonacular: [spoonacular.com/food-api](https://spoonacular.com/food-api)
   - Gemini: [aistudio.google.com](https://aistudio.google.com)
4. Run on a device or emulator (API 21+)

> `local.properties` is gitignored and must be created manually on each machine.

> To develop without Gemini quota, set `TEST_MODE = true` in `PantryActivity.java` — Vivian will return Spoonacular search results instead of AI responses.

## Design Resources

- Theme: [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/)
- Icons: [IconKitchen](https://icon.kitchen)
