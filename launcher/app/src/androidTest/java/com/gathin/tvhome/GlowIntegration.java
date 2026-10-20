package com.gathin.tvhome;

import android.app.Instrumentation;
import android.content.*;
import android.os.*;
import org.json.*;
import java.util.*;

/** On-device checks against real Android JSON, preferences, and gallery behavior. Restores state. */
public class GlowIntegration extends Instrumentation {
    @Override public void onCreate(Bundle args) { super.onCreate(args); start(); }
    @Override public void onStart() {
        Context c = getTargetContext();
        SharedPreferences prefs = GlowPrefs.get(c);
        Map<String, ?> original = new HashMap<>(prefs.getAll());
        Bundle result = new Bundle();
        int code = -1;
        try {
            JSONObject initial = GlowPrefs.backup(c);
            GlowPrefs.restore(c, initial.toString());
            require(initial.toString().equals(GlowPrefs.backup(c).toString()), "Backup round trip");
            JSONObject bad = new JSONObject(initial.toString()).put("dim_percent", 999);
            try { GlowPrefs.restore(c, bad.toString()); throw new AssertionError("Malformed backup accepted"); }
            catch (IllegalArgumentException expected) { }
            require(initial.toString().equals(GlowPrefs.backup(c).toString()), "Malformed restore changed state");
            try { GlowPrefs.parseFavorites("[\"com.test.app\",\"com.test.app\"]"); throw new AssertionError("Duplicates accepted"); }
            catch (IllegalArgumentException expected) { }
            prefs.edit().putString("wallpaper","porsche").putBoolean("rotate",true)
                    .putLong("rotation_start",SystemClock.elapsedRealtime()-600000L).commit();
            require(WallpaperGallery.current(c).equals("scenic"), "Rotation boundary");
            prefs.edit().putBoolean("rotate",false).commit();
            require(WallpaperGallery.current(c).equals("porsche"), "Pinned wallpaper");
            prefs.edit().putString("favorites","[\"com.test.one\",\"com.test.two\"]").commit();
            Config cfg = Config.load(c);
            require(cfg.columns == 2 && cfg.dockApps.size() == 2, "Favorite count controls dock");
            result.putString("stream", "PASS: backup round trip, invalid backup atomicity, duplicate rejection, rotation, pinning, dock count\n");
        } catch (Throwable error) {
            code = 0; result.putString("stream", "FAIL: " + error.toString());
        } finally {
            SharedPreferences.Editor edit = prefs.edit().clear();
            for (Map.Entry<String, ?> entry : original.entrySet()) {
                Object value = entry.getValue(); String key = entry.getKey();
                if (value instanceof String) edit.putString(key,(String)value);
                else if (value instanceof Boolean) edit.putBoolean(key,(Boolean)value);
                else if (value instanceof Integer) edit.putInt(key,(Integer)value);
                else if (value instanceof Long) edit.putLong(key,(Long)value);
                else if (value instanceof Float) edit.putFloat(key,(Float)value);
            }
            edit.commit();
        }
        finish(code,result);
    }
    private void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
