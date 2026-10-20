package com.gathin.tvhome;

import java.util.ArrayList;
import java.util.List;

/** Pure policies shared by the remote editor, backup reader, and wallpaper timer. */
final class GlowRules {
    static final long ROTATION_MS = 10 * 60 * 1000L;
    static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    static int wallpaperIndex(long elapsed, int count) {
        return count <= 0 ? 0 : (int) (Math.max(0, elapsed) / ROTATION_MS % count);
    }
    static List<String> move(List<String> source, int from, int to) {
        List<String> result = new ArrayList<>(source);
        if (from < 0 || from >= result.size() || to < 0 || to >= result.size()) return result;
        result.add(to, result.remove(from));
        return result;
    }
}
