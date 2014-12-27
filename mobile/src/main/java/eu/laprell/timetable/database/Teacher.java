package eu.laprell.timetable.database;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by david on 24.11.14.
 */
public class Teacher implements Parcelable {
    private final long mId;
    private String mName;

    public Teacher(long mId) {
        this.mId = mId;
    }

    public Teacher(long id, Teacher p) {
        mId = id;
        mName = p.mName;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mTitle) {
        this.mName = mTitle;
    }

    public long getId() {
        return mId;
    }

    protected Teacher(Parcel in) {
        //fromParcel(in);

        mId = in.readLong();
        mName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mName);
    }

    @SuppressWarnings("unused")
    public static final Creator<Teacher> CREATOR = new Creator<Teacher>() {
        @Override
        public Teacher createFromParcel(Parcel in) {
            return new Teacher(in);
        }

        @Override
        public Teacher[] newArray(int size) {
            return new Teacher[size];
        }
    };
}
