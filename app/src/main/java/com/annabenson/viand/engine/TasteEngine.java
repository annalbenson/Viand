package com.annabenson.viand.engine;

import com.annabenson.viand.data.DatabaseHandler;
import com.annabenson.viand.models.TasteTag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TasteEngine {

    public static final Map<String, List<String>> CUISINE_GRAPH;
    public static final Map<String, List<String>> INGREDIENT_CUISINE_MAP;

    private static final String[] PROMPT_TOPICS = {
        "fish", "spicy dishes", "vegetarian meals", "Italian food", "Japanese food",
        "Mexican food", "Thai food", "Indian food", "shellfish", "red meat", "dairy", "Chinese food"
    };

    static {
        Map<String, List<String>> graph = new LinkedHashMap<>();
        graph.put("Italian",        Arrays.asList("Mediterranean", "French", "Greek"));
        graph.put("Mediterranean",  Arrays.asList("Italian", "Greek", "Middle Eastern", "Spanish", "Moroccan"));
        graph.put("Greek",          Arrays.asList("Mediterranean", "Turkish", "Middle Eastern"));
        graph.put("French",         Arrays.asList("Italian", "Spanish", "European"));
        graph.put("Spanish",        Arrays.asList("French", "Mediterranean", "Latin American"));
        graph.put("Mexican",        Arrays.asList("Latin American", "Spanish", "Cajun"));
        graph.put("Latin American", Arrays.asList("Mexican", "Spanish", "Caribbean"));
        graph.put("Caribbean",      Arrays.asList("Latin American", "African"));
        graph.put("Indian",         Arrays.asList("Middle Eastern", "Thai", "Pakistani"));
        graph.put("Middle Eastern", Arrays.asList("Indian", "Mediterranean", "Greek", "Turkish", "Moroccan"));
        graph.put("Turkish",        Arrays.asList("Middle Eastern", "Greek", "Mediterranean"));
        graph.put("Moroccan",       Arrays.asList("Mediterranean", "Middle Eastern", "African"));
        graph.put("African",        Arrays.asList("Moroccan", "Caribbean"));
        graph.put("Thai",           Arrays.asList("Indian", "Vietnamese", "Chinese", "Asian"));
        graph.put("Vietnamese",     Arrays.asList("Thai", "Chinese", "Asian"));
        graph.put("Chinese",        Arrays.asList("Asian", "Japanese", "Korean", "Vietnamese"));
        graph.put("Japanese",       Arrays.asList("Chinese", "Korean", "Asian"));
        graph.put("Korean",         Arrays.asList("Japanese", "Chinese", "Asian"));
        graph.put("Asian",          Arrays.asList("Chinese", "Japanese", "Thai", "Vietnamese", "Korean"));
        graph.put("American",       Arrays.asList("Cajun", "BBQ", "Southern"));
        graph.put("Southern",       Arrays.asList("American", "Cajun"));
        graph.put("Cajun",          Arrays.asList("American", "Southern", "Caribbean"));
        graph.put("BBQ",            Arrays.asList("American", "Southern"));
        graph.put("European",       Arrays.asList("French", "Italian", "German", "Nordic"));
        graph.put("German",         Arrays.asList("European"));
        graph.put("Nordic",         Arrays.asList("European"));
        CUISINE_GRAPH = Collections.unmodifiableMap(graph);

        Map<String, List<String>> imap = new LinkedHashMap<>();
        imap.put("Garlic",       Arrays.asList("Italian", "Mediterranean", "Greek"));
        imap.put("Chicken",      Arrays.asList("Indian", "American", "Italian"));
        imap.put("Beef",         Arrays.asList("American", "Korean", "BBQ"));
        imap.put("Pork",         Arrays.asList("BBQ", "Chinese", "German"));
        imap.put("Fish",         Arrays.asList("Japanese", "Thai", "Mediterranean", "Nordic"));
        imap.put("Shrimp",       Arrays.asList("Cajun", "Thai", "Caribbean"));
        imap.put("Mushrooms",    Arrays.asList("Italian", "French", "Asian"));
        imap.put("Pasta",        Arrays.asList("Italian"));
        imap.put("Rice",         Arrays.asList("Japanese", "Chinese", "Thai", "Indian"));
        imap.put("Avocado",      Arrays.asList("Mexican", "American"));
        imap.put("Eggs",         Arrays.asList("French", "American"));
        imap.put("Cheese",       Arrays.asList("Italian", "French", "Mediterranean"));
        imap.put("Tomatoes",     Arrays.asList("Italian", "Mediterranean", "Mexican"));
        imap.put("Onion",        Arrays.asList("Indian", "French", "American"));
        imap.put("Lemon",        Arrays.asList("Mediterranean", "Greek", "Middle Eastern"));
        imap.put("Butter",       Arrays.asList("French", "European"));
        imap.put("Spicy food",   Arrays.asList("Thai", "Indian", "Mexican", "Korean", "Cajun"));
        imap.put("Bacon",        Arrays.asList("American", "BBQ", "Southern"));
        imap.put("Olive oil",    Arrays.asList("Mediterranean", "Italian", "Greek"));
        imap.put("Broccoli",     Arrays.asList("Chinese", "Asian", "American"));
        imap.put("Sweet potato", Arrays.asList("American", "African", "Southern"));
        imap.put("Beans",        Arrays.asList("Mexican", "Latin American", "African"));
        imap.put("Nuts",         Arrays.asList("Middle Eastern", "Moroccan", "Indian"));
        imap.put("Coconut",      Arrays.asList("Thai", "Indian", "Caribbean"));
        imap.put("Chocolate",    Arrays.asList("French", "Italian", "American"));
        INGREDIENT_CUISINE_MAP = Collections.unmodifiableMap(imap);
    }

    public static String getTopCuisine(List<TasteTag> profile) {
        if (profile == null || profile.isEmpty()) return null;
        // loadCuisineProfile returns list sorted by score desc
        return profile.get(0).getTag();
    }

    public static String getTopCuisineWithIngredients(List<TasteTag> cuisineProfile,
                                                       Map<String, Float> ingredientProfile) {
        // Seed scores from cuisine profile
        Map<String, Float> scores = new HashMap<>();
        for (TasteTag tag : cuisineProfile) {
            if (CUISINE_GRAPH.containsKey(tag.getTag())) {
                scores.put(tag.getTag(), tag.getScore());
            }
        }

        // Blend in ingredient boosts: (score - 4) / 6 * 2 maps [1,10] → [−1, +2], neutral=0
        for (Map.Entry<String, Float> entry : ingredientProfile.entrySet()) {
            List<String> cuisines = INGREDIENT_CUISINE_MAP.get(entry.getKey());
            if (cuisines == null) continue;
            float boost = (entry.getValue() - 4f) / 6f * 2f;
            for (String cuisine : cuisines) {
                scores.put(cuisine, scores.getOrDefault(cuisine, 0f) + boost);
            }
        }

        String best = null;
        float bestScore = Float.NEGATIVE_INFINITY;
        for (String cuisine : CUISINE_GRAPH.keySet()) {
            float s = scores.getOrDefault(cuisine, 0f);
            if (s > bestScore) {
                bestScore = s;
                best = cuisine;
            }
        }
        return best;
    }

    public static List<String> getSimilarCuisines(String cuisine) {
        List<String> neighbors = CUISINE_GRAPH.get(cuisine);
        return neighbors != null ? new ArrayList<>(neighbors) : new ArrayList<>();
    }

    public static List<String> getAdventurousCuisines(String cuisine) {
        List<String> oneHop = getSimilarCuisines(cuisine);
        Set<String> exclude = new HashSet<>(oneHop);
        exclude.add(cuisine);

        List<String> twoHop = new ArrayList<>();
        for (String neighbor : oneHop) {
            List<String> neighborNeighbors = CUISINE_GRAPH.get(neighbor);
            if (neighborNeighbors == null) continue;
            for (String nn : neighborNeighbors) {
                if (!exclude.contains(nn) && !twoHop.contains(nn)) {
                    twoHop.add(nn);
                }
            }
        }
        return twoHop;
    }

    public static String getNextPromptTopic(DatabaseHandler db, int userId) {
        List<String> candidates = new ArrayList<>(Arrays.asList(PROMPT_TOPICS));
        Collections.shuffle(candidates);
        long now = System.currentTimeMillis() / 1000;
        long threshold = 24 * 60 * 60;
        for (String topic : candidates) {
            long lastAsked = db.getLastPromptTime(userId, topic);
            if (lastAsked == -1 || (now - lastAsked) > threshold) {
                return topic;
            }
        }
        return null;
    }
}
