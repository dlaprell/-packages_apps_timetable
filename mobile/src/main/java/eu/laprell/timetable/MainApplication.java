package eu.laprell.timetable;

import android.app.Application;

import eu.laprell.timetable.animation.ActivityTransitions;
import eu.laprell.timetable.background.SpecialBitmapCache;

/**
 * Created by david on 13.12.14.
 */
public class MainApplication extends Application {

    private ActivityTransitions mTransitions;

    @Override
    public void onCreate() {
        super.onCreate();

        mTransitions = ActivityTransitions.init(this);
        SpecialBitmapCache.init(this);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        mTransitions.onTrimMemory(level);
    }
}
