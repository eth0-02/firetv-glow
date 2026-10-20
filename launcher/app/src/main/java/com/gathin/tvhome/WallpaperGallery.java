package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import java.io.File;
import java.util.*;

final class WallpaperGallery {
    static File folder(Context c) {
        File folder = new File(c.getFilesDir(), "wallpapers");
        folder.mkdirs();
        return folder;
    }
    static List<String> entries(Context c) {
        List<String> list = new ArrayList<>(Arrays.asList("porsche", "scenic"));
        File[] files = folder(c).listFiles((dir, name) -> name.matches("photo_[a-zA-Z0-9_-]+\\.jpg"));
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName));
            for (File file : files) list.add(file.getName());
        }
        return list;
    }
    static String current(Context c) {
        android.content.SharedPreferences p = GlowPrefs.get(c);
        List<String> entries = entries(c);
        String selected = p.getString("wallpaper", "porsche");
        int base = Math.max(0, entries.indexOf(selected));
        if (!p.getBoolean("rotate", false)) return entries.get(base);
        long now = android.os.SystemClock.elapsedRealtime();
        long start = p.getLong("rotation_start", now);
        if (start > now || !p.contains("rotation_start")) {
            p.edit().putLong("rotation_start", now).apply();
            start = now;
        }
        return entries.get((base + GlowRules.wallpaperIndex(now - start, entries.size())) % entries.size());
    }
    static String label(String id) {
        return id.equals("porsche") ? "Porsche" : id.equals("scenic") ? "Mountain lake" : "Photo " + id.substring(6, id.length() - 4);
    }
    static void show(Context c, ImageView view, String id) {
        if (id.equals("porsche") || id.equals("scenic")) {
            view.setImageResource(id.equals("porsche") ? R.drawable.porsche_wallpaper : R.drawable.scenic_wallpaper);
            return;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        File file = new File(folder(c), id);
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), options);
        options.inSampleSize = 1;
        while (options.outWidth / options.inSampleSize > 1920 || options.outHeight / options.inSampleSize > 1200)
            options.inSampleSize *= 2;
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), options);
        if (bitmap == null) view.setImageResource(R.drawable.porsche_wallpaper);
        else view.setImageBitmap(bitmap);
    }
}
