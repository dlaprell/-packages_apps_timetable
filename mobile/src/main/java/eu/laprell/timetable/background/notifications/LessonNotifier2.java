package eu.laprell.timetable.background.notifications;

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
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Calendar;

import eu.laprell.timetable.MainActivity;
import eu.laprell.timetable.R;
import eu.laprell.timetable.background.Logger;
import eu.laprell.timetable.background.TimeReceiver;
import eu.laprell.timetable.database.Day;
import eu.laprell.timetable.database.Lesson;
import eu.laprell.timetable.database.Place;
import eu.laprell.timetable.database.Teacher;
import eu.laprell.timetable.database.TimeUnit;
import eu.laprell.timetable.database.TimetableDatabase;
import eu.laprell.timetable.utils.Const;

import static eu.laprell.timetable.background.notifications.NotifUtils.getCurrentTimeInM;
import static eu.laprell.timetable.background.notifications.NotifUtils.getDayOfWeek;
import static eu.laprell.timetable.database.AbsTimetableDatabase.TYPE_LESSON;
import static eu.laprell.timetable.database.AbsTimetableDatabase.TYPE_TIMEUNIT;
import static eu.laprell.timetable.database.AbsTimetableDatabase.isNoId;

/**
 * Class that controls when and how a Notification is posted.
 */
public class LessonNotifier2 {

    private static final String TAG = "LessonNotifier2";
    //private static final int VERSION = 1; // For now unused ...

    private Context mContext;
    private TimeUnit[] mTimes;
    private int mNotifyInSBefore;
    private TimetableDatabase mDatabase;
    private NotificationStore mStore;

    public LessonNotifier2(Context c) {
        mContext = c.getApplicationContext();
        mDatabase = new TimetableDatabase(mContext);
        mStore = new NotificationStore(mContext);

        // For now hardcoded but this implementation can be easily changed so that the user
        // can decide when to notify
        mNotifyInSBefore = 60 * 10;

        reloadTimes();

        checkForNewNotifications();
    }

    public void reloadTimes() {
        mTimes = mDatabase.getTimeUnitsByIds(mDatabase.getDatabaseEntries(TYPE_TIMEUNIT));

        // maybe call checkForNewNotifications() ?
    }

    public void pendingTimeUnit(final Intent i) {
        int d = i.getIntExtra("day", -1);
        TimeUnit timeunit = i.getParcelableExtra("timeunit");

        NotificationObject obj = NotificationObject.obtain();
        obj.mAtDay = d;
        obj.mForTime = timeunit;
        obj.mNotificationTime = generateNotificationTime(obj);

        obj.mForTime = (TimeUnit) mDatabase.getDatabaseEntryById(TYPE_TIMEUNIT,
                obj.mForTime.getId());

        Logger.log(TAG, "Got pendingTU=" + i + " made=" + obj);

        if (!mStore.isNotificationPostedFor(obj)) {
            tryPostNotification(obj);
        }

        // MUST! be called after tryPostNotification, never before if we got a pending timeunit
        // Reason: If we call it before tryPost we will run into the situation, that we try to
        //         set the AlarmManager to the current time again
        checkForNewNotifications();
    }

    private void tryPostNotification(NotificationObject obj) {
        // at this point we have to query the day
        Day day = mDatabase.getDayForDayOfWeek(obj.mAtDay);

        if (isAtRightTime(obj)) {

            long lid = day.getLessonIdAt(obj.mForTime);
            if(!isNoId(lid)) {
                Lesson lesson = (Lesson) mDatabase.getDatabaseEntryById(
                        TYPE_LESSON, lid);

                // Only post a notification if the lesson is != null
                if(lesson != null) {
                    Logger.log(TAG, "Posting a notification for " + obj + " lesson=" + lesson);

                    _postNotification(lesson, obj.mForTime, day, false);
                }
            }

            // We still save that we notified this Lesson (even if it doesn't exists) because
            // otherwise we would try to notify it again. This would end in an infinitive loop
            mStore.saveNotificationPostedFor(obj);
        } else {
            // Hmm... what exactly went wrong?!
            Logger.log(TAG, "We have the wrong time for: " + obj);
        }
    }

    public void checkForNewNotifications() {
        final int dayOfWeek = getDayOfWeek();

        int afterTime = getCurrentTimeInM();

        for (int i = 0;i < 7;i++) {
            int day = ((dayOfWeek + i - 1) % 7) + 1;

            NotificationObject o = getNextNotificationTime(day, afterTime);

            while (o != null) {

                // isNotificationPostedFor should have a higher priority than isAlarmAlreadySetFor
                // basically because a posted notification is completely ready, isAlarmAlreadySetFor
                // means there a steps missing

                if (!mStore.isNotificationPostedFor(o)) {
                    if (!mStore.isAlarmAlreadySetFor(o)) {
                        // if we haven't set the alarm manager already to it now
                        setAlarmManagerWakeUp(o);

                        Logger.log(TAG, "Setting the AlarmManager to wake up for: " + o);

                    } // else we already set a alarmanager for it

                    o.finish();

                    // We can return at that point since, we haven't posted the notification yet,
                    // but already set the alarm
                    return;
                } // else we have already posted a notification for this, so search for next one

                // So now get the start time for the notifcation, next time we want to get the one
                // after this time
                afterTime = o.mForTime.getStartTime();
                o = getNextNotificationTime(day, afterTime);
            }

            Logger.log(TAG, "Nothing found at the current day[" + day + "]; go to next");

            // Hmm... nothing found so we go to the next day
            afterTime = -1;
        }
    }

    /**
     * This method tries to find the next possible TimeUnit to notify it. If a TimeUnit was
     * found a NotificationObject will be build and return
     * @param dayOfWeek to build the NotificationObject
     * @param afterTime the time to search for next TimeUnit after it
     * @return null or on succeed the built NotificationObject
     */
    private NotificationObject getNextNotificationTime(int dayOfWeek, int afterTime) {
        TimeUnit timeUnit = getNextTimeUnitAfter(afterTime);

        if (timeUnit != null) {
            NotificationObject obj = NotificationObject.obtain();
            obj.mForTime = timeUnit;
            obj.mAtDay = dayOfWeek;
            obj.mNotificationTime = generateNotificationTime(obj);

            return obj;
        } // else we have no more timeunit int the current day so return nothing

        return null;
    }

    private void setAlarmManagerWakeUp(NotificationObject o) {
        PendingIntent pIntent = buildIntentForWakeUp(o);

        AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, o.mNotificationTime.getTimeInMillis(), pIntent);

        mStore.saveAlarmSetForNotification(o);
    }

    private PendingIntent buildIntentForWakeUp(NotificationObject o) {
        Intent intent = new Intent(mContext, TimeReceiver.class);

        intent.putExtra("day", o.mAtDay);
        intent.putExtra("timeunit", o.mForTime);
        intent.setAction(Const.ACTION_NEXT_TIMEUNIT_PENDING);

        return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private TimeUnit getNextTimeUnitAfter(int timeInM) {
        for (TimeUnit t : mTimes) {
            if(t.getStartTime() > timeInM)
                return t;
        }

        return null;
    }

    public void cancelCurrentNotification() {
        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(mContext);

        // Build the notification and issues it with notification manager.
        notificationManager.cancel(Const.NOTIFICATION_ID_NEXT_LESSON);
    }

    public boolean makeTestNotification(Lesson lesson, TimeUnit time, Day day) {
        return _postNotification(lesson, time, day, true);
    }

    private boolean isAtRightTime(NotificationObject obj) {
        return isAtRightTime(obj.mForTime, obj.mAtDay);
    }

    private boolean isAtRightTime(TimeUnit time, int day) {
        long now = getCurrentTimeInM();

        long timediff = Math.abs(now - time.getStartTime());

        return (day == getDayOfWeek()
                && timediff < mNotifyInSBefore * 1.3f / 60);
    }

    private boolean _postNotification(Lesson lesson, TimeUnit time, Day day, boolean test) {
        if (lesson == null)
            return false; // How should we notify a lesson that doesn't exits?S

        Place place;
        if (test) {
            place = new Place(-1);
            place.setTitle("Test-Place");
        } else {
            place = (Place) mDatabase.getDatabaseEntryById(TimetableDatabase.TYPE_PLACE,
                    day.getPlaceIdAt(time));
        }

        Teacher teacher;
        if(test) {
            teacher = new Teacher(-1);
            teacher.setPrefix("Herr Prof.");
            teacher.setFirstName("Albert");
            teacher.setSecondName("Einstein");
        } else {
            teacher = (Teacher) mDatabase.getDatabaseEntryById(TimetableDatabase.TYPE_TEACHER,
                    lesson.getTeacherId());
        }

        String title = lesson.getTitle();
        String content = time.makeTimeString("s - e");

        String secText = "";

        if(place != null)
            secText += place.getTitle() + "\n";

        if(teacher != null)
            secText += teacher.getFullName();

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

    private int calculateDayDelta(int d) {
        if(getDayOfWeek() > d)
            return (d + 7) - getDayOfWeek();
        else
            return d - getDayOfWeek();
    }

    private Calendar generateNotificationTime(NotificationObject obj) {
        return generateNotificationTime(obj.mAtDay, obj.mForTime);
    }

    /**
     *
     * @param d the day at which the notification will be triggered
     * @param t the timeunit describing the time at which the notification will be triggered
     * @return the calendar that was generated
     */
    private Calendar generateNotificationTime(int d, TimeUnit t) {
        Calendar c = Calendar.getInstance();

        c.add(Calendar.DAY_OF_YEAR, calculateDayDelta(d));

        final int h = t.getStartTime() / 60;
        final int m = t.getStartTime() % 60;
        // First go ahead and set it to the appropriate times
        // -> lesson start
        c.set(Calendar.HOUR_OF_DAY, h);
        c.set(Calendar.MINUTE, m);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        // Now go to the notification time
        c.add(Calendar.SECOND, -mNotifyInSBefore);

        return c;
    }
}
