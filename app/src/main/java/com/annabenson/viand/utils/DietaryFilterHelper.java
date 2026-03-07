package com.annabenson.viand.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DietaryFilterHelper {

    /**
     * Maps a comma-separated dietary preference string (as stored in UserAccountsTable)
     * to Spoonacular query parameters.
     *
     * @return String[2] where [0] = diet param (nullable), [1] = intolerances param (nullable)
     */
    public static String[] deriveDietaryFilters(String prefsStr) {
        if (prefsStr == null || prefsStr.isEmpty()) return new String[]{null, null};
        Set<String> prefs = new HashSet<>(Arrays.asList(prefsStr.split(",")));

        // Spoonacular accepts one diet value; pick the strictest applicable
        String diet = null;
        if (prefs.contains("Vegan"))       diet = "vegan";
        else if (prefs.contains("Vegetarian")) diet = "vegetarian";

        // Spoonacular intolerances are comma-separated
        List<String> intolerances = new ArrayList<>();
        if (prefs.contains("Gluten Free")) intolerances.add("gluten");
        if (prefs.contains("Dairy Free"))  intolerances.add("dairy");
        if (prefs.contains("Nut Free"))    { intolerances.add("peanut"); intolerances.add("tree nut"); }

        return new String[]{diet, intolerances.isEmpty() ? null : String.join(",", intolerances)};
    }
}
