package eu.laprell.timetable.background.notifications;

import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

import eu.laprell.timetable.database.Day;

/**
 * Created by david on 26.01.15
 */
public class NotifUtils {

    public static String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        return DateFormat.format("dd-MM-yyyy HH:mm", cal).toString();
    }

    public static String  getDateM(int time) {
        return (time / 60) + ":" + (time % 60);
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
