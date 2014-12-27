package eu.laprell.timetable.fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.github.machinarius.preferencefragment.PreferenceFragment;

import eu.laprell.timetable.BackgroundService;
import eu.laprell.timetable.BuildConfig;
import eu.laprell.timetable.R;

/**
 * Created by david on 07.11.14.
 */
public class SettingsFragmentCompat extends PreferenceFragment implements Preference.OnPreferenceChangeListener{

    public SettingsFragmentCompat() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_main);

        final PreferenceScreen prefs = getPreferenceScreen();

        if (!BuildConfig.DEBUG) {
            PreferenceCategory debugCat = (PreferenceCategory)
                    prefs.findPreference("key_prefc_debug");

            prefs.removePreference(debugCat);
        } else {
            prefs.findPreference("pref_enable_debug_notifications")
                    .setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if("pref_enable_debug_notifications".equals(preference.getKey())) {
            if((Boolean)newValue) {
                BackgroundService.get().getLessonNotifier().checkForNewNotifications();
            }
        }

        return true;
    }
}
