package eu.laprell.timetable.background;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

import eu.laprell.timetable.BuildConfig;
import eu.laprell.timetable.MainActivity;
import eu.laprell.timetable.R;
import eu.laprell.timetable.database.AbsTimetableDatabase;
import eu.laprell.timetable.database.Day;
import eu.laprell.timetable.database.Lesson;
import eu.laprell.timetable.database.Place;
import eu.laprell.timetable.database.TimeUnit;
import eu.laprell.timetable.database.TimetableDatabase;
import eu.laprell.timetable.utils.Const;

/**
 * Created by david on 08.12.14.
 */
@SuppressLint("CommitPrefEdits")
public class LessonNotifier {

    private static final String TAG = "LessonNotifier";
    private static final int VERSION = 1;

    private static final long RES_ALARM_ALREADY_SET = -1;
    private static final long RES_NOTHING_FOUND = -2;

    private Context mContext;
    private TimeUnit[] mTimes;

    private Handler mHandler, mUiHandler;
    private TimetableDatabase mDatabase;
    private Thread mThread = new Thread(new Runnable() {
        @Override
        public void run() {
            Looper.prepare();
            mHandler = new Handler();
            mHandler.postAtFrontOfQueue(mInitRunnable);
            Looper.loop();
        }
    });
    private SharedPreferences mNotifPref;

    private PowerManager.WakeLock mNotificationWakeLock;

    private int mNotifyInSBefore = 10 * 60;

    private Runnable mInitRunnable = new Runnable() {
        @Override
        public void run() {
            mDatabase = new TimetableDatabase(mContext);
            mTimes = mDatabase.getTimeUnitsByIds(
                    mDatabase.getDatabaseEntries(TimetableDatabase.TYPE_TIMEUNIT));
            mNotifPref = mContext.getSharedPreferences("notifications", Context.MODE_PRIVATE);
            if(mNotifPref.getInt("version", 0) < VERSION  || BuildConfig.DEBUG) {
                mNotifPref.edit().clear().commit();
                mNotifPref.edit().putInt("version", VERSION).commit();
            }
        }
    };

    public LessonNotifier(Context c) {
        mContext = c;

        mUiHandler = new Handler();
        mThread.start();
    }

    /**
     * This method will initialize a new check for notifications.
     * The real check will not run imminently it will run in the
     * background
     */
    public void checkForNewNotifications() {
        if(mHandler != null)
            mHandler.post(mDoCheck);
        else if(mUiHandler != null) {
            mUiHandler.removeCallbacks(mDelayCheck);
            mUiHandler.postDelayed(mDelayCheck, 1000);
        }
    }
    private Runnable mDoCheck = new Runnable() {
        @Override
        public void run() {
            _check(-1);
        }
    };
    private Runnable mDelayCheck = new Runnable() {
        @Override
        public void run() {
            checkForNewNotifications();
        }
    };

    /**
     * Will initialize a check for the given Intent witch the action
     * {@link eu.laprell.timetable.utils.Const#ACTION_NEXT_TIMEUNIT_PENDING}
     * @param i the intent that got triggered
     */
    public void pendingTimeUnit(final Intent i) {
        if(mNotificationWakeLock != null && mNotificationWakeLock.isHeld()) {
            mNotificationWakeLock.release();
        }

        PowerManager power = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        mNotificationWakeLock = power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "Timetable_Notification");
        mNotificationWakeLock.acquire(10 * 1000);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                _pendingTimeUnit(i);
            }
        });
    }

    public void cancelCurrentNotification() {
        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(mContext);

        // Build the notification and issues it with notification manager.
        notificationManager.cancel(Const.NOTIFICATION_ID_NEXT_LESSON);
    }

    private void _pendingTimeUnit(Intent intent) {
        Logger.log(TAG, "got intent: " + intent.toString());

        TimeUnit time = intent.getParcelableExtra("timeunit");
        Day day = intent.getParcelableExtra("day");

        time = (TimeUnit) mDatabase.getDatabaseEntryById(TimetableDatabase.TYPE_TIMEUNIT,
                time.getId());
        day = mDatabase.getDayForDayOfWeek(day.getDayOfWeek());

        Logger.log(TAG, "We have the day=" + day.getDayOfWeek() + " time=" +
                getDateM(time.getStartTime()));

        executeNewPendingTimeUnit(day, time);
    }

    private void executeNewPendingTimeUnit(Day day, TimeUnit time) {
        long lid = day.getLessonIdAt(time);
        boolean maybeOldShown;

        if(!TimetableDatabase.isNoId(lid)) {
            Lesson les = (Lesson)mDatabase.getDatabaseEntryById(
                    TimetableDatabase.TYPE_LESSON, lid);

            Logger.log(TAG, "Making Notification for=" + les.getTitle());

            // makeNotification returns true when showing a new notification
            // this should be kept and will replace the (maybe current one)
            maybeOldShown = !_makeNotification(les, time, day, false);
        } else {
            Logger.log(TAG, "What no valid id here?");
            maybeOldShown = true;
        }

        if(maybeOldShown && PreferenceManager.getDefaultSharedPreferences(mContext)
                .getBoolean("pref_dismiss_notif_when_past", true)) {
            cancelCurrentNotification();
        }

        _check(time.getId());
    }

    private void _check(long skip) {
        final int dayOfWeek = getDayOfWeek();
        long time = getCurrentTimeInM();

        int skipTime = 0;
        if(!AbsTimetableDatabase.isNoId(skip)) {
            TimeUnit t = (TimeUnit) mDatabase.getDatabaseEntryById(
                    AbsTimetableDatabase.TYPE_TIMEUNIT, skip);

            skipTime = t.getStartTime();
        }

        for (int i =  0;i < 7;i++) {
            int d = ((dayOfWeek + i - 1) % 7) + 1;

            long result = checkForDayAndFromTime(d, time, skip, skipTime);

            Logger.log(TAG, "Got result=" + result);

            if(result == RES_ALARM_ALREADY_SET) {
                return;
            } else if (result == RES_NOTHING_FOUND) {
                Logger.log(TAG, "We found nothing - go to next day");
            } else {
                return;
            }

            time = 0;
            skip = -1;
        }
    }

    private long checkForDayAndFromTime(int d, long time, long skip, int skipAllBefore) {
        Day day = mDatabase.getDayForDayOfWeek(d);

        for(TimeUnit t : mTimes) {
            String txt = d + " at " + getDateM(t.getStartTime());
            Logger.log(TAG, txt);

            if(t.isAfter(time) && t.getStartTime() > skipAllBefore) {
                if(t.getId() == skip) {
                    Logger.log(TAG, "We are skipping the TimeUnit because after=" +
                            getDateM(skipAllBefore) + " at="
                            + getDateM(t.getStartTime()));
                    continue;
                }

                if(!isAlarmAlreadySet(day.getDayOfWeek(), t)) {
                    Calendar c = generateNotificationTime(day, t);
                    PendingIntent pIn = buildIntentForWakeup(day, t);

                    updateForNextWakeUp(pIn, c.getTimeInMillis());

                    saveNotifAlarmSet(day.getDayOfWeek(), t);

                    return t.getId();
                } else {
                    Logger.log(TAG, "We already set the alarm for the next lesson: "
                            + t.getId() + " at=" + getDateM(t.getStartTime()));
                    return -1;
                }
            } else if(t.getStartTime() > time && t.getEndTime() < time) {
                if(!isAlreadyNotifiedToday(day.getDayOfWeek(), t)) {
                    //makeNotification()

                    Logger.log(TAG, "Is in time with: " + getDateM(t.getStartTime()) + " " + t.getId());
                }
            }
        }

        return -2;
    }

    public boolean makeTestNotification(Lesson lesson, TimeUnit time, Day day) {
        return _makeNotification(lesson, time, day, true);
    }

    private boolean _makeNotification(Lesson lesson, TimeUnit time, Day day, boolean test) {
        if(!test) {
            long now = getCurrentTimeInM();

            long timediff = Math.abs(now - time.getStartTime());

            if (day.getDayOfWeek() != getDayOfWeek()
                    || timediff > mNotifyInSBefore * 1.3f / 60) {
                Logger.log(TAG, "Wrong time for: " + lesson.getTitle() + " timediff=" + timediff);
                return false;
            }

            mNotifPref.edit().putInt(getNotiPrefFor(day.getDayOfWeek(),
                    time.getId()), getDayOfYear()).apply();
        }

        Place place = null;
        if(test) {
            place = new Place(-1);
            place.setTitle("Test-Place");
        } else {
            place = (Place) mDatabase.getDatabaseEntryById(TimetableDatabase.TYPE_PLACE,
                    day.getPlaceIdAt(time));
        }

        String title = lesson.getTitle();
        String content = time.makeTimeString("s - e");

        String secText = "";

        if(place != null)
            secText += place.getTitle() + "\n";

        if(lesson.getTeacher() != null)
            secText += lesson.getTeacher();

        if(secText.length() == 0) secText = null;

        int imageId = mDatabase.getImageIdForLesson(lesson);
        Bitmap image = getBitmapForWearable(imageId, lesson.getColor());
        int nid = test ? Const.NOTIFICATION_ID_NEXT_LESSON_TEST : Const.NOTIFICATION_ID_NEXT_LESSON;

        showNextSubjectNotification(title, content, image, lesson.getId(), secText, nid);

        return true;
    }

    private void showNextSubjectNotification(String contentTitle, String contentText,
                                            Bitmap background, long lid,
                                            String secText, int notifId) {
        // Build intent for notification content
        Intent viewIntent = new Intent(mContext, MainActivity.class);
        viewIntent.setAction(Const.ACTION_VIEW_IN_TIMETABLE);
        viewIntent.putExtra(Const.EXTRA_DAY_OF_WEEK_BY_NUM, getDayOfWeek());
        viewIntent.putExtra(Const.EXTRA_LESSON_ID, lid);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(mContext, 0, viewIntent, 0);

        Intent cancelIntent = new Intent(mContext, TimeReceiver.class);
        cancelIntent.setAction(Const.ACTION_CANCEL_NEXT_LESSON_NOTIFICATION);
        PendingIntent cancelPendingIntent =
                PendingIntent.getBroadcast(mContext, 0, cancelIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.ic_school_white_48dp)
                        .setContentTitle(contentTitle)
                        .setContentText(contentText)
                        .setContentIntent(viewPendingIntent)
                        .addAction(R.drawable.ic_done_white_48dp,
                                mContext.getString(R.string.done), cancelPendingIntent);

        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender()
                        .setBackground(background);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        if(secText != null && prefs.getBoolean("pref_wear_extended_infos", true)) {
            // Create a big text style for the second page
            NotificationCompat.BigTextStyle secondPageStyle
                    = new NotificationCompat.BigTextStyle();

            secondPageStyle.setBigContentTitle("Page 2") // TODO: find where is the title needed?!
                    .bigText(secText);

            // Create second page notification
            Notification secondPageNotification =
                    new NotificationCompat.Builder(mContext)
                            .setStyle(secondPageStyle)
                            .build();

            wearableExtender.addPage(secondPageNotification);
        }

        notificationBuilder.extend(wearableExtender);

        if(prefs.getBoolean("pref_enable_notifications", true)) {

            // Get an instance of the NotificationManager service
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(mContext);

            // Build the notification and issues it with notification manager.
            notificationManager.notify(notifId, notificationBuilder.build());
        }
    }

    private Bitmap getBitmapForWearable(int image, int color) {
        final int width = 640;
        final int height = 400;

        BitmapFactory.Options opt  = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(mContext.getResources(), image, opt);

        int scale = opt.outWidth / width;

        while(scale != 1 && scale != 2 && scale != 4 && scale != 8 && scale != 16) {
            scale--;
        }

        opt = new BitmapFactory.Options();
        opt.inSampleSize = scale;
        Bitmap b = BitmapFactory.decodeResource(mContext.getResources(), image, opt);

        Drawable d = new BitmapDrawable(mContext.getResources(), b);
        PorterDuff.Mode mMode = PorterDuff.Mode.MULTIPLY;
        d.setColorFilter(color, mMode);

        b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);

        d.setBounds(0, 0, width, height);
        d.draw(canvas);

        return b;
    }

    /**
     *
     * @param d the day at which the notification will be triggered
     * @param t the timeunit desribing the time at which the notification will be triggered
     * @return the calendar that was generated
     */
    private Calendar generateNotificationTime(Day d, TimeUnit t) {
        Calendar c = Calendar.getInstance();

        int deltaDays;
        if(getDayOfWeek() > d.getDayOfWeek())
            deltaDays = (d.getDayOfWeek() + 7) - getDayOfWeek();
        else
            deltaDays = d.getDayOfWeek() - getDayOfWeek();
        c.add(Calendar.DAY_OF_YEAR, deltaDays);

        final int h = t.getStartTime() / 60;
        final int m = t.getStartTime() % 60;
        // First go ahead and set it to the appropriate times
        // -> lesson start
        c.set(Calendar.HOUR_OF_DAY, h);
        c.set(Calendar.MINUTE, m);
        c.set(Calendar.SECOND, 0);

        // Now go to the notification time
        c.add(Calendar.SECOND, -mNotifyInSBefore);

        return c;
    }

    /**
     * Will save the prop that the alarm was set
     * @param day day of week
     * @param t time
     */
    private void saveNotifAlarmSet(int day, TimeUnit t) {
        mNotifPref.edit().putInt(getPrefFor(day, t.getId()), t.getStartTime()).commit();
    }

    private PendingIntent buildIntentForWakeup(Day d, TimeUnit t) {
        Intent intent = new Intent(mContext, TimeReceiver.class);

        intent.putExtra("day", d);
        intent.putExtra("timeunit", t);
        intent.setAction(Const.ACTION_NEXT_TIMEUNIT_PENDING);

        return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void updateForNextWakeUp(PendingIntent p, long at) {
        Logger.log(TAG, "Set alarmanager wakeup to " + getDate(at) + " p=" + p.toString());

        AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, at, p);
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        return DateFormat.format("dd-MM-yyyy HH:mm", cal).toString();
    }

    private String getDateM(int time) {
        return (time / 60) + ":" + (time % 60);
    }

    private boolean isAlarmAlreadySet(int d, TimeUnit t) {
        return mNotifPref.getInt(getPrefFor(d, t.getId()), -1)
                == t.getStartTime();
    }

    private boolean isAlreadyNotifiedToday(int day, TimeUnit t) {
        return mNotifPref.getInt(getNotiPrefFor(day, t.getId()), -1)
                == getDayOfYear();
    }

    private static String getPrefFor(int day, long tid) {
        return "notif_chk_" + String.valueOf(day) + "-" + String.valueOf(tid);
    }

    private static String getNotiPrefFor(int day, long tid) {
        return "notif_notified_" + String.valueOf(day) + "-" + String.valueOf(tid);
    }

    /**
     * Returns the current day of week as one of {@link eu.laprell.timetable.database.Day.OF_WEEK}
     * @return the current day of week
     */
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

    public static int getDayOfYear() {
        return Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Returns the current time of the day in seconds passed 0:00:00
     * @return the seconds that passed the day
     */
    public static int getCurrentTimeInS() {
        Calendar c = Calendar.getInstance();
        long now = c.getTimeInMillis();

        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        long passed = now - c.getTimeInMillis();

        return (int)(passed / 1000);
    }

    /**
     * Returns the current time of the day in minutes passed 0:00
     * @return the minutes that passed the day
     */
    public static int getCurrentTimeInM() {
        return getCurrentTimeInS() / 60;
    }
}
