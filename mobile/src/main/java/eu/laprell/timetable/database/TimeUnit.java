package eu.laprell.timetable.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import static eu.laprell.timetable.database.AbsTimetableDatabase.TimeUnitEntry.COLUMN_NAME_BREAK;
import static eu.laprell.timetable.database.AbsTimetableDatabase.TimeUnitEntry.COLUMN_NAME_END_TIME;
import static eu.laprell.timetable.database.AbsTimetableDatabase.TimeUnitEntry.COLUMN_NAME_START_TIME;
import static eu.laprell.timetable.database.AbsTimetableDatabase.TimeUnitEntry.TABLE_NAME;

/**
 * Created by david on 11.11.14.
 */
public class TimeUnit extends DatabaseEntry<TimeUnit> implements Parcelable {

    private int mStartTime;
    private int mEndTime;
    private boolean mBreak;

    public TimeUnit(long mId) {
        super(mId);
    }

    public TimeUnit(long mId, TimeUnit t) {
        super(mId, t);

        mStartTime = t.mStartTime;
        mEndTime = t.mEndTime;
        mBreak = t.mBreak;
    }

    @Override
    public TimeUnit makeCopy(long newId) {
        return new TimeUnit(newId, this);
    }

    public boolean isBreak() {
        return mBreak;
    }

    public void setBreak(boolean mBreak) {
        this.mBreak = mBreak;
    }

    public int getStartTime() {
        return mStartTime;
    }

    public void setStartTime(int mStartTime) {
        this.mStartTime = mStartTime;
    }

    public int getEndTime() {
        return mEndTime;
    }

    public void setEndTime(int mEndTime) {
        this.mEndTime = mEndTime;
    }

    @Override
    public String getTable() {
        return TABLE_NAME;
    }

    @Override
    protected void inContentValues(ContentValues v) {
        v.put(COLUMN_NAME_BREAK, isBreak() ? 1 : 0);
        v.put(COLUMN_NAME_START_TIME, getStartTime());
        v.put(COLUMN_NAME_END_TIME, getEndTime());
    }

    @Override
    protected void generateFromCursor(@NonNull Cursor c) {
        setBreak(c.getInt(c.getColumnIndex(COLUMN_NAME_BREAK)) == 1);
        setStartTime(c.getInt(c.getColumnIndex(COLUMN_NAME_START_TIME)));
        setEndTime(c.getInt(c.getColumnIndex(COLUMN_NAME_END_TIME)));
    }

    @Override
    protected String[] geDefaultProjection() {
        return new String[] {
                COLUMN_NAME_BREAK,
                COLUMN_NAME_START_TIME,
                COLUMN_NAME_END_TIME,
        };
    }

    public void setStartTime(int hour, int minutes) {
        setStartTime(60 * hour + minutes);
    }

    public void setEndTime(int hour, int minutes) {
        setEndTime(60 * hour + minutes);
    }

    public int getDuration() {
        return mEndTime - mStartTime;
    }

    public String getTimeAsString() {
        return makeTimeString("s - e");
    }

    public String makeTimeString(String format) {
        int m = mStartTime % 60;
        int h = (mStartTime / 60);

        int em = mEndTime % 60;
        int eh = (mEndTime / 60);

        String result = format.replaceAll("s", h + ":" + String.format("%02d", m));
        result = result.replaceAll("e", eh + ":" + String.format("%02d", em));

        return result;
    }

    public boolean isBefore(TimeUnit t) {
        return mStartTime < t.getStartTime();
    }

    public boolean isBefore(long t) {
        return mStartTime < t;
    }

    public boolean isAfter(TimeUnit t) {
        return mStartTime > t.getStartTime();
    }

    public boolean isAfter(long t) {
        return mStartTime > t;
    }

    public boolean isMatching(TimeUnit t) {
        return t.getStartTime() == mStartTime;
    }

    @Override
    public String toString() {
        return "TimeUnit{" +
                "mStartTime=" + mStartTime +
                ", mEndTime=" + mEndTime +
                ", mBreak=" + mBreak +
                ", getId()='" + getId() + '\'' +
                ", toNormalTime" + makeTimeString("{s:e}") +
                '}';
    }

    protected TimeUnit(Parcel in) {
        this(in.readLong());
        fromParcel(in);

        mStartTime = in.readInt();
        mEndTime = in.readInt();
        mBreak = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(getId());
        inParcel(dest);

        dest.writeInt(mStartTime);
        dest.writeInt(mEndTime);
        dest.writeByte((byte) (mBreak ? 0x01 : 0x00));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeUnit timeUnit = (TimeUnit) o;

        if (mBreak != timeUnit.mBreak) return false;
        if (mEndTime != timeUnit.mEndTime) return false;
        if (mStartTime != timeUnit.mStartTime) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mStartTime;
        result = 31 * result + mEndTime;
        result = 31 * result + (mBreak ? 1 : 0);
        return result;
    }

    @Override
    public String getDefaultSortOrder() {
        return COLUMN_NAME_START_TIME + " ASC";
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TimeUnit> CREATOR = new Parcelable.Creator<TimeUnit>() {
        @Override
        public TimeUnit createFromParcel(Parcel in) {
            return new TimeUnit(in);
        }

        @Override
        public TimeUnit[] newArray(int size) {
            return new TimeUnit[size];
        }
    };
}