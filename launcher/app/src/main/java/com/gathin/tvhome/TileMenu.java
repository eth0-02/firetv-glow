package com.gathin.tvhome;

import android.app.*;
import android.content.*;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;
import org.json.JSONArray;
import java.util.*;

final class TileMenu {
    static void show(Activity activity, String pkg, String label, Runnable refresh) {
        List<String> apps = GlowPrefs.favorites(activity);
        int index = apps.indexOf(pkg);
        List<String> titles = new ArrayList<>();
        List<Runnable> actions = new ArrayList<>();
        if (index >= 0) {
            if (index > 0) {
                titles.add("Move left"); actions.add(() -> save(activity, GlowRules.move(apps,index,index-1),refresh));
            }
            if (index < apps.size()-1) {
                titles.add("Move right"); actions.add(() -> save(activity, GlowRules.move(apps,index,index+1),refresh));
            }
            titles.add("Replace favorite"); actions.add(() -> replace(activity,apps,index,refresh));
        } else if (apps.size() < 7) {
            titles.add("Add to favorites"); actions.add(() -> { apps.add(pkg); save(activity,apps,refresh); });
        }
        titles.add("App settings"); actions.add(() -> {
            try { activity.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"+pkg))); }
            catch (ActivityNotFoundException | SecurityException e) {
                Toast.makeText(activity,"App settings are unavailable for this item",Toast.LENGTH_LONG).show();
            }
        });
        new AlertDialog.Builder(activity).setTitle(label).setItems(titles.toArray(new String[0]),
                (dialog,which) -> actions.get(which).run()).setNegativeButton("Cancel",null).show();
    }
    private static void replace(Activity activity,List<String> apps,int index,Runnable refresh) {
        List<ResolveInfo> found = activity.getPackageManager().queryIntentActivities(
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER),0);
        Map<String,String> candidates = new TreeMap<>();
        List<String> hidden = Config.load(activity).hiddenApps;
        for (ResolveInfo item:found) {
            String pkg=item.activityInfo.packageName;
            if (!apps.contains(pkg) && !pkg.equals(activity.getPackageName()) && !hidden.contains(pkg))
                candidates.put(pkg,Cards.cleanLabel(item.loadLabel(activity.getPackageManager()).toString(),pkg));
        }
        List<String> packages = new ArrayList<>(candidates.keySet());
        packages.sort(Comparator.comparing(candidates::get,String.CASE_INSENSITIVE_ORDER));
        if (packages.isEmpty()) { Toast.makeText(activity,"No other TV apps found",Toast.LENGTH_LONG).show(); return; }
        String[] labels=new String[packages.size()];
        for(int i=0;i<labels.length;i++) labels[i]=candidates.get(packages.get(i));
        new AlertDialog.Builder(activity).setTitle("Replace favorite").setItems(labels,(d,which) -> {
            apps.set(index,packages.get(which)); save(activity,apps,refresh);
        }).setNegativeButton("Cancel",null).show();
    }
    private static void save(Activity activity,List<String> apps,Runnable refresh) {
        GlowPrefs.get(activity).edit().putString("favorites",new JSONArray(apps).toString()).commit();
        refresh.run();
    }
}
