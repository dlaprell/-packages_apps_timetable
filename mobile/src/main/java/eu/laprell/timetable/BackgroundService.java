package eu.laprell.timetable;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import java.util.Calendar;

import eu.laprell.timetable.background.LessonNotifier;
import eu.laprell.timetable.database.Day;
import eu.laprell.timetable.database.Lesson;
import eu.laprell.timetable.database.Place;
import eu.laprell.timetable.database.TimeUnit;
import eu.laprell.timetable.database.TimetableDatabase;

public class BackgroundService extends Service {
    private static BackgroundService sService;
    public static BackgroundService get() {
        return sService;
    }

    private TimetableDatabase mDb;
    private LessonNotifier mNotifier;

    private int mNotifyBeforeSec = 10 * 60;

    private Thread mWorkerThread = new Thread(new Runnable() {
        @Override
        public void run() {
            Looper.prepare();
            mWorkerHandler = new Handler();

            mWorkerHandler.post(new Runnable() {
                @Override
                public void run() {
                    mNotifier.checkForNewNotifications();
                }
            });
            Looper.loop();
        }
    });
    private Handler mWorkerHandler, mUiHandler;

    private class Data {
        TimeUnit time;
        Lesson lesson;
        Place place;
    }

    public BackgroundService() {
    }

    @Override
    public void onCreate() {
        mDb = new TimetableDatabase(this);
        sService = this;

        mNotifier = new LessonNotifier(this);

        mUiHandler = new Handler();
        mWorkerThread.start();
    }

    public LessonNotifier getLessonNotifier() {
        return mNotifier;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public TimetableDatabase getTimetableDatabase() {
        return mDb;
    }

    public static int getDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        switch (day) {
            case Calendar.MONDAY:
                return Day.OF_WEEK.MONDAY;

            case Calendar.TUESDAY:
                return Day.OF_WEEK.TUESDAY;

            case Calendar.WEDNESDAY:
                return Day.OF_WEEK.WEDNESDAY;

            case Calendar.THURSDAY:
                return Day.OF_WEEK.THURSDAY;

            case Calendar.FRIDAY:
                return Day.OF_WEEK.FRIDAY;

            case Calendar.SATURDAY:
                return Day.OF_WEEK.SATURDAY;

            case Calendar.SUNDAY:
                return Day.OF_WEEK.SUNDAY;
        }

        return -1;
    }
}
