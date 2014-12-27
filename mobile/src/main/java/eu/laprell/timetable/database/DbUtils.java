package eu.laprell.timetable.database;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import eu.laprell.timetable.BackgroundService;

/**
 * Created by david on 05.12.14
 */
public class DbUtils {

    public static void saveDayAsync(Day d, Context c, TaskFinishedCallback cb) {
        new DaySaverTask(c, cb, d).execute();
    }

    private static class DaySaverTask extends BasicTask {
        private Day mDay;

        private DaySaverTask(Context mContext, TaskFinishedCallback mCallback, Day mDay) {
            super(mContext, mCallback);
            this.mDay = mDay;
        }

        @Override
        protected void doInBackground(TimetableDatabase db) {
            db.updateDatabaseEntry(mDay);
        }
    }

    private abstract static class BasicTask extends AsyncTask<Void, Void, Void> {
        @SuppressWarnings("unused")
        private BasicTask mNextTask;
        private Context mContext;
        private TaskFinishedCallback mCallback;

        public BasicTask(Context mContext, TaskFinishedCallback mCallback) {
            this.mContext = mContext;
            this.mCallback = mCallback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            TimetableDatabase db;
            boolean fromService = true;

            try {
                db = BackgroundService.get().getTimetableDatabase();
            } catch (Exception ex) {
                Log.w("Timetable", "Failed to obtain Db from BackgroundService");

                fromService = false;
                db = new TimetableDatabase(mContext);
            }

            doInBackground(db);

            if(!fromService)
                db.clearCache();

            if(mNextTask != null)
                mNextTask.doInBackground(db);

            return null;
        }

        protected abstract void doInBackground(TimetableDatabase db);

        @Override
        protected final void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            onPostExecute();

            if(mCallback != null) {
                mCallback.onFinished();
            }

            if(mNextTask != null)
                mNextTask.onPostExecute(null);
        }

        protected void onPostExecute() {

        }
    }

    public interface TaskFinishedCallback {
        public void onFinished();
    }
}
