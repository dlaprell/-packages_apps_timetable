package eu.laprell.timetable.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import static eu.laprell.timetable.database.AbsTimetableDatabase.TaskEntry.COLUMN_NAME_DEADLINE;
import static eu.laprell.timetable.database.AbsTimetableDatabase.TaskEntry.COLUMN_NAME_DESCRIPTION;
import static eu.laprell.timetable.database.AbsTimetableDatabase.TaskEntry.COLUMN_NAME_LESSON_ID;
import static eu.laprell.timetable.database.AbsTimetableDatabase.TaskEntry.COLUMN_NAME_READY;
import static eu.laprell.timetable.database.AbsTimetableDatabase.TaskEntry.COLUMN_NAME_TIME_EXACLTY;
import static eu.laprell.timetable.database.AbsTimetableDatabase.TaskEntry.COLUMN_NAME_TIME_ID;
import static eu.laprell.timetable.database.AbsTimetableDatabase.TaskEntry.COLUMN_NAME_TITLE;
import static eu.laprell.timetable.database.AbsTimetableDatabase.TaskEntry.TABLE_NAME;

/**
 * Created by david on 15.12.14
 */
public class Task extends DatabaseEntry<Task> implements Parcelable {

    private String mTitle;
    private String mDescription;
    private long mLessonId;
    private long mTimeUnitId;
    private long mTime;
    private long mDeadline;
    private boolean mReady;

    public Task(long l) {
        super(l);
    }

    public Task(long l, Task copy) {
        super(l, copy);

        mTitle = copy.mTitle;
        mDescription = copy.mDescription;
        mLessonId = copy.mLessonId;
        mTimeUnitId = copy.mTimeUnitId;
        mTime = copy.mTime;
        mDeadline = copy.mDeadline;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public long getLessonId() {
        return mLessonId;
    }

    public void setLessonId(long mLessonId) {
        this.mLessonId = mLessonId;
    }

    public long getTimeUnitId() {
        return mTimeUnitId;
    }

    public void setTimeUnitId(long mTimeUnitId) {
        this.mTimeUnitId = mTimeUnitId;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long mTime) {
        this.mTime = mTime;
    }

    public long getDeadline() {
        return mDeadline;
    }

    public void setDeadline(long mDeadline) {
        this.mDeadline = mDeadline;
    }

    @Override
    public Task makeCopy(long newId) {
        return new Task(newId, this);
    }

    @Override
    public String getTable() {
        return TABLE_NAME;
    }

    @Override
    protected void inContentValues(ContentValues v) {
        v.put(COLUMN_NAME_TITLE, mTitle);
        v.put(COLUMN_NAME_DESCRIPTION, mDescription);
        v.put(COLUMN_NAME_LESSON_ID, mLessonId);
        v.put(COLUMN_NAME_TIME_EXACLTY, mTime);
        v.put(COLUMN_NAME_TIME_ID, mTimeUnitId);
        v.put(COLUMN_NAME_READY, mReady ? 1 : 0);
    }

    @Override
    protected void generateFromCursor(@NonNull Cursor c) {
        mTitle = c.getString(c.getColumnIndex(COLUMN_NAME_TITLE));
        mDeadline = c.getLong(c.getColumnIndex(COLUMN_NAME_DEADLINE));
        mDescription = c.getString(c.getColumnIndex(COLUMN_NAME_DESCRIPTION));
        mTime = c.getLong(c.getColumnIndex(COLUMN_NAME_TIME_EXACLTY));
        mTimeUnitId = c.getLong(c.getColumnIndex(COLUMN_NAME_TIME_ID));
        mReady = c.getInt(c.getColumnIndex(COLUMN_NAME_READY)) == 1;
    }

    @Override
    protected String[] geDefaultProjection() {
        return new String[] {
                COLUMN_NAME_DESCRIPTION,
                COLUMN_NAME_TIME_ID,
                COLUMN_NAME_LESSON_ID,
                COLUMN_NAME_TIME_EXACLTY,
                COLUMN_NAME_DEADLINE,
                COLUMN_NAME_READY
        };
    }

    protected Task(Parcel in) {
        super(in.readLong());
        fromParcel(in);

        mTitle = in.readString();
        mDescription = in.readString();
        mLessonId = in.readLong();
        mTimeUnitId = in.readLong();
        mTime = in.readLong();
        mDeadline = in.readLong();
        mReady = in.readInt() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(getId());
        inParcel(dest);

        dest.writeString(mTitle);
        dest.writeString(mDescription);
        dest.writeLong(mLessonId);
        dest.writeLong(mTimeUnitId);
        dest.writeLong(mTime);
        dest.writeLong(mDeadline);
        dest.writeInt(mReady ? 1 : 0);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    public boolean isReady() {
        return mReady;
    }

    public void setReady(boolean mReady) {
        this.mReady = mReady;
    }
}
