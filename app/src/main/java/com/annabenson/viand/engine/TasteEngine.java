package com.annabenson.viand.engine;

import com.annabenson.viand.data.DatabaseHandler;
import com.annabenson.viand.models.TasteTag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TasteEngine {

    public static final Map<String, List<String>> CUISINE_GRAPH;

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
    }

    public static String getTopCuisine(List<TasteTag> profile) {
        if (profile == null || profile.isEmpty()) return null;
        // loadCuisineProfile returns list sorted by score desc
        return profile.get(0).getTag();
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
