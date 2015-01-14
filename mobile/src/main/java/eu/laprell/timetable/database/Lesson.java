package eu.laprell.timetable.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import static eu.laprell.timetable.database.AbsTimetableDatabase.LessonEntry.COLUMN_NAME_COLOR;
import static eu.laprell.timetable.database.AbsTimetableDatabase.LessonEntry.COLUMN_NAME_ID_IMAGE;
import static eu.laprell.timetable.database.AbsTimetableDatabase.LessonEntry.COLUMN_NAME_ID_NUMBER;
import static eu.laprell.timetable.database.AbsTimetableDatabase.LessonEntry.COLUMN_NAME_TEACHER;
import static eu.laprell.timetable.database.AbsTimetableDatabase.LessonEntry.COLUMN_NAME_TITLE;
import static eu.laprell.timetable.database.AbsTimetableDatabase.LessonEntry.TABLE_NAME;

/**
 * Created by david on 09.11.14.
 */
public class Lesson extends DatabaseEntry<Lesson> implements Parcelable {

    private long mTeacherId = -1;
    private String mIdNumber;
    private String mTitle;
    private int mTime;
    private int mColor;
    private int mImageId = -1;

    public Lesson(long id) {
        super(id);
    }

    public Lesson(long mId, Lesson l) {
        super(mId, l);

        this.mTeacherId = l.mTeacherId;
        this.mIdNumber = l.mIdNumber;
        this.mTitle = l.mTitle;
        this.mTime = l.mTime;
        this.mColor = l.mColor;
        this.mImageId = l.mImageId;
    }

    @Override
    public Lesson makeCopy(long newId) {
        return new Lesson(newId, this);
    }

    public int getImageId() {
        return mImageId;
    }

    public void setImageId(int mImageId) {
        this.mImageId = mImageId;
    }

    public long getTeacherId() {
        return mTeacherId;
    }

    public void setTeacherId(long teacherId) {
        this.mTeacherId = teacherId;
    }

    public String getIdNumber() {
        return mIdNumber;
    }

    public void setIdNumber(String mIdNumber) {
        this.mIdNumber = mIdNumber;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    @Override
    public String getTable() {
        return TABLE_NAME;
    }

    @Override
    protected void inContentValues(ContentValues v) {
        v.put(COLUMN_NAME_TITLE, getTitle());
        v.put(COLUMN_NAME_TEACHER, String.valueOf(getTeacherId()));
        v.put(COLUMN_NAME_ID_NUMBER, getIdNumber());
        v.put(COLUMN_NAME_COLOR, getColor());
        v.put(COLUMN_NAME_ID_IMAGE, getImageId());
    }

    @Override
    protected void generateFromCursor(@NonNull Cursor c) {
        setIdNumber(c.getString(c.getColumnIndex(COLUMN_NAME_ID_NUMBER)));
        setTitle(c.getString(c.getColumnIndex(COLUMN_NAME_TITLE)));
        setColor(c.getInt(c.getColumnIndex(COLUMN_NAME_COLOR)));
        setImageId(c.getInt(c.getColumnIndex(COLUMN_NAME_ID_IMAGE)));

        long tId = -1;
        try {
            String num = c.getString(c.getColumnIndex(COLUMN_NAME_TEACHER));
            tId = Long.parseLong(num);
        } catch (NumberFormatException ignored) {
            // Not much we can do here ...
        }

        mTeacherId = tId;
    }

    @Override
    protected String[] geDefaultProjection() {
        return new String[]{
                COLUMN_NAME_TITLE,
                COLUMN_NAME_TEACHER,
                COLUMN_NAME_ID_NUMBER,
                COLUMN_NAME_COLOR,
                COLUMN_NAME_ID_IMAGE,
        };
    }

    public int getTime() {
        return mTime;
    }

    public void setTime(int mTime) {
        this.mTime = mTime;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int mColor) {
        this.mColor = mColor | 0xFF000000;
    }

    public boolean hasColor() {
        return (mColor & 0x00FFFFFF) != 0;
    }

    protected Lesson(Parcel in) {
        super(in.readLong());
        fromParcel(in);

        mTeacherId = in.readLong();
        mIdNumber = in.readString();
        mTitle = in.readString();
        mTime = in.readInt();
        mColor = in.readInt();
        mImageId = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(getId());
        inParcel(dest);

        dest.writeLong(mTeacherId);
        dest.writeString(mIdNumber);
        dest.writeString(mTitle);
        dest.writeInt(mTime);
        dest.writeInt(mColor);
        dest.writeInt(mImageId);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Lesson> CREATOR = new Parcelable.Creator<Lesson>() {
        @Override
        public Lesson createFromParcel(Parcel in) {
            return new Lesson(in);
        }

        @Override
        public Lesson[] newArray(int size) {
            return new Lesson[size];
        }
    };
}
