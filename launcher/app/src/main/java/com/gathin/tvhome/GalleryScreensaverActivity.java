package com.gathin.tvhome;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

public final class GalleryScreensaverActivity extends Activity {
    private GalleryDreamView gallery;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        gallery=new GalleryDreamView(this); setContentView(gallery);
    }
    @Override public void onResume() { super.onResume(); gallery.start(); }
    @Override public void onPause() { gallery.stop(); super.onPause(); }
    @Override public boolean dispatchKeyEvent(KeyEvent event) { if(event.getAction()==KeyEvent.ACTION_DOWN) finish(); return true; }
    @Override public boolean onTouchEvent(MotionEvent event) { if(event.getAction()==MotionEvent.ACTION_DOWN) finish(); return true; }
}
