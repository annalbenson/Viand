# Viand Backlog

Items are roughly priority-ordered within each section. Move things around freely.

---

## Recipes

- [ ] **Macro/nutrition display** — show calorie + protein counts on recipe detail screen (Spoonacular's `/recipes/{id}/nutritionWidget.json` endpoint)
- [ ] **Recipe web clipper** — import a recipe from any URL (parse title, ingredients, steps)

## Pantry

- [ ] **Pantry tracker** — let users mark ingredients they have on hand; Vivian automatically factors pantry contents into suggestions

## Grocery List

- [ ] **Aisle-sorted grocery list** — group items by store section (produce, dairy, meat, etc.) instead of alphabetically

## Household Sharing

- [ ] **Shared meal plans and grocery lists** — multi-user households; shared plan visible to all members, collaborative grocery list

## Vivian / Recommendations

- [ ] Apply dietary filtering to Vivian test-mode chat (`sendTestModeRequest` still calls unfiltered `searchRecipes`)
- [ ] Surface "why we recommended this" — show top cuisine + what drove it (e.g. "Because you love Fish → Japanese") on Vivian recommendation cards

## Taste Profile

- [ ] Show a summary of the user's top ingredients and cuisines on the Taste Profile screen after quiz completion
- [ ] Allow re-rating individual ingredients without retaking the full quiz (tap to edit on a summary list)

## Account / Settings

- [ ] Add ability to edit account name post-signup (AccountSettingsActivity already exists, just missing the name field)

## Recipe Book

- [ ] **Recipe book tab** — a pinboard-style tab where users can save recipe links with an optional notes field for each; think Pinterest for recipes

## Testing / Release

- [ ] Push to release build and test Gemini chat path end-to-end
- [ ] Add unit tests for `getTopCuisineWithIngredients` in TasteEngine

---

## Completed

- [x] Dietary filtering on Vivian recommendations (efd19ec)
- [x] Dietary filtering on ingredient suggestions in Vivian (b95d7af)
- [x] Dietary filtering on home screen search (144305c)
- [x] Ingredient preference quiz — 25-ingredient rating screen (60fc3ca)
- [x] Wire ingredient quiz scores into Vivian cuisine selection (144305c)
- [x] Resume ingredient quiz from last unanswered item (144305c)
- [x] Account settings — edit dietary preferences post-signup (b95d7af)
- [x] Seeded test data with real Spoonacular snapshots (3b4d788)
