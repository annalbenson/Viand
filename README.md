# Viand

An Android meal planning app that aggregates ingredients across your weekly meal plan and checks nearby stores for availability.

## Features

- Search recipes and build a weekly meal plan
- Automatic ingredient list aggregation across planned meals
- Nearby grocery store lookup via device location
- Ingredient availability and pricing via Kroger API
- Local account system with login

## Tech Stack

- Java, Android SDK (min SDK 21, target SDK 33)
- androidx libraries
- SQLite via SQLiteOpenHelper
- Spoonacular API (recipe search + ingredients)
- Kroger API (store locations + product availability)

## Setup

1. Clone the repo
2. Open in Android Studio
3. Add your API keys to `local.properties`:
   ```
   SPOONACULAR_KEY=your_key_here
   KROGER_CLIENT_ID=your_id_here
   KROGER_CLIENT_SECRET=your_secret_here
   ```
4. Run on a device or emulator (API 21+)

> `local.properties` is gitignored and must be created manually on each machine.

## Design Resources

- Theme: [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/)
- Icons: [Kitchener](https://kitchen.io)
