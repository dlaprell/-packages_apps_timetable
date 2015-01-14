package eu.laprell.timetable;

import android.app.Application;

import eu.laprell.timetable.animation.ActivityTransitions;
import eu.laprell.timetable.background.LessonNotifier;
import eu.laprell.timetable.background.Logger;
import eu.laprell.timetable.background.SpecialBitmapCache;

/**
 * Created by david on 13.12.14.
 */
public class MainApplication extends Application {

    public static boolean sLoggingEnabled = false;

    private ActivityTransitions mTransitions;
    private LessonNotifier mNotifier;

    @Override
    public void onCreate() {
        super.onCreate();

        sLoggingEnabled = getSharedPreferences("dbg", 0).getBoolean("logging", false);
        Logger.assignContext(this);

        mNotifier = new LessonNotifier(this);

        mTransitions = ActivityTransitions.init(this);
        SpecialBitmapCache.init(this);
    }

    public LessonNotifier getLessonNotifier() {
        return mNotifier;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        mTransitions.onTrimMemory(level);
    }

    public void saveLogSettings() {
        getSharedPreferences("dbg", 0).edit().putBoolean("logging", sLoggingEnabled).commit();
    }

    @Override
    public void onTerminate() {
        saveLogSettings();

        super.onTerminate();
    }
}
