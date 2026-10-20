package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Color;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.List;

/** Shared dim gallery and drifting clock for the system dream and Home's idle fallback. */
final class GalleryDreamView extends FrameLayout {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ImageView photo;
    private final LinearLayout clock;
    private long start;
    private String shown = "";
    private boolean running;
    private int corner;
    GalleryDreamView(Context context) {
        super(context); setBackgroundColor(Color.BLACK);
        photo = new ImageView(context); photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(photo,new LayoutParams(-1,-1));
        View dim = new View(context); dim.setBackgroundColor(0xA6000000);
        addView(dim,new LayoutParams(-1,-1));
        clock = new LinearLayout(context); clock.setOrientation(LinearLayout.VERTICAL);
        TextClock time = new TextClock(context); time.setFormat12Hour("h:mm a"); time.setFormat24Hour("HH:mm");
        time.setTextColor(0xBBFFFFFF); time.setTextSize(48); time.setFontFeatureSettings("tnum");
        clock.addView(time);
        TextClock date = new TextClock(context); date.setFormat12Hour("EEEE, d MMMM"); date.setFormat24Hour("EEEE, d MMMM");
        date.setTextSize(16); date.setTextColor(0x88FFFFFF); clock.addView(date);
        addView(clock,new LayoutParams(-2,-2));
    }
    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!running) return;
            List<String> gallery = WallpaperGallery.entries(getContext());
            String selected = GlowPrefs.get(getContext()).getString("wallpaper","porsche");
            int base = Math.max(0,gallery.indexOf(selected));
            String next = gallery.get((base + GlowRules.wallpaperIndex(SystemClock.elapsedRealtime()-start,gallery.size())) % gallery.size());
            if (!next.equals(shown)) { WallpaperGallery.fade(getContext(),photo,next,!shown.isEmpty()); shown=next; }
            clock.animate().alpha(0f).setDuration(500).withEndAction(() -> {
                if (!running) return;
                int margin=Math.round(35*getResources().getDisplayMetrics().density);
                int maxX=Math.max(margin,getWidth()-clock.getWidth()-margin);
                int maxY=Math.max(margin,getHeight()-clock.getHeight()-margin);
                clock.setX((corner%2==0)?margin:maxX);
                clock.setY((corner<2)?margin:maxY);
                corner=(corner+1)%4;
                clock.animate().alpha(0.8f).setDuration(800).start();
            }).start();
            handler.postDelayed(this,60000L);
        }
    };
    void start() { if (running) return; running=true; start=SystemClock.elapsedRealtime(); post(tick); }
    void stop() { running=false; handler.removeCallbacks(tick); removeCallbacks(tick); clock.animate().cancel(); }
}
