package com.annabenson.viand;

import com.annabenson.viand.engine.TasteEngine;
import com.annabenson.viand.models.TasteTag;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TasteEngineTest {

    // ── getTopCuisine ─────────────────────────────────────────────────────────

    @Test
    public void getTopCuisine_nullProfile_returnsNull() {
        assertNull(TasteEngine.getTopCuisine(null));
    }

    @Test
    public void getTopCuisine_emptyProfile_returnsNull() {
        assertNull(TasteEngine.getTopCuisine(Arrays.asList()));
    }

    @Test
    public void getTopCuisine_singleEntry_returnsIt() {
        List<TasteTag> profile = Arrays.asList(new TasteTag("Italian", "cuisine", 8f));
        assertEquals("Italian", TasteEngine.getTopCuisine(profile));
    }

    @Test
    public void getTopCuisine_multipleEntries_returnsFirst() {
        // loadCuisineProfile returns list sorted by score desc; top cuisine is index 0
        List<TasteTag> profile = Arrays.asList(
                new TasteTag("Japanese", "cuisine", 9f),
                new TasteTag("Italian",  "cuisine", 7f),
                new TasteTag("Mexican",  "cuisine", 3f)
        );
        assertEquals("Japanese", TasteEngine.getTopCuisine(profile));
    }

    // ── getSimilarCuisines ────────────────────────────────────────────────────

    @Test
    public void getSimilarCuisines_knownCuisine_returnsNeighbors() {
        List<String> similar = TasteEngine.getSimilarCuisines("Italian");
        assertTrue(similar.contains("Mediterranean"));
        assertTrue(similar.contains("French"));
        assertTrue(similar.contains("Greek"));
    }

    @Test
    public void getSimilarCuisines_unknownCuisine_returnsEmptyList() {
        List<String> similar = TasteEngine.getSimilarCuisines("Martian");
        assertNotNull(similar);
        assertTrue(similar.isEmpty());
    }

    @Test
    public void getSimilarCuisines_doesNotReturnSelf() {
        for (String cuisine : TasteEngine.CUISINE_GRAPH.keySet()) {
            assertFalse("getSimilarCuisines(\"" + cuisine + "\") should not contain itself",
                    TasteEngine.getSimilarCuisines(cuisine).contains(cuisine));
        }
    }

    @Test
    public void getSimilarCuisines_returnsNewListEachCall() {
        List<String> a = TasteEngine.getSimilarCuisines("Italian");
        List<String> b = TasteEngine.getSimilarCuisines("Italian");
        assertNotSame(a, b); // mutation of one must not affect the other
    }

    // ── getAdventurousCuisines ────────────────────────────────────────────────

    @Test
    public void getAdventurousCuisines_doesNotContainOrigin() {
        List<String> adventurous = TasteEngine.getAdventurousCuisines("Italian");
        assertFalse(adventurous.contains("Italian"));
    }

    @Test
    public void getAdventurousCuisines_doesNotContainDirectNeighbors() {
        List<String> similar = TasteEngine.getSimilarCuisines("Italian");
        List<String> adventurous = TasteEngine.getAdventurousCuisines("Italian");
        for (String neighbor : similar) {
            assertFalse("Adventurous should not include direct neighbor: " + neighbor,
                    adventurous.contains(neighbor));
        }
    }

    @Test
    public void getAdventurousCuisines_containsTwoHopNodes() {
        // Italian → Mediterranean → Middle Eastern (two hops from Italian)
        List<String> adventurous = TasteEngine.getAdventurousCuisines("Italian");
        assertTrue(adventurous.contains("Middle Eastern"));
    }

    @Test
    public void getAdventurousCuisines_unknownCuisine_returnsEmptyList() {
        List<String> adventurous = TasteEngine.getAdventurousCuisines("Martian");
        assertNotNull(adventurous);
        assertTrue(adventurous.isEmpty());
    }

    @Test
    public void getAdventurousCuisines_noDuplicates() {
        List<String> adventurous = TasteEngine.getAdventurousCuisines("Asian");
        long distinct = adventurous.stream().distinct().count();
        assertEquals("Adventurous list should have no duplicates", distinct, adventurous.size());
    }
}
