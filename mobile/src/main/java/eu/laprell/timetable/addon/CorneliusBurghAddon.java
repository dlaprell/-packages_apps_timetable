package eu.laprell.timetable.addon;

import android.content.Context;
import android.content.SharedPreferences;

import eu.laprell.timetable.R;
import eu.laprell.timetable.database.TimeUnit;
import eu.laprell.timetable.database.TimetableDatabase;
import eu.laprell.timetable.utils.PrefUtils;

/**
 * Created by david on 21.01.15.
 */
public class CorneliusBurghAddon {
    public static void runAddon(Context c) {
        TimetableDatabase db = new TimetableDatabase(c);

        initSchool(PrefUtils.getSchoolPref(c));
        addTimeTable(db);
        addTeachers(c, db);

        db.clearCache();
    }

    public static void addTimeTable(TimetableDatabase db) {
        TimeUnit t = new TimeUnit(-1);

        t.setStartTime(7, 30);
        t.setEndTime(8, 15);
        t.setBreak(false);
        db.insertDatabaseEntryForId(t);

        t.setStartTime(8, 15);
        t.setEndTime(9, 0);
        t.setBreak(false);
        db.insertDatabaseEntryForId(t);

        t.setStartTime(9, 5);
        t.setEndTime(9, 50);
        t.setBreak(false);
        db.insertDatabaseEntryForId(t);

        t.setStartTime(9, 50);
        t.setEndTime(10, 15);
        t.setBreak(true);
        db.insertDatabaseEntryForId(t);

        t.setStartTime(10, 15);
        t.setEndTime(11, 0);
        t.setBreak(false);
        db.insertDatabaseEntryForId(t);

        t.setStartTime(11, 0);
        t.setEndTime(11, 45);
        t.setBreak(false);
        db.insertDatabaseEntryForId(t);

        t.setStartTime(11, 50);
        t.setEndTime(12, 35);
        t.setBreak(false);
        db.insertDatabaseEntryForId(t);

        t.setStartTime(12, 35);
        t.setEndTime(13, 35);
        t.setBreak(true);
        db.insertDatabaseEntryForId(t);

        t.setStartTime(13, 35);
        t.setEndTime(14, 20);
        t.setBreak(false);
        db.insertDatabaseEntryForId(t);

        t.setStartTime(14, 20);
        t.setEndTime(15, 5);
        t.setBreak(false);
        db.insertDatabaseEntryForId(t);
    }

    public static void initSchool(SharedPreferences pref) {
        pref.edit().putInt("school_id", Addons.Ids.ID_CORNELIUS_BURGH_GYMNASIUM_HEINSBERG).apply();
    }

    public static void addTeachers(Context c, TimetableDatabase db) {
        String[] teachersList = c.getResources().getStringArray(R.array.addon_array_cbge_teachers);

        int[] columnOrder = new int[] {
                Addons.INDEX_LASTNAME,
                Addons.INDEX_PREFIX,
                Addons.INDEX_FIRSTNAME,
        };

        Addons.insertTeachers(teachersList, columnOrder, db);
    }
}
