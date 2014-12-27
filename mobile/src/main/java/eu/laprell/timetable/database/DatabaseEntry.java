package eu.laprell.timetable.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

/**
 * Created by david on 10.12.14.
 */
public abstract class DatabaseEntry<T extends DatabaseEntry> {

    public static final String COLUMN_NAME_LAST_CHANGED = "last_changed";
    private static final String COLUMN_NAME_NULLABLE = null;

    private long mId;
    private long mLastChange;

    public DatabaseEntry(long l) {
        mId = l;
    }

    public DatabaseEntry(long l, T copy) {
        this(l);

        mLastChange = copy.getLastChange();
    }

    public abstract T makeCopy(long newId);

    public final long getId() {
        return mId;
    }

    public final long getLastChange() {
        return mLastChange;
    }

    public final void changed() {
        mLastChange = System.currentTimeMillis();
    }

    protected final void inParcel(Parcel p) {
        p.writeLong(mLastChange);
    }
    protected final void fromParcel(Parcel p) {
        mLastChange = p.readLong();
    }

    public abstract String getTable();

    protected abstract void inContentValues(@NonNull ContentValues v);
    public final ContentValues convertToContentValues() {
        ContentValues values = new ContentValues();

        inContentValues(values);

        // NEVER ever save the id in the contentvalues -> during update & insert
        // (-1 will replace autoincrement)
        if(values.containsKey(BaseColumns._ID))
            values.remove(BaseColumns._ID);

        values.put(COLUMN_NAME_LAST_CHANGED, getLastChange());

        return values;
    }

    protected abstract void generateFromCursor(@NonNull Cursor c);
    public final void inflateFromCursor(@NonNull Cursor c) {
        generateFromCursor(c);

        mId = c.getLong(c.getColumnIndexOrThrow(BaseColumns._ID));
        mLastChange = c.getLong(c.getColumnIndexOrThrow(COLUMN_NAME_LAST_CHANGED));
    }

    /**
     * Get the default projection to fetch from the db. Must always return the same value!
     * @return String array
     */
    protected abstract String[] geDefaultProjection();
    public final String[] getProjection() {
        String[] defaults = geDefaultProjection();

        String[] res = new String[defaults.length + 2];
        System.arraycopy(defaults, 0, res, 2, defaults.length);

        res[0] = BaseColumns._ID;
        res[1] = COLUMN_NAME_LAST_CHANGED;

        return res;
    }

    public String getNullable() {
        return COLUMN_NAME_NULLABLE;
    }

    public String getDefaultSortOrder() {
        return null;
    }
}
