package com.gathin.tvhome;

import android.app.*;
import android.content.*;
import android.content.pm.ResolveInfo;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import org.json.JSONArray;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/** Primary customization actions work with a directional remote and OK/Back. */
public final class LauncherSettingsActivity extends Activity {
    private LinearLayout rows;
    private String page = "Home settings";
    private boolean firstRow;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private static final int PHOTO = 71, EXPORT = 72, IMPORT = 73;
    private final Map<String, String> labels = new LinkedHashMap<>();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        List<ResolveInfo> found = getPackageManager().queryIntentActivities(
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER), 0);
        found.sort(Comparator.comparing(x -> x.loadLabel(getPackageManager()).toString().toLowerCase()));
        for (ResolveInfo info : found) {
            String pkg = info.activityInfo.packageName;
            if (!pkg.equals(getPackageName()) && !Config.load(this).hiddenApps.contains(pkg))
                labels.put(pkg, Cards.cleanLabel(info.loadLabel(getPackageManager()).toString(), pkg));
        }
        home();
        if (getIntent().getBooleanExtra("network", false)) NetworkCheck.show(this);
    }
    private void screen(String title) {
        page = title;
        firstRow = true;
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xff101419);
        rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        rows.setPadding(dp(65), dp(30), dp(65), dp(35));
        scroll.addView(rows);
        TextView heading = new TextView(this);
        heading.setText(title); heading.setTextColor(-1); heading.setTextSize(30);
        heading.setPadding(0, 0, 0, dp(16)); rows.addView(heading);
        setContentView(scroll);
    }
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }
    private void note(String text) {
        TextView label = new TextView(this); label.setText(text); label.setTextSize(15);
        label.setTextColor(0xffb9c1ca); label.setPadding(0, dp(6), 0, dp(12)); rows.addView(label);
    }
    private void row(String label, Runnable action) {
        TextView button = new TextView(this); button.setText(label); button.setTextColor(-1);
        button.setTextSize(18); button.setGravity(Gravity.CENTER_VERTICAL);
        button.setPadding(dp(18), 0, dp(18), 0);
        button.setBackgroundResource(R.drawable.settings_choice_bg);
        button.setFocusable(true); button.setClickable(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(51)); lp.bottomMargin = dp(7);
        rows.addView(button, lp); button.setOnClickListener(v -> action.run());
        if (firstRow) { firstRow = false; button.post(button::requestFocus); }
    }
    private void changed() { setResult(RESULT_OK); }
    private void home() {
        screen("Home settings");
        row("Favorites  ·  add, remove and reorder", this::favorites);
        row("Wallpaper gallery", this::gallery);
        row("Appearance", this::appearance);
        row("Personal screensaver", this::screensaver);
        row("Connection test", () -> NetworkCheck.show(this));
        row("Backup and restore", this::backups);
        row("Fire TV settings", () -> {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            intent.setPackage("com.amazon.tv.settings.v2");
            if (intent.resolveActivity(getPackageManager()) == null) intent.setPackage(null);
            launch(intent, 0);
        });
        row("Return home", this::finish);
    }
    private void favorites() {
        screen("Favorites");
        List<String> apps = GlowPrefs.favorites(this);
        note("Select a favorite to move or remove it. Keep between 1 and 7 favorites.");
        for (int i = 0; i < apps.size(); i++) {
            final int index = i;
            row((i + 1) + "   " + labels.getOrDefault(apps.get(i), apps.get(i)), () ->
                new AlertDialog.Builder(this).setTitle(labels.getOrDefault(apps.get(index), apps.get(index)))
                    .setItems(new String[]{"Move left", "Move right", "Remove"}, (d, choice) -> {
                        List<String> next = new ArrayList<>(apps);
                        if (choice == 2) {
                            if (next.size() == 1) { toast("Keep at least one favorite"); return; }
                            next.remove(index);
                        } else next = GlowRules.move(apps, index, index + (choice == 0 ? -1 : 1));
                        saveFavorites(next); favorites();
                    }).show());
        }
        if (apps.size() < 7) row("+ Add favorite", () -> {
            List<String> available = new ArrayList<>(labels.keySet()); available.removeAll(apps);
            if (available.isEmpty()) { toast("No more TV apps found"); return; }
            String[] names = new String[available.size()];
            for (int i = 0; i < names.length; i++) names[i] = labels.get(available.get(i));
            new AlertDialog.Builder(this).setTitle("Add favorite").setItems(names, (d, which) -> {
                apps.add(available.get(which)); saveFavorites(apps); favorites();
            }).show();
        });
        row("Back", this::home);
    }
    private void screensaver() {
        screen("Personal screensaver");
        int delay = GlowPrefs.number(this,"idle_minutes",5,0,30);
        note("Dim gallery pictures and a drifting clock. Any remote key returns you home. The Home timer pauses while another app is open.");
        row("Start after  ·  " + (delay == 0 ? "Off" : delay + " minutes on Home"), () -> {
            String[] labels = {"Off", "3 minutes", "5 minutes", "10 minutes", "20 minutes"};
            int[] values = {0,3,5,10,20};
            new AlertDialog.Builder(this).setTitle("Screensaver delay").setItems(labels,(d,which) -> {
                GlowPrefs.get(this).edit().putInt("idle_minutes",values[which]).apply(); changed(); screensaver();
            }).show();
        });
        row("Preview screensaver", () -> startActivity(new Intent(this,GalleryScreensaverActivity.class)));
        row("Back",this::home);
    }
    private void saveFavorites(List<String> apps) {
        GlowPrefs.get(this).edit().putString("favorites", new JSONArray(apps).toString()).apply(); changed();
    }
    private void appearance() {
        screen("Appearance");
        row("Icon size  ·  " + GlowPrefs.number(this,"icon_percent",100,70,110) + "%",
                () -> pickNumber("Icon size", "icon_percent", new int[]{70,85,100,110}, "%"));
        row("Clock size  ·  " + GlowPrefs.number(this,"clock_sp",42,30,54),
                () -> pickNumber("Clock size", "clock_sp", new int[]{30,36,42,48,54}, ""));
        row("Wallpaper position  ·  " + GlowPrefs.number(this,"wall_offset",-10,-20,10) + "%",
                () -> pickNumber("Vertical position (negative moves up)", "wall_offset", new int[]{-20,-15,-10,-5,0,5,10}, "%"));
        row("Wallpaper dimming  ·  " + GlowPrefs.number(this,"dim_percent",0,0,60) + "%",
                () -> pickNumber("Dimming", "dim_percent", new int[]{0,15,30,45,60}, "%"));
        boolean clean = GlowPrefs.get(this).getBoolean("clean_home", true);
        row("Hide lower row until Down  ·  " + (clean ? "On" : "Off"), () -> {
            GlowPrefs.get(this).edit().putBoolean("clean_home", !clean).apply(); changed(); appearance();
        });
        row("Back", this::home);
    }
    private void pickNumber(String title, String key, int[] values, String suffix) {
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) names[i] = values[i] + suffix;
        new AlertDialog.Builder(this).setTitle(title).setItems(names, (d, which) -> {
            GlowPrefs.get(this).edit().putInt(key, values[which]).apply(); changed(); appearance();
        }).show();
    }
    private void gallery() {
        screen("Wallpaper gallery");
        boolean rotate = GlowPrefs.get(this).getBoolean("rotate", false);
        row("Rotate every 10 minutes  ·  " + (rotate ? "On" : "Off"), () -> {
            GlowPrefs.get(this).edit().putBoolean("rotate", !rotate)
                    .putLong("rotation_start", SystemClock.elapsedRealtime()).apply(); changed(); gallery();
        });
        row("Keep current wallpaper", () -> {
            String current = WallpaperGallery.current(this);
            GlowPrefs.get(this).edit().putString("wallpaper", current).putBoolean("rotate", false).apply();
            changed(); gallery(); toast("Wallpaper pinned");
        });
        row("+ Import picture", () -> launch(new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .setType("image/*").addCategory(Intent.CATEGORY_OPENABLE), PHOTO));
        note("Choose a picture to preview it. Imported pictures stay on this TV. Rotation uses all gallery pictures.");
        for (String id : WallpaperGallery.entries(this)) row(WallpaperGallery.label(id), () -> preview(id));
        row("Back", this::home);
    }
    private void preview(String id) {
        ImageView picture = new ImageView(this);
        picture.setAdjustViewBounds(true); picture.setMaxHeight(dp(290)); picture.setScaleType(ImageView.ScaleType.FIT_CENTER);
        WallpaperGallery.show(this, picture, id);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this).setTitle(WallpaperGallery.label(id)).setView(picture)
                .setPositiveButton("Use picture", (d, w) -> {
                    GlowPrefs.get(this).edit().putString("wallpaper", id)
                            .putLong("rotation_start", SystemClock.elapsedRealtime()).apply();
                    changed(); toast("Wallpaper selected");
                }).setNegativeButton("Close", null);
        if (id.startsWith("photo_")) dialog.setNeutralButton("Remove", (d,w) -> {
            if (!new File(WallpaperGallery.folder(this), id).delete()) { toast("Could not remove picture"); return; }
            changed(); gallery();
        });
        dialog.show();
    }
    private File backupFile() { return new File(getFilesDir(), "glow-backup.json"); }
    private void backups() {
        screen("Backup and restore");
        note("Saves favorites and appearance. Imported picture files remain on this TV and are not included in exported layout backups.");
        row("Save layout on this TV", () -> {
            try { write(new FileOutputStream(backupFile()), GlowPrefs.backup(this).toString(2)); toast("Layout saved"); }
            catch (Exception e) { toast("Could not save backup"); }
        });
        row("Restore saved layout", () -> new AlertDialog.Builder(this).setTitle("Restore saved layout?")
                .setMessage("Replace your current favorites and appearance with the saved layout.")
                .setPositiveButton("Restore", (d,w) -> {
                    try { GlowPrefs.restore(this, read(new FileInputStream(backupFile()))); changed(); toast("Layout restored"); }
                    catch (Exception e) { toast("No valid saved backup found"); }
                }).setNegativeButton("Cancel", null).show());
        row("Export layout backup", () -> launch(new Intent(Intent.ACTION_CREATE_DOCUMENT)
                .setType("application/json").addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_TITLE, "firetv-glow-layout.json"), EXPORT));
        row("Import layout backup", () -> launch(new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .setType("*/*").addCategory(Intent.CATEGORY_OPENABLE), IMPORT));
        row("Back", this::home);
    }
    private void launch(Intent intent, int code) {
        try { if (code == 0) startActivity(intent); else startActivityForResult(intent, code); }
        catch (ActivityNotFoundException | SecurityException e) {
            new AlertDialog.Builder(this).setTitle("Not available")
                    .setMessage("This Fire TV does not provide a compatible app for this action. Built-in wallpapers and on-TV backups still work. Photos can also be added with the companion ADB import script.")
                    .setPositiveButton("OK", null).show();
        }
    }
    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (result != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        if (request == IMPORT) {
            try {
                String text = read(getContentResolver().openInputStream(uri));
                new AlertDialog.Builder(this).setTitle("Import this layout?")
                        .setMessage("This replaces your current favorites and appearance.")
                        .setPositiveButton("Restore", (d,w) -> {
                            try { GlowPrefs.restore(this, text); changed(); toast("Layout restored"); }
                            catch (Exception e) { toast("Invalid layout backup; settings unchanged"); }
                        }).setNegativeButton("Cancel", null).show();
            } catch (Exception e) { toast("Could not read backup"); }
        } else if (request == EXPORT) {
            try { write(getContentResolver().openOutputStream(uri), GlowPrefs.backup(this).toString(2)); toast("Backup exported"); }
            catch (Exception e) { toast("Could not export backup"); }
        } else if (request == PHOTO) {
            toast("Importing picture…");
            worker.execute(() -> {
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options(); options.inJustDecodeBounds = true;
                    try (InputStream stream = getContentResolver().openInputStream(uri)) { BitmapFactory.decodeStream(stream,null,options); }
                    if (options.outWidth <= 0 || options.outHeight <= 0) throw new IOException();
                    options.inSampleSize = 1;
                    while (options.outWidth / options.inSampleSize > 1920 || options.outHeight / options.inSampleSize > 1200)
                        options.inSampleSize *= 2;
                    options.inJustDecodeBounds = false;
                    Bitmap bitmap;
                    try (InputStream stream = getContentResolver().openInputStream(uri)) { bitmap = BitmapFactory.decodeStream(stream,null,options); }
                    if (bitmap == null) throw new IOException();
                    File file = new File(WallpaperGallery.folder(this), "photo_" + System.currentTimeMillis() + ".jpg");
                    try (OutputStream out = new FileOutputStream(file)) {
                        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)) throw new IOException();
                    } finally { bitmap.recycle(); }
                    runOnUiThread(() -> { if (!isFinishing() && !isDestroyed()) { changed(); gallery(); toast("Picture imported"); } });
                } catch (Exception e) { runOnUiThread(() -> toast("Could not import picture")); }
            });
        }
    }
    private static String read(InputStream stream) throws Exception {
        try (InputStream in = stream; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096]; int count;
            while ((count = in.read(buffer)) != -1) {
                if (out.size() + count > 65536) throw new IOException("Backup too large");
                out.write(buffer, 0, count);
            }
            return out.toString("UTF-8");
        }
    }
    private static void write(OutputStream stream, String text) throws Exception {
        try (OutputStream out = stream) { out.write(text.getBytes(StandardCharsets.UTF_8)); }
    }
    private void toast(String text) { if (!isDestroyed()) Toast.makeText(this, text, Toast.LENGTH_LONG).show(); }
    @Override public void onBackPressed() { if (page.equals("Home settings")) super.onBackPressed(); else home(); }
    @Override public void onDestroy() { worker.shutdownNow(); super.onDestroy(); }
}
