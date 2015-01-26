package eu.laprell.timetable;

import android.app.Application;
import android.content.Context;
import android.support.v4.util.ArrayMap;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.Map;

import eu.laprell.timetable.animation.ActivityTransitions;
import eu.laprell.timetable.background.Logger;
import eu.laprell.timetable.background.SpecialBitmapCache;
import eu.laprell.timetable.background.notifications.LessonNotifier2;

/**
 * Created by david on 13.12.14.
 */
public class MainApplication extends Application {

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    public static boolean sLoggingEnabled = false;

    private static MainApplication sApplication;

    private ActivityTransitions mTransitions;
    private LessonNotifier2 mNotifier;

    private final Map<TrackerName, Tracker> mTrackers = new ArrayMap<>();

    @Override
    public void onCreate() {
        super.onCreate();

        sLoggingEnabled = getSharedPreferences("dbg", 0).getBoolean("logging", false);
        Logger.assignContext(this);

        mNotifier = new LessonNotifier2(this);

        mTransitions = ActivityTransitions.init(this);
        SpecialBitmapCache.init(this);

        sApplication = this;
    }

    public LessonNotifier2 getLessonNotifier() {
        return mNotifier;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        mTransitions.onTrimMemory(level);
        SpecialBitmapCache.getInstance().onTrimMemory(level);
    }

    public void saveLogSettings() {
        getSharedPreferences("dbg", 0).edit().putBoolean("logging", sLoggingEnabled).commit();
    }

    @Override
    public void onTerminate() {
        saveLogSettings();

        super.onTerminate();

        if (sApplication == this)
            sApplication = null;
    }

    public static LessonNotifier2 getLessonNotifier(Context c) {
        if (sApplication != null && c.getPackageName().equals(sApplication.getPackageName()))
            return sApplication.getLessonNotifier();
        else
            return null;
    }

    private static final String PROPERTY_ID = "UA-58004170-2";
    private synchronized Tracker _getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(sApplication);
            Tracker t = analytics.newTracker(PROPERTY_ID);
            t.enableAutoActivityTracking(true);
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }

    public static Tracker getTracker(TrackerName t) {
        return sApplication._getTracker(t);
    }
}
