package eu.laprell.timetable.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import eu.laprell.timetable.R;
import eu.laprell.timetable.utils.ArrayUtils;

import static eu.laprell.timetable.database.AbsTimetableDatabase.DayEntry.COLUMN_NAME_DAY_OF_WEEK;
import static eu.laprell.timetable.database.AbsTimetableDatabase.DayEntry.COLUMN_NAME_LESSONS;
import static eu.laprell.timetable.database.AbsTimetableDatabase.DayEntry.COLUMN_NAME_PLACES;
import static eu.laprell.timetable.database.AbsTimetableDatabase.DayEntry.COLUMN_NAME_TIMES;
import static eu.laprell.timetable.database.AbsTimetableDatabase.DayEntry.TABLE_NAME;

/**
 * Class representing a Day in a Timetable
 */
public class Day extends DatabaseEntry<Day> implements Parcelable {

    /**
     * All Days of the Week
     */
    public abstract class OF_WEEK {
        public static final int MONDAY = 1;
        public static final int TUESDAY = 2;
        public static final int WEDNESDAY = 3;
        public static final int THURSDAY = 4;
        public static final int FRIDAY = 5;
        public static final int SATURDAY = 6;
        public static final int SUNDAY = 7;
    }

    /**
     * Value for no lesson set
     */
    public static final int NO_LESSON = -1;

    private int mDayOfWeek;
    private long[] mLessons;
    private long[] mTimeUnits;
    private long[] mPlaces;

    /**
     * Constructs a new Day with the given id
     * @param mId the id to assign
     */
    public Day(long mId) {
        super(mId);
    }

    /**
     * Constructs a new Day with the given id and day of week
     * @param mId the id to assign
     * @param mDayOfWeek the day to assign
     */
    public Day(long mId, int mDayOfWeek) {
        super(mId);

        this.mDayOfWeek = mDayOfWeek;
    }

    /**
     * Copy cosntructor, but with given id
     * @param id the id to assign
     * @param d the Day to copy the other values from
     */
    public Day(long id, Day d) {
        super(id, d);

        mDayOfWeek = d.mDayOfWeek;
        mLessons = d.mLessons;
        mPlaces = d.mPlaces;
        mTimeUnits = d.mTimeUnits;

        checkIdArrays();
    }

    @Override
    public Day makeCopy(long newId) {
        return new Day(newId, this);
    }

    private void checkIdArrays() {
        if(mLessons == null)
            mLessons = new long[0];

        if(mTimeUnits == null)
            mTimeUnits = new long[0];

        if(mPlaces == null)
            mPlaces = new long[0];
    }

    public long[] getTimeUnits() {
        return mTimeUnits;
    }

    public void setTimeUnits(long[] mTimeUnits) {
        this.mTimeUnits = mTimeUnits;

        checkIdArrays();
    }

    public int getDayOfWeek() {
        return mDayOfWeek;
    }

    public void setDayOfWeek(int mDayOfWeek) {
        this.mDayOfWeek = mDayOfWeek;
    }

    public long[] getLessons() {
        return mLessons;
    }

    public void setLessons(long[] mLessons) {
        this.mLessons = mLessons;

        checkIdArrays();
    }

    @Override
    public String getTable() {
        return TABLE_NAME;
    }

    @Override
    protected void inContentValues(ContentValues v) {
        v.put(COLUMN_NAME_DAY_OF_WEEK, getDayOfWeek());
        v.put(COLUMN_NAME_LESSONS, ArrayUtils.implode(";", getLessons()));
        v.put(COLUMN_NAME_TIMES, ArrayUtils.implode(";", getTimeUnits()));
        v.put(COLUMN_NAME_PLACES, ArrayUtils.implode(";", getPlaces()));
    }

    @Override
    protected void generateFromCursor(@NonNull Cursor c) {
        setDayOfWeek(c.getInt(c.getColumnIndex(COLUMN_NAME_DAY_OF_WEEK)));
        long[] l = ArrayUtils.explodeLong(";", c.getString(
                c.getColumnIndex(COLUMN_NAME_LESSONS)));
        setLessons(l);

        l = ArrayUtils.explodeLong(";", c.getString(
                c.getColumnIndex(COLUMN_NAME_TIMES)));
        setTimeUnits(l);

        l = ArrayUtils.explodeLong(";", c.getString(
                c.getColumnIndex(COLUMN_NAME_PLACES)));
        setPlaces(l);
    }

    @Override
    protected String[] geDefaultProjection() {
        return new String[] {
                COLUMN_NAME_LESSONS,
                COLUMN_NAME_TIMES,
                COLUMN_NAME_PLACES,
                COLUMN_NAME_DAY_OF_WEEK
        };
    }

    public long[] getPlaces() {
        return mPlaces;
    }

    public void setPlaces(long[] mPlaces) {
        this.mPlaces = mPlaces;

        checkIdArrays();
    }

    public void expandIdArrays() {
        mLessons = ArrayUtils.expandByOne(mLessons);
        mTimeUnits = ArrayUtils.expandByOne(mTimeUnits);
        mPlaces = ArrayUtils.expandByOne(mPlaces);
    }

    public long getLessonIdAt(TimeUnit t) {
        for (int i = 0;i < mTimeUnits.length;i++) {
            if(mTimeUnits[i] == t.getId())
                return mLessons[i];
        }
        return -1;
    }

    public long getPlaceIdAt(TimeUnit t) {
        for (int i = 0;i < mTimeUnits.length;i++) {
            if(mTimeUnits[i] == t.getId())
                return mPlaces[i];
        }
        return -1;
    }

    protected Day(Parcel in) {
        super(in.readLong());
        fromParcel(in);

        mDayOfWeek = in.readInt();

        mLessons = new long[in.readInt()];
        in.readLongArray(mLessons);

        mPlaces = new long[in.readInt()];
        in.readLongArray(mPlaces);

        mTimeUnits = new long[in.readInt()];
        in.readLongArray(mTimeUnits);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(getId());
        inParcel(dest);

        dest.writeInt(mDayOfWeek);

        dest.writeInt(mLessons.length);
        dest.writeLongArray(mLessons);

        dest.writeInt(mPlaces.length);
        dest.writeLongArray(mPlaces);

        dest.writeInt(mTimeUnits.length);
        dest.writeLongArray(mTimeUnits);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Day> CREATOR = new Parcelable.Creator<Day>() {
        @Override
        public Day createFromParcel(Parcel in) {
            return new Day(in);
        }

        @Override
        public Day[] newArray(int size) {
            return new Day[size];
        }
    };

    public static String getString(int day, Context c) {
        return c.getResources().getStringArray(R.array.array_days)[day - 1];
    }
}
