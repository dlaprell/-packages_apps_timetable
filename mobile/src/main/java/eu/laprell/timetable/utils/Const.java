package eu.laprell.timetable.utils;

import android.os.Build;

/**
 * Created by david on 17.11.14.
 */
public class Const {
    public static final String EXTRA_DAY_OF_WEEK_BY_NUM = "extra_day_of_week_by_num";
    public static final String EXTRA_LESSON_ID = "extra_lesson_id";
    public static final String EXTRA_TIMEUNIT_ID = "extra_timeunit_id";
    public static final String EXTRA_PLACE_ID = "extra_place_id";
    public static final String EXTRA_TIME_AT = "extra_time_at";

    public static final String ACTION_NOTIFY_NEXT_LESSON
            = "eu.laprell.timetable.NOTIFY_NEXT_LESSON";

    public static final String ACTION_CANCEL_NEXT_LESSON_NOTIFICATION
            = "eu.laprell.timetable.CANCEL_NEXT_LESSON_NOTIFICATION";

    public static final String ACTION_VIEW_IN_TIMETABLE
            = "eu.laprell.timetable.VIEW_IN_TIMETABLE";

    public static final String ACTION_NEXT_TIMEUNIT_PENDING =
            "eu.laprell.timetable.NEXT_TIMEUNIT_PENDING_NEW";

    public static final int NOTIFICATION_ID_NEXT_LESSON = 1;
    public static final int NOTIFICATION_ID_NEXT_LESSON_TEST = 100;

    public static final boolean FW_SUPPORTS_HERO_TRANSITION =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && false;

    public static final boolean FW_SUPPORTS_DROP_SHADOWS =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
}
