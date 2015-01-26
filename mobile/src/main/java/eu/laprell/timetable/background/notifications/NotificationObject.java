package eu.laprell.timetable.background.notifications;

import java.util.ArrayList;
import java.util.Calendar;

import eu.laprell.timetable.database.TimeUnit;

/**
 * Created by david on 26.01.15
 */
public class NotificationObject {

    /*package*/ TimeUnit mForTime;
    /*package*/ int mAtDay;
    /*package*/ Calendar mNotificationTime;

    private NotificationObject() {

    }

    public void finish() {
        mForTime = null;
        mAtDay = -1;
        mNotificationTime = null;

        if(sCache.size() < 8)
            sCache.add(this);
    }

    @Override
    public String toString() {
        String forTime = mForTime == null ? "null" :
                mForTime.getId() + mForTime.makeTimeString("{s-e}");

        String time = mNotificationTime == null ? "null" :
                NotifUtils.getDate(mNotificationTime.getTimeInMillis());

        return "NotificationObject{" +
                "mForTime=" + forTime +
                ", mAtDay=" + mAtDay +
                ", mNotificationTime=" + time +
                '}';
    }

    private static final ArrayList<NotificationObject> sCache = new ArrayList<>(8);
    public static NotificationObject obtain() {
        NotificationObject obj;

        if(sCache.size() > 0) {
            // Always remove the last one to avoid causing a internal ArrayList gc
            obj = sCache.remove(sCache.size() - 1);
        } else {
            obj = new NotificationObject();
        }

        return obj;
    }
}
