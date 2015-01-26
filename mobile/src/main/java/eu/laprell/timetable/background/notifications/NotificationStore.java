package eu.laprell.timetable.background.notifications;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by david on 26.01.15.
 */
public class NotificationStore {
    @SuppressWarnings("FieldCanBeLocal")
    private Context mContext;

    private final SharedPreferences mSaveState;
    private final Object mLock = new Object();

    public NotificationStore(Context c) {
        mContext = c;
        mSaveState = mContext.getSharedPreferences("notifications_store", 0);
    }

    // region notificationposted
    public void saveNotificationPostedFor(NotificationObject obj) {
        synchronized (mLock) {
            mSaveState.edit().putLong(getNotifiedKey(obj),
                    obj.mNotificationTime.getTimeInMillis()).apply();
        }
    }

    public boolean isNotificationPostedFor(NotificationObject obj) {
        long savedTime;

        synchronized (mLock) {
            savedTime = mSaveState.getLong(getNotifiedKey(obj), -1);
        }

        return savedTime == obj.mNotificationTime.getTimeInMillis();
    }

    private String getNotifiedKey(NotificationObject o) {
        String key = "notif_already_posted_";
        key += getNotificationBaseString(o);
        return key;
    }
    // endregion notificationposted

    // region alarm
    public void saveAlarmSetForNotification(NotificationObject o) {
        long saveTime = o.mNotificationTime.getTimeInMillis();

        synchronized (mLock) {
            mSaveState.edit().putLong(getAlarmKey(o), saveTime).apply();
        }
    }

    public boolean isAlarmAlreadySetFor(NotificationObject o) {
        long savedTime;

        synchronized (mLock) {
            savedTime = mSaveState.getLong(getAlarmKey(o), -1);
        }

        return savedTime == o.mNotificationTime.getTimeInMillis();
    }

    private String getAlarmKey(NotificationObject o) {
        String key = "notif_alarm_set_";
        key += getNotificationBaseString(o);
        return key;
    }
    // endregion alarm

    private String getNotificationBaseString(NotificationObject o) {
        return String.valueOf(o.mAtDay) + "-" + String.valueOf(o.mForTime);
    }
}
