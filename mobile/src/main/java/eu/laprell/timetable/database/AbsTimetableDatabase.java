package eu.laprell.timetable.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import eu.laprell.timetable.R;
import eu.laprell.timetable.addon.Addons;
import eu.laprell.timetable.utils.PrefUtils;

/**
 * Created by david on 09.11.14
 */
public class AbsTimetableDatabase {

    public static boolean isNoId(long id) {
        return id == 0 || id == -1;
    }

    public static final Lesson TYPE_LESSON = new Lesson(-1);
    public static final TimeUnit TYPE_TIMEUNIT = new TimeUnit(-1);
    public static final Day TYPE_DAY = new Day(-1);
    public static final Place TYPE_PLACE = new Place(-1);
    public static final Teacher TYPE_TEACHER = new Teacher(-1);
    public static final Task TYPE_TASK = new Task(-1);

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES_LESSONS =
            "CREATE TABLE " + LessonEntry.TABLE_NAME + " (" +
                    LessonEntry._ID + INT_TYPE + " PRIMARY KEY," +
                    LessonEntry.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                    LessonEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    LessonEntry.COLUMN_NAME_TIME + TEXT_TYPE + COMMA_SEP +
                    LessonEntry.COLUMN_NAME_TEACHER + TEXT_TYPE + COMMA_SEP +
                    LessonEntry.COLUMN_NAME_COLOR + INT_TYPE + COMMA_SEP +
                    LessonEntry.COLUMN_NAME_ID_NUMBER + TEXT_TYPE + COMMA_SEP +
                    DatabaseEntry.COLUMN_NAME_LAST_CHANGED + INT_TYPE + COMMA_SEP +
                    LessonEntry.COLUMN_NAME_ID_IMAGE + INT_TYPE +
            " )";
    private static final String SQL_ALTER1_ENTRIES_LESSONS =
            "ALTER TABLE "+ LessonEntry.TABLE_NAME +" ADD COLUMN " +
                    LessonEntry.COLUMN_NAME_ID_IMAGE + INT_TYPE +
            ";";
    private static final String SQL_ALTER2_ENTRIES_LESSONS =
            "ALTER TABLE "+ LessonEntry.TABLE_NAME +" ADD COLUMN " +
                    DatabaseEntry.COLUMN_NAME_LAST_CHANGED + INT_TYPE +
            ";";

    private static final String SQL_CREATE_ENTRIES_DAYS =
            "CREATE TABLE " + DayEntry.TABLE_NAME + " (" +
                    DayEntry._ID + INT_TYPE + " PRIMARY KEY," +
                    DayEntry.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                    DayEntry.COLUMN_NAME_LESSONS + TEXT_TYPE + COMMA_SEP +
                    DayEntry.COLUMN_NAME_TIMES + TEXT_TYPE + COMMA_SEP +
                    DayEntry.COLUMN_NAME_PLACES + TEXT_TYPE + COMMA_SEP +
                    DayEntry.COLUMN_NAME_CYCLE + INT_TYPE + COMMA_SEP +
                    DatabaseEntry.COLUMN_NAME_LAST_CHANGED + INT_TYPE + COMMA_SEP +
                    DayEntry.COLUMN_NAME_DAY_OF_WEEK + INT_TYPE +
            " )";
    private static final String SQL_ALTER2_ENTRIES_DAYS =
            "ALTER TABLE "+ DayEntry.TABLE_NAME +" ADD COLUMN " +
                    DatabaseEntry.COLUMN_NAME_LAST_CHANGED + INT_TYPE +
            ";";

    private static final String SQL_CREATE_ENTRIES_TIMEUNITS =
            "CREATE TABLE " + TimeUnitEntry.TABLE_NAME + " (" +
                    TimeUnitEntry._ID + INT_TYPE + " PRIMARY KEY," +
                    TimeUnitEntry.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                    TimeUnitEntry.COLUMN_NAME_START_TIME + INT_TYPE + COMMA_SEP +
                    TimeUnitEntry.COLUMN_NAME_BREAK + INT_TYPE + COMMA_SEP +
                    DatabaseEntry.COLUMN_NAME_LAST_CHANGED + INT_TYPE + COMMA_SEP +
                    TimeUnitEntry.COLUMN_NAME_END_TIME + INT_TYPE +
            " )";
    private static final String SQL_ALTER2_ENTRIES_TIMEUNITS =
            "ALTER TABLE "+ TimeUnitEntry.TABLE_NAME +" ADD COLUMN " +
                    DatabaseEntry.COLUMN_NAME_LAST_CHANGED + INT_TYPE +
                    ";";

    private static final String SQL_CREATE_ENTRIES_PLACES =
            "CREATE TABLE " + PlaceEntry.TABLE_NAME + " (" +
                    PlaceEntry._ID + INT_TYPE + " PRIMARY KEY," +
                    PlaceEntry.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                    DatabaseEntry.COLUMN_NAME_LAST_CHANGED + INT_TYPE + COMMA_SEP +
                    PlaceEntry.COLUMN_NAME_TITLE + TEXT_TYPE +
            " )";
    private static final String SQL_ALTER2_ENTRIES_PLACES =
            "ALTER TABLE "+ PlaceEntry.TABLE_NAME +" ADD COLUMN " +
                    DatabaseEntry.COLUMN_NAME_LAST_CHANGED + INT_TYPE +
                    ";";

    private static final String SQL_CREATE_ENTRIES_TASKS =
            "CREATE TABLE " + TaskEntry.TABLE_NAME + " (" +
                    TaskEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    TaskEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    TaskEntry.COLUMN_NAME_LESSON_ID + INT_TYPE + COMMA_SEP +
                    TaskEntry.COLUMN_NAME_TIME_ID + INT_TYPE + COMMA_SEP +
                    TaskEntry.COLUMN_NAME_TIME_EXACLTY + INT_TYPE + COMMA_SEP +
                    TaskEntry.COLUMN_NAME_DEADLINE + INT_TYPE + COMMA_SEP +
                    TaskEntry.COLUMN_NAME_READY + INT_TYPE + COMMA_SEP +
                    DatabaseEntry.COLUMN_NAME_LAST_CHANGED + INT_TYPE +
            ")";

    private static final String SQL_CREATE_ENTRIES_TEACHERS =
            "CREATE TABLE " + TeacherEntry.TABLE_NAME + " (" +
                    TeacherEntry._ID + INT_TYPE + " PRIMARY KEY," +
                    TeacherEntry.COLUMN_NAME_FIRST_NAME + TEXT_TYPE + COMMA_SEP +
                    TeacherEntry.COLUMN_NAME_SECOND_NAME + TEXT_TYPE + COMMA_SEP +
                    TeacherEntry.COLUMN_NAME_PREFIX + INT_TYPE + COMMA_SEP +
                    DatabaseEntry.COLUMN_NAME_LAST_CHANGED + INT_TYPE +
                    ")";

    private Context mContext;
    private TableDbHelper mHelper;

    private final Object mRLock = new Object();
    private final Object mWLock = new Object();

    private SQLiteDatabase mWriteable, mReadable;

    public AbsTimetableDatabase(Context c) {
        mContext = c;

        mHelper = new TableDbHelper(c);
    }

    /**
     * Private helper function to get a readable {@link android.database.sqlite.SQLiteDatabase}
     * This method returns a cached version, so make sure to use the mRLock when using the Database
     *
     * @see #mRLock
     *
     * @return a {@link android.database.sqlite.SQLiteDatabase}
     */
    private @NonNull SQLiteDatabase getR() {
        if(mReadable == null)
            mReadable = mHelper.getReadableDatabase();
        return mReadable;
    }

    /**
     * Private helper function to get a writeable {@link android.database.sqlite.SQLiteDatabase}
     * This method returns a cached version, so make sure to use the mWLock when using the Database
     *
     * @see #mWLock
     *
     * @return a {@link android.database.sqlite.SQLiteDatabase}
     */
    private @NonNull SQLiteDatabase getW() {
        if(mWriteable == null)
            mWriteable = mHelper.getWritableDatabase();
        return mWriteable;
    }

    /**
     * This method will clear the Database cache. It will
     * close all cached Databases used by this class. <br>
     * Since it uses the cached Databases this method can
     * take long, so try to avoid calling it from the UI-Thread
     */
    public void clearCache() {
        synchronized (mWLock) {
            if (mWriteable != null)
                mWriteable.close();
            mWriteable = null;
        }

        synchronized (mRLock) {
            if (mReadable != null)
                mReadable.close();
            mReadable = null;
        }
    }

    /**
     * Checks if the parameter is one of the defined DatabaseEntry Constants
     *
     * @see #TYPE_PLACE
     * @see #TYPE_TIMEUNIT
     * @see #TYPE_LESSON
     * @see #TYPE_DAY
     * @see #TYPE_TASK
     * @see #TYPE_TEACHER
     *
     * @param e a arbitrary DatabaseEntry
     * @return true if it is, false if not
     */
    private boolean isOneOfTypes(DatabaseEntry e) {
        return (e == TYPE_DAY
            || e == TYPE_LESSON
            || e == TYPE_PLACE
            || e == TYPE_TIMEUNIT
            || e == TYPE_TASK
            || e == TYPE_TEACHER
        );
    }

    /**
     * Queries a DatabaseEntry from the {@link eu.laprell.timetable.database.DatabaseEntry#getTable()} table by
     * the given id
     *
     * @param type one of the type constants
     * @param id the id to query
     * @return a DatabaseEntry instance of the given type
     */
    public DatabaseEntry getDatabaseEntryById(DatabaseEntry type, long id) {
        return queryDatabaseEntry(type, BaseColumns._ID + " LIKE ?", new String[] { String.valueOf(id) });
    }

    public DatabaseEntry queryDatabaseEntry(DatabaseEntry type, String selection, String[] selectionArgs) {
        if(!isOneOfTypes(type))
            throw new RuntimeException("Use one of the types!");

        DatabaseEntry res = type.makeCopy(-1);

        String[] projection = res.getProjection();

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                res.getDefaultSortOrder();

        Cursor c;

        synchronized (mRLock) {
            c = getR().query(
                    res.getTable(),  // The table to query
                    projection,      // The columns to return
                    selection,       // The columns for the WHERE clause
                    selectionArgs,   // The values for the WHERE clause
                    null,            // don't group the rows
                    null,            // don't filter by row groups
                    sortOrder        // The sort order
            );
        }

        if(c.getCount() != 1) {
            c.close();
            return null;
        }
        c.moveToFirst();

        res.inflateFromCursor(c);

        c.close();

        return res;
    }

    /**
     * Will insert the given DatabaseEntry and return a new instance of the
     * same type, but with the new {@link eu.laprell.timetable.database.DatabaseEntry#getId()}
     * @param e the DatabaseEntry to insert
     * @return a new DatabaseEntry instance
     */
    public DatabaseEntry insertDatabaseEntryForId(DatabaseEntry e) {
        // Create a new map of values, where column names are the keys
        ContentValues values = e.convertToContentValues();

        // Insert the new row, returning the primary key value of the new row
        long newRowId;

        synchronized (mWLock) {
            newRowId = getW().insert(
                    e.getTable(),
                    e.getNullable(),
                    values);
        }

        return e.makeCopy(newRowId);
    }

    /**
     * Will update the given DatabaseEntry
     * @param e the DatabaseEntry to update
     * @return the number of entries that was affected
     */
    public int updateDatabaseEntry(DatabaseEntry e) {
        // Create a new map of values, where column names are the keys
        ContentValues values = e.convertToContentValues();

        // Which row to update, based on the ID
        String selection = BaseColumns._ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(e.getId()) };

        int res;

        synchronized (mWLock) {
            res = getW().update(
                    e.getTable(),
                    values,
                    selection,
                    selectionArgs);
        }

        return res;
    }

    /**
     * Queries a array of ids from the Database from the given type
     * using the {@link eu.laprell.timetable.database.DatabaseEntry#getTable()} and {@link eu.laprell.timetable.database.DatabaseEntry#getDefaultSortOrder()}
     *
     * @param type one of the type constants
     *
     * @return a array with the queried ids
     */
    public long[] getDatabaseEntries(DatabaseEntry type) {
        return queryDatabaseEntries(type, null, null);
    }

    public long[] queryDatabaseEntries(DatabaseEntry type, @Nullable String selection,
                                       String[] selectionArgs) {
        return queryDatabaseEntries(type, selection, selectionArgs, null);
    }

    public long[] queryDatabaseEntries(DatabaseEntry type, @Nullable String selection,
                                       String[] selectionArgs, String sortOrder) {
        if (!isOneOfTypes(type))
            throw new RuntimeException("Use one of the types.");

        String[] projection = {
                BaseColumns._ID,
        };

        // How you want the results sorted in the resulting Cursor
        if(sortOrder == null)
            sortOrder = type.getDefaultSortOrder();

        Cursor c;

        synchronized (mRLock) {
            c = getR().query(
                    type.getTable(),  // The table to query
                    projection,                               // The columns to return
                    selection,                                // The columns for the WHERE clause
                    selectionArgs,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    sortOrder                                 // The sort order
            );
        }

        long[] res = new long[c.getCount()];

        c.moveToFirst();
        for (int i = 0;i < res.length;i++) {
            res[i] = c.getLong(c.getColumnIndex(BaseColumns._ID));
            c.moveToNext();
        }
        c.close();

        return res;
    }

    /**
     * Deletes a DatabaseEntry from its Table
     * @param e the DatabaseEntry to remove
     * @return the num of affected rows, should normally be 1
     */
    public int removeDatabaseEntry(DatabaseEntry e) {
        String selection = BaseColumns._ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(e.getId()) };
        String table = e.getTable();

        int res;

        synchronized (mWLock) {
            res = getW().delete(table, selection, selectionArgs);
        }

        return res;
    }

    protected Context getContext() {
        return mContext;
    }

    /* Inner class that defines the table contents */
    public static abstract class LessonEntry implements BaseColumns {
        public static final String TABLE_NAME = "lessons";
        public static final String COLUMN_NAME_ENTRY_ID = "entryid";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_COLOR = "color";
        public static final String COLUMN_NAME_TEACHER = "teacher";
        public static final String COLUMN_NAME_ID_NUMBER = "id_number";
        public static final String COLUMN_NAME_ID_IMAGE = "id_image";
        public static final String COLUMN_NAME_NULLABLE = null;
    }

    /* Inner class that defines the table contents */
    public static abstract class DayEntry implements BaseColumns {
        public static final String TABLE_NAME = "days";
        public static final String COLUMN_NAME_DAY_OF_WEEK = "day_of_week";
        public static final String COLUMN_NAME_LESSONS = "lessons";
        public static final String COLUMN_NAME_TIMES = "times";
        public static final String COLUMN_NAME_ENTRY_ID = "entryid";
        public static final String COLUMN_NAME_PLACES = "places";
        public static final String COLUMN_NAME_CYCLE = "cycle";
    }

    /* Inner class that defines the table contents */
    public static abstract class TimeUnitEntry implements BaseColumns {
        public static final String TABLE_NAME = "time_units";
        public static final String COLUMN_NAME_START_TIME = "start_time";
        public static final String COLUMN_NAME_END_TIME = "end_time";
        public static final String COLUMN_NAME_BREAK = "break";
        public static final String COLUMN_NAME_ENTRY_ID = "entryid";
    }

    public static abstract class PlaceEntry implements BaseColumns {
        public static final String TABLE_NAME = "places";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_ENTRY_ID = "entryid";
    }

    public static abstract class TaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "tasks";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_LESSON_ID = "lessonid";
        public static final String COLUMN_NAME_TIME_ID = "timeid";
        public static final String COLUMN_NAME_TIME_EXACLTY = "time_exactly";
        public static final String COLUMN_NAME_DEADLINE = "deadline";
        public static final String COLUMN_NAME_READY = "ready";
    }

    public static abstract class TeacherEntry implements BaseColumns {
        public static final String TABLE_NAME = "teachers";
        public static final String COLUMN_NAME_PREFIX = "prefix";
        public static final String COLUMN_NAME_FIRST_NAME = "first_name";
        public static final String COLUMN_NAME_SECOND_NAME = "second_name";
    }

    private class TableDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 5;
        public static final String DATABASE_NAME = "Timetable.db";

        public TableDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES_LESSONS);
            db.execSQL(SQL_CREATE_ENTRIES_DAYS);
            db.execSQL(SQL_CREATE_ENTRIES_TIMEUNITS);
            db.execSQL(SQL_CREATE_ENTRIES_PLACES);
            db.execSQL(SQL_CREATE_ENTRIES_TASKS);
            db.execSQL(SQL_CREATE_ENTRIES_TEACHERS);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            if (oldVersion < 2 && newVersion >= 2) {
                db.execSQL(SQL_ALTER1_ENTRIES_LESSONS);
            }

            if (oldVersion < 3 && newVersion >= 3) {
                db.execSQL(SQL_ALTER2_ENTRIES_DAYS);
                db.execSQL(SQL_ALTER2_ENTRIES_LESSONS);
                db.execSQL(SQL_ALTER2_ENTRIES_TIMEUNITS);
                db.execSQL(SQL_ALTER2_ENTRIES_PLACES);
            }

            if (oldVersion < 4 && newVersion >= 4) {
                db.execSQL(SQL_CREATE_ENTRIES_TASKS);
            }

            if (oldVersion < 5 && newVersion >= 5) {
                db.execSQL(SQL_CREATE_ENTRIES_TEACHERS);

                // HACK: In versions prior to this we hardcoded the kgh.
                //       That means we have to to a compat saving if the
                //       Kgh was selected.
                if(PrefUtils.getSchoolPref(getContext()).getInt("school_id", 0)
                        == Addons.Ids.ID_KREISGYMNASIUM_HEINSBERG) {
                    makeKghTeacherListCompat(db);
                }
            }
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

        private void makeKghTeacherListCompat(SQLiteDatabase db) {
            final Context c = getContext();

            // Make sure nothing is left
            db.execSQL("DELETE FROM "+ TeacherEntry.TABLE_NAME);

            String[] teachersList = c.getResources().getStringArray(R.array.addon_array_kgh_teachers);

            // Me must try to match the earlier just "name" location to the new one
            mReadable = db;
            mWriteable = db;

            long[] lids = getDatabaseEntries(TYPE_LESSON);
            Lesson[] lessons = new Lesson[lids.length];
            String[] teacherNames = new String[lids.length];

            for (int i = 0;i < lids.length;i++) {
                lessons[i] = (Lesson)getDatabaseEntryById(TYPE_LESSON, lids[i]);

                Cursor cu = db.query(
                        TYPE_LESSON.getTable(),
                        new String[]{LessonEntry.COLUMN_NAME_TEACHER},
                        LessonEntry._ID + " LIKE ?",
                        new String[]{String.valueOf(lids[i])},
                        null,
                        null,
                        null
                );
                cu.moveToFirst();
                teacherNames[i] = cu.getString(cu.getColumnIndex(LessonEntry.COLUMN_NAME_TEACHER));
                cu.close();
            }

            Teacher t = new Teacher(-1);
            for (int i = 0;i < teachersList.length;i++) {
                String[] data = teachersList[i].split("\\|");

                t.setPrefix(data[0]); // Should be at least Herr
                t.setFirstName(data[1].length() == 0 ? null : data[1]);
                t.setSecondName(data[2]);

                long id = db.insert(TeacherEntry.TABLE_NAME, null, t.convertToContentValues());

                for (int j = 0;j < lids.length;j++) {
                    if(teacherNames[j].contains(data[0])
                            && teacherNames[j].contains(data[2])
                            && (data[1].length() == 0 || teacherNames[j].contains(data[1]))) {
                        lessons[j].setTeacherId(id);
                    }
                }
            }

            for (Lesson l : lessons) {
                updateDatabaseEntry(l);
            }

            mReadable = null; // Make sure to go back to starting point
            mWriteable = null;
        }
    }
}
