package com.gathin.tvhome;

import android.service.dreams.DreamService;

/** System screensaver uses the same dim gallery as Home's idle fallback. */
public class ClockDream extends DreamService {
    private GalleryDreamView gallery;
    @Override public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        setScreenBright(false);
        gallery = new GalleryDreamView(this);
        setContentView(gallery);
    }
    @Override public void onDreamingStarted() { super.onDreamingStarted(); gallery.start(); }
    @Override public void onDreamingStopped() { if (gallery != null) gallery.stop(); super.onDreamingStopped(); }
    @Override public void onDetachedFromWindow() { if (gallery != null) gallery.stop(); super.onDetachedFromWindow(); }
}
