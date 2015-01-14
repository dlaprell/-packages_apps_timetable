package eu.laprell.timetable.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import static eu.laprell.timetable.database.AbsTimetableDatabase.TeacherEntry.COLUMN_NAME_FIRST_NAME;
import static eu.laprell.timetable.database.AbsTimetableDatabase.TeacherEntry.COLUMN_NAME_PREFIX;
import static eu.laprell.timetable.database.AbsTimetableDatabase.TeacherEntry.COLUMN_NAME_SECOND_NAME;
import static eu.laprell.timetable.database.AbsTimetableDatabase.TeacherEntry.TABLE_NAME;

/**
 * Created by david on 24.11.14.
 */
@SuppressWarnings("UnusedDeclaration")
public class Teacher extends DatabaseEntry<Teacher> implements Parcelable {
    private String mFirstName;
    private String mSecondName;
    private String mPrefix;

    /**
     * Constructor for Teacher with an arbitrary Id
     * @param mId the id to assign
     */
    public Teacher(long mId) {
        super(mId);
    }

    /**
     * Copy constructor that will copy all values except the Id
     * @param id the new id to assign
     * @param t the Teacher to copy from
     */
    public Teacher(long id, Teacher t) {
        super(id, t);
        mFirstName = t.mFirstName;
        mSecondName = t.mSecondName;
        mPrefix = t.mPrefix;
    }

    /**
     * Method to invoke the {@link #Teacher(long, Teacher)} constructor with this
     * instance as parameter and the given new Id
     * @param newId the new id to assign to the returned Teacher object
     * @return the newly created teacher object
     */
    @Override
    public Teacher makeCopy(long newId) {
        return new Teacher(newId, this);
    }

    /**
     * Returns the name of the table in which a Teacher object gets saved.
     * @return the name of the table
     */
    @Override
    public String getTable() {
        return TABLE_NAME;
    }

    /**
     * Writes all values of this Teacher object into the supplied
     * ContentValues.
     * @param v the ContentValues to write the values into
     */
    @Override
    protected void inContentValues(@NonNull ContentValues v) {
        v.put(COLUMN_NAME_FIRST_NAME, mFirstName);
        v.put(COLUMN_NAME_SECOND_NAME, mSecondName);
        v.put(COLUMN_NAME_PREFIX, mPrefix);
    }

    /**
     * Extracts all values for this Teacher Object from the supplied Cursor
     * @param c the Cursor to inflate from
     */
    @Override
    protected void generateFromCursor(@NonNull Cursor c) {
        setFirstName(c.getString(c.getColumnIndex(COLUMN_NAME_FIRST_NAME)));
        setSecondName(c.getString(c.getColumnIndex(COLUMN_NAME_SECOND_NAME)));
        setPrefix(c.getString(c.getColumnIndex(COLUMN_NAME_PREFIX)));
    }

    /**
     * Returns the default sort order that should be used if querying Teacher Objects from
     * the Database.
     * @return the sort order as String
     */
    @Override
    public String getDefaultSortOrder() {
        return COLUMN_NAME_SECOND_NAME + " ASC";
    }

    /**
     * Returns the default projection that should be used if querying Teacher Objects from
     * the Database.
     * @return the default projection as String array
     */
    @Override
    protected String[] geDefaultProjection() {
        return new String[] {
                COLUMN_NAME_FIRST_NAME,
                COLUMN_NAME_SECOND_NAME,
                COLUMN_NAME_PREFIX
        };
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String mFirstName) {
        this.mFirstName = mFirstName;
    }

    public String getSecondName() {
        return mSecondName;
    }

    public void setSecondName(String mSecondName) {
        this.mSecondName = mSecondName;
    }

    public String getPrefix() {
        return mPrefix;
    }

    public void setPrefix(String mPrefix) {
        this.mPrefix = mPrefix;
    }

    public String getFullName() {
        String result = "";

        if(mPrefix != null) result += mPrefix + " ";
        if(mFirstName != null) result += mFirstName + " ";
        if(mSecondName != null) result += mSecondName;

        return result;
    }

    protected Teacher(Parcel in) {
        super(in.readLong());
        fromParcel(in);

        mPrefix = in.readString();
        mFirstName = in.readString();
        mSecondName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(getId());
        inParcel(dest);

        dest.writeString(mPrefix);
        dest.writeString(mFirstName);
        dest.writeString(mSecondName);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Teacher> CREATOR = new Parcelable.Creator<Teacher>() {
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