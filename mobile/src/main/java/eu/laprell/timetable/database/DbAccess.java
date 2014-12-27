package eu.laprell.timetable.database;

import android.content.Context;

import eu.laprell.timetable.BackgroundService;

/**
 * Created by david on 11.12.14.
 */
public class DbAccess {
    private Context mContext;
    private TimetableDatabase mDb;
    private boolean mFromService;

    public DbAccess(Context c) {
        mContext = c;
    }

    public TimetableDatabase get() {
        if(mDb == null) {
            try {
                mDb = BackgroundService.get().getTimetableDatabase();
                mFromService = true;
            } catch (Exception ex) {
                ex.printStackTrace();

                mDb = new TimetableDatabase(mContext);
                mFromService = false;
            }
        }

        return mDb;
    }

    public void close() {
        if(!mFromService && mDb != null)
            mDb.clearCache();

        mContext = null;
        mDb = null;
    }
}
