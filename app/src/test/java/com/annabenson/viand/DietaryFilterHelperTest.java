package com.annabenson.viand;

import com.annabenson.viand.utils.DietaryFilterHelper;

import org.junit.Test;

import static org.junit.Assert.*;

public class DietaryFilterHelperTest {

    // ── No preferences ────────────────────────────────────────────────────────

    @Test
    public void noPrefs_nullString_returnsBothNull() {
        String[] result = DietaryFilterHelper.deriveDietaryFilters(null);
        assertNull(result[0]);
        assertNull(result[1]);
    }

    @Test
    public void noPrefs_emptyString_returnsBothNull() {
        String[] result = DietaryFilterHelper.deriveDietaryFilters("");
        assertNull(result[0]);
        assertNull(result[1]);
    }

    @Test
    public void unsupportedPrefsOnly_kosherHalal_returnsBothNull() {
        String[] result = DietaryFilterHelper.deriveDietaryFilters("Kosher,Halal");
        assertNull(result[0]);
        assertNull(result[1]);
    }

    // ── Diet param ────────────────────────────────────────────────────────────

    @Test
    public void vegan_setsDietVegan() {
        String[] result = DietaryFilterHelper.deriveDietaryFilters("Vegan");
        assertEquals("vegan", result[0]);
        assertNull(result[1]);
    }

    @Test
    public void vegetarian_setsDietVegetarian() {
        String[] result = DietaryFilterHelper.deriveDietaryFilters("Vegetarian");
        assertEquals("vegetarian", result[0]);
        assertNull(result[1]);
    }

    @Test
    public void veganAndVegetarian_veganWins() {
        String[] result = DietaryFilterHelper.deriveDietaryFilters("Vegan,Vegetarian");
        assertEquals("vegan", result[0]);
    }

    // ── Intolerances param ────────────────────────────────────────────────────

    @Test
    public void glutenFree_setsGlutenIntolerance() {
        String[] result = DietaryFilterHelper.deriveDietaryFilters("Gluten Free");
        assertNull(result[0]);
        assertEquals("gluten", result[1]);
    }

    @Test
    public void dairyFree_setsDairyIntolerance() {
        String[] result = DietaryFilterHelper.deriveDietaryFilters("Dairy Free");
        assertNull(result[0]);
        assertEquals("dairy", result[1]);
    }

    @Test
    public void nutFree_setsPeanutAndTreeNutIntolerances() {
        String[] result = DietaryFilterHelper.deriveDietaryFilters("Nut Free");
        assertNull(result[0]);
        assertNotNull(result[1]);
        assertTrue(result[1].contains("peanut"));
        assertTrue(result[1].contains("tree nut"));
    }

    @Test
    public void dairyFreeAndNutFree_combinedIntolerances() {
        String[] result = DietaryFilterHelper.deriveDietaryFilters("Dairy Free,Nut Free");
        assertNotNull(result[1]);
        assertTrue(result[1].contains("dairy"));
        assertTrue(result[1].contains("peanut"));
        assertTrue(result[1].contains("tree nut"));
    }

    // ── Combined diet + intolerances ─────────────────────────────────────────

    @Test
    public void vegan_withGlutenAndNutFree_setsBoth() {
        String[] result = DietaryFilterHelper.deriveDietaryFilters("Vegan,Gluten Free,Nut Free");
        assertEquals("vegan", result[0]);
        assertNotNull(result[1]);
        assertTrue(result[1].contains("gluten"));
        assertTrue(result[1].contains("peanut"));
        assertTrue(result[1].contains("tree nut"));
    }

    @Test
    public void allPrefs_veganDominatesDiet() {
        String[] result = DietaryFilterHelper.deriveDietaryFilters(
                "Gluten Free,Vegetarian,Vegan,Kosher,Halal,Dairy Free,Nut Free");
        assertEquals("vegan", result[0]);
        assertNotNull(result[1]);
    }
}
