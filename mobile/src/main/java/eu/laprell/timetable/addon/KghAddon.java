package eu.laprell.timetable.addon;

import android.content.Context;
import android.content.SharedPreferences;

import eu.laprell.timetable.database.TimeUnit;
import eu.laprell.timetable.database.TimetableDatabase;
import eu.laprell.timetable.utils.PrefUtils;

/**
 * Created by david on 11.11.14.
 */
public class KghAddon {

    public static void runAddon(Context c) {
        initSchool(PrefUtils.getSchoolPref(c));

        TimetableDatabase db = new TimetableDatabase(c);

        addTimeTable(db);

        db.clearCache();
    }

    public static void addTimeTable(TimetableDatabase db) {
        TimeUnit t = new TimeUnit(-1);

        t.setStartTime(7, 45);
        t.setEndTime(8, 53);
        TimeUnit t1 = (TimeUnit) db.insertDatabaseEntryForId(t);

        t.setStartTime(9, 0);
        t.setEndTime(10, 8);
        TimeUnit t2 = (TimeUnit) db.insertDatabaseEntryForId(t);

        t.setStartTime(10, 8);
        t.setEndTime(10, 30);
        t.setBreak(true);
        TimeUnit b1 = (TimeUnit) db.insertDatabaseEntryForId(t);

        t.setStartTime(10, 30);
        t.setEndTime(11, 38);
        t.setBreak(false);
        TimeUnit t3 = (TimeUnit) db.insertDatabaseEntryForId(t);

        t.setStartTime(11, 45);
        t.setEndTime(12, 53);
        TimeUnit t4 = (TimeUnit) db.insertDatabaseEntryForId(t);

        t.setStartTime(12, 53);
        t.setEndTime(13, 45);
        t.setBreak(true);
        TimeUnit b2 = (TimeUnit) db.insertDatabaseEntryForId(t);

        t.setStartTime(13, 45);
        t.setEndTime(14, 53);
        t.setBreak(false);
        TimeUnit t5 = (TimeUnit) db.insertDatabaseEntryForId(t);

        t.setStartTime(15, 0);
        t.setEndTime(16, 8);
        TimeUnit t6 = (TimeUnit) db.insertDatabaseEntryForId(t);
    }

    public static void initSchool(SharedPreferences pref) {
        SharedPreferences.Editor e = pref.edit();

        e.putInt("school_id", Addons.Ids.ID_KREISGYMNASIUM_HEINSBERG);

        e.apply();
    }
}
