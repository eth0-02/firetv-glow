package com.gathin.tvhome;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class GlowRulesTest {
    @Test public void rotationChangesOnlyAtTenMinuteBoundary() {
        assertEquals(0, GlowRules.wallpaperIndex(599999, 3));
        assertEquals(1, GlowRules.wallpaperIndex(600000, 3));
        assertEquals(2, GlowRules.wallpaperIndex(1200000, 3));
        assertEquals(0, GlowRules.wallpaperIndex(1800000, 3));
    }
    @Test public void rotationHandlesRebootAndEmptyGallery() {
        assertEquals(0, GlowRules.wallpaperIndex(-100, 2));
        assertEquals(0, GlowRules.wallpaperIndex(10000000, 0));
    }
    @Test public void reorderingPreservesOtherFavoritesAndInput() {
        List<String> before = Arrays.asList("a","b","c");
        assertEquals(Arrays.asList("b","c","a"), GlowRules.move(before,0,2));
        assertEquals(Arrays.asList("a","b","c"), before);
        assertEquals(before, GlowRules.move(before,0,-1));
        assertEquals(before, GlowRules.move(before,2,3));
    }
    @Test public void appearanceBoundsPreventExtremeValues() {
        assertEquals(70, GlowRules.clamp(-10,70,110));
        assertEquals(110, GlowRules.clamp(999,70,110));
        assertEquals(85, GlowRules.clamp(85,70,110));
    }
}
