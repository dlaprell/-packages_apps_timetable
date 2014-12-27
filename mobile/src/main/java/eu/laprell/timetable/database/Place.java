package eu.laprell.timetable.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import static eu.laprell.timetable.database.AbsTimetableDatabase.PlaceEntry.COLUMN_NAME_TITLE;
import static eu.laprell.timetable.database.AbsTimetableDatabase.PlaceEntry.TABLE_NAME;

/**
 * Created by david on 24.11.14.
 */
public class Place extends DatabaseEntry<Place> implements Parcelable {
    private String mTitle;

    public Place(long mId) {
        super(mId);
    }

    public Place(long id, Place p) {
        super(id, p);
        mTitle = p.mTitle;
    }

    @Override
    public Place makeCopy(long newId) {
        return new Place(newId, this);
    }

    @Override
    public String getTable() {
        return TABLE_NAME;
    }

    @Override
    protected void inContentValues(ContentValues v) {
        v.put(COLUMN_NAME_TITLE, getTitle());
    }

    @Override
    protected void generateFromCursor(@NonNull Cursor c) {
        setTitle(c.getString(c.getColumnIndex(COLUMN_NAME_TITLE)));
    }

    @Override
    public String getDefaultSortOrder() {
        return COLUMN_NAME_TITLE + " ASC";
    }

    @Override
    protected String[] geDefaultProjection() {
        return new String[] {
                COLUMN_NAME_TITLE,
        };
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    protected Place(Parcel in) {
        super(in.readLong());
        fromParcel(in);

        mTitle = in.readString();
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
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };
}
