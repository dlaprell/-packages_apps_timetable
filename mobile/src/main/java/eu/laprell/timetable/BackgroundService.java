package eu.laprell.timetable;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.util.Calendar;

import eu.laprell.timetable.background.notifications.LessonNotifier2;
import eu.laprell.timetable.database.Day;
import eu.laprell.timetable.database.TimetableDatabase;

public class BackgroundService extends Service {
    private static BackgroundService sService;
    public static BackgroundService get() {
        return sService;
    }

    private TimetableDatabase mDb;
    private LessonNotifier2 mNotifier;

    public BackgroundService() {
    }

    @Override
    public void onCreate() {
        mDb = new TimetableDatabase(this);
        sService = this;

        mNotifier = new LessonNotifier2(this);
    }

    public LessonNotifier2 getLessonNotifier() {
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
