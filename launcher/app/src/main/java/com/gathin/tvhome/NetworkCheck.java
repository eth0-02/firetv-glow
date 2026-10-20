package com.gathin.tvhome;

import android.app.*;
import android.content.*;
import android.net.*;
import java.net.*;
import java.util.concurrent.*;

final class NetworkCheck {
    static String status(Context c) {
        ConnectivityManager manager = c.getSystemService(ConnectivityManager.class);
        NetworkCapabilities caps = manager.getNetworkCapabilities(manager.getActiveNetwork());
        if (caps == null) return "Disconnected";
        if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL)) return "Sign-in required";
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ? "Connected" : "Internet unverified";
    }
    static void show(Activity activity) {
        AlertDialog dialog = new AlertDialog.Builder(activity).setTitle("Connection test")
                .setMessage("Checking connection and DNS…").setNegativeButton("Close", null).show();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String result;
            ConnectivityManager manager = activity.getSystemService(ConnectivityManager.class);
            Network network = manager.getActiveNetwork();
            if (network == null) result = "Wi-Fi / Ethernet disconnected.\nOpen Fire TV network settings to reconnect.";
            else {
                FutureTask<InetAddress[]> dns = new FutureTask<>(() -> network.getAllByName("example.com"));
                Thread resolver = new Thread(dns, "glow-dns"); resolver.setDaemon(true); resolver.start();
                try {
                    dns.get(5, TimeUnit.SECONDS);
                    HttpURLConnection connection = null;
                    try {
                        connection = (HttpURLConnection) network.openConnection(new URL("https://example.com"));
                        connection.setConnectTimeout(5000); connection.setReadTimeout(5000);
                        connection.setInstanceFollowRedirects(false);
                        int code = connection.getResponseCode();
                        result = code >= 200 && code < 400
                                ? "Internet works.\nDNS resolution and a secure web request succeeded."
                                : "DNS works. The web test returned HTTP " + code + ".";
                    } catch (Exception e) {
                        result = "DNS works, but the secure web test failed.\nCheck your router, sign-in page, and the TV's date/time.";
                    } finally { if (connection != null) connection.disconnect(); }
                } catch (Exception e) {
                    dns.cancel(true);
                    result = "DNS lookup failed or timed out.\nWi-Fi is connected, but name resolution is unavailable. Check Private DNS and router settings.";
                }
                result += "\n\nAndroid status: " + status(activity);
            }
            final String message = result;
            activity.runOnUiThread(() -> {
                if (!activity.isFinishing() && !activity.isDestroyed() && dialog.isShowing()) dialog.setMessage(message);
            });
            executor.shutdown();
        });
    }
}
