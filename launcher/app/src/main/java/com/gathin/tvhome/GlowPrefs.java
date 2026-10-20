package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

final class GlowPrefs {
    static SharedPreferences get(Context c) { return c.getSharedPreferences("glow", Context.MODE_PRIVATE); }
    static int number(Context c, String key, int fallback, int min, int max) {
        return GlowRules.clamp(get(c).getInt(key, fallback), min, max);
    }
    static List<String> favorites(Context c) {
        String saved = get(c).getString("favorites", null);
        if (saved == null) return new ArrayList<>(Config.load(c).dockApps);
        return parseFavorites(saved);
    }
    static List<String> parseFavorites(String json) {
        List<String> out = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json);
            if (array.length() > 7) throw new IllegalArgumentException("Too many favorites");
            for (int i = 0; i < array.length() && out.size() < 7; i++) {
                String pkg = array.getString(i);
                if (!pkg.matches("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+") || out.contains(pkg))
                    throw new IllegalArgumentException("Invalid favorites");
                out.add(pkg);
            }
        } catch (Exception e) { throw new IllegalArgumentException("Invalid favorites", e); }
        if (out.isEmpty()) throw new IllegalArgumentException("Keep at least one favorite");
        return out;
    }
    static JSONObject backup(Context c) throws Exception {
        JSONObject data = new JSONObject();
        data.put("version", 1);
        data.put("favorites", new JSONArray(favorites(c)));
        SharedPreferences p = get(c);
        data.put("icon_percent", p.getInt("icon_percent", 100));
        data.put("clock_sp", p.getInt("clock_sp", 42));
        data.put("wall_offset", p.getInt("wall_offset", -10));
        data.put("dim_percent", p.getInt("dim_percent", 0));
        data.put("clean_home", p.getBoolean("clean_home", true));
        data.put("rotate", p.getBoolean("rotate", false));
        data.put("wallpaper", p.getString("wallpaper", "porsche"));
        data.put("idle_minutes", p.getInt("idle_minutes", 5));
        return data;
    }
    static void restore(Context c, String text) throws Exception {
        JSONObject data = new JSONObject(text);
        if (data.getInt("version") != 1) throw new IllegalArgumentException("Unsupported backup version");
        List<String> apps = parseFavorites(data.getJSONArray("favorites").toString());
        String wallpaper = data.getString("wallpaper");
        if (!wallpaper.equals("porsche") && !wallpaper.equals("scenic")
                && !wallpaper.matches("photo_[a-zA-Z0-9_-]+\\.jpg"))
            throw new IllegalArgumentException("Invalid wallpaper");
        // Validate everything before changing any setting; malformed backups never partially apply.
        int size = bounded(data, "icon_percent", 70, 110);
        int clock = bounded(data, "clock_sp", 30, 54);
        int offset = bounded(data, "wall_offset", -20, 10);
        int dim = bounded(data, "dim_percent", 0, 60);
        boolean clean = data.getBoolean("clean_home"), rotate = data.getBoolean("rotate");
        int idle = data.optInt("idle_minutes", 5);
        if (idle < 0 || idle > 30) throw new IllegalArgumentException("Invalid screensaver delay");
        get(c).edit().putString("favorites", new JSONArray(apps).toString())
                .putInt("icon_percent", size).putInt("clock_sp", clock)
                .putInt("idle_minutes", idle)
                .putInt("wall_offset", offset).putInt("dim_percent", dim)
                .putBoolean("clean_home", clean).putBoolean("rotate", rotate)
                .putString("wallpaper", wallpaper).putLong("rotation_start", android.os.SystemClock.elapsedRealtime())
                .commit();
    }
    private static int bounded(JSONObject data, String key, int min, int max) throws Exception {
        int value = data.getInt(key);
        if (value < min || value > max) throw new IllegalArgumentException("Invalid " + key);
        return value;
    }
}
