# Viand

An Android recipe discovery and cooking assistant app. Search for recipes, save favorites, create your own versions, and chat with Vivian — an AI cooking assistant — about what you can make with whatever's in your fridge.

## Features

- **Recipe search** — powered by the Spoonacular API; horizontal card browse with images, ingredients, and step-by-step instructions
- **Favorites** — save recipes to a local SQLite list, organized by meal type (Breakfast, Lunch, Dinner, Dessert, Snack); rate each with custom thumbs-up / neutral / thumbs-down icons after you try it; tap the category label on any card to reassign it
- **Create My Version** — customize any search result into your own recipe with editable title, ingredients, and instructions
- **Vivian AI** — chat with an AI cooking assistant (Google Gemini 2.0 Flash) that suggests recipes based on what you have; Vivian only responds to cooking and meal-related messages (non-food queries are deflected without an API call); ask things like "suggest something with chicken" or "what can I make using eggs?" and Vivian searches Spoonacular for matching recipes; ask "what sounds good?" or "help me decide" and Vivian returns three personalized picks:
  - *Your Recent Go-To* — a random saved favorite
  - *Try Something Similar!* — a Spoonacular result from a cuisine similar to your taste profile
  - *Feeling Bold?* — a result from a more adventurous cuisine two hops away
- **Meal log** — tap "I Made This!" on any recipe to log it; ask Vivian "what did I cook last week?" or "what have I been eating that I liked?" for a formatted history with meal types, ratings, and dates — no Gemini call needed
- **Taste profile** — Vivian learns your preferences over time from saves and ratings; she occasionally asks quick questions ("Do you like fish? Often / Sometimes / Rarely / Never") to refine her recommendations; edit everything manually via the Taste Profile screen
- **Weekly meal planner** — add any recipe to your meal plan while viewing it; pick a day (Monday–Sunday) or drop it in an unscheduled Bucket; view your whole week at a glance, delete entries, and tap "Build Grocery List" to auto-generate a consolidated, alphabetically sorted shopping list grouped by ingredient name with quantities and source recipes
- **User accounts** — local login with email and password; dietary preferences (Gluten Free, Vegetarian, Vegan, Kosher, Halal, Dairy Free, Nut Free) saved at signup; all data (favorites, recipes, taste profile, meal log, meal plan) is fully isolated per account
- **Remember me** — optional credential persistence so you skip the login screen on return visits
- **Personalized greeting** — "Hello, [name]" banner with sign-out and Taste Profile access on the main screen

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
├── engine/       TasteEngine — cuisine similarity graph and recommendation logic
├── models/       POJO data models
├── data/         DatabaseHandler (SQLite, schema v11)
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

> `TEST_MODE` in `VivianActivity.java` is tied to `BuildConfig.DEBUG` — debug builds use Spoonacular responses instead of Gemini; release builds use real AI. Recommendation requests ("what sounds good?") work in both modes.

## Design Resources

- Theme: [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/)
- Icons: [IconKitchen](https://icon.kitchen)
