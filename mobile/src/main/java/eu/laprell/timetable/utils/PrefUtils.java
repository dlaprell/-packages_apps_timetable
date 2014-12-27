package eu.laprell.timetable.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by david on 20.11.14.
 */
public class PrefUtils {

    public static boolean isDebugNotifications(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c)
                .getBoolean("pref_enable_debug_notifications", false);
    }

    public static SharedPreferences getSchoolPref(Context c) {
        return c.getSharedPreferences("school", Context.MODE_PRIVATE);
    }
}
