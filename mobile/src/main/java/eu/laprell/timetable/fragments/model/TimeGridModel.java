package eu.laprell.timetable.fragments.model;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.github.adnansm.timelytextview.TimelyView;

import java.util.ArrayList;

import eu.laprell.timetable.animation.WaveAnimator;
import eu.laprell.timetable.background.Logger;
import eu.laprell.timetable.database.Day;
import eu.laprell.timetable.database.DbAccess;
import eu.laprell.timetable.database.TimeUnit;
import eu.laprell.timetable.database.TimetableDatabase;
import eu.laprell.timetable.fragments.BaseFragment;
import eu.laprell.timetable.utils.ArrayUtils;

/**
 * Created by david on 22.01.15.
 */
public class TimeGridModel extends AbsModel {

    public interface TimeGridVisualizer {
        public View prepareViewFromData(Data d);
        public void animateNewViewIn(Data d, int pos);
        public WaveAnimator buildWaveAnimator();
        public void reloadedTable(@Nullable WaveAnimator a);
        public void finishedDeletingTimeUnit(int pos);
    }

    private ArrayList<Data> mList;
    private SavingAsyncTask mTask;

    private TimeGridVisualizer mVisual;

    public TimeGridModel(BaseFragment f) {
        super(f);

        mList = new ArrayList<>();
        mTask = new SavingAsyncTask();
        mVisual = (TimeGridVisualizer)f;
    }

    public void addNewTime() {
        Data d = new Data();
        Data pre = mList.get(mList.size() - 1);

        TimeUnit t = new TimeUnit(-1);

        if(pre != null) {
            int time = pre.time.getStartTime() < pre.time.getEndTime()
                    ? pre.time.getEndTime() : pre.time.getStartTime();

            t.setStartTime(time);
            t.setEndTime(time + 1);
        }

        d.time = t;

        mList.add(d);
        updateAllNums(-3); // The newly created timeunit has an id of -1! So do not skip it

        int pos = mList.size() - 1;

        mVisual.prepareViewFromData(d);
        mVisual.animateNewViewIn(d, pos);

        mTask.doExecute(d);
    }

    public void reloadTableAsync() {
        new AsyncTask<Void, Void, WaveAnimator>() {
            @Override
            protected WaveAnimator doInBackground(Void... params) {
                DbAccess access = new DbAccess(getContext());
                TimetableDatabase db = access.get();

                long[] tids = db.getDatabaseEntries(TimetableDatabase.TYPE_TIMEUNIT);

                int num = 1;
                for (int i = 0;i < tids.length;i++) {
                    long tid = tids[i];

                    Data d = new Data();
                    d.time = (TimeUnit) db.getDatabaseEntryById(TimetableDatabase.TYPE_TIMEUNIT, tid);

                    if(!d.time.isBreak()) {
                        d.num = num;
                        num++;
                    }

                    mList.add(d);

                    access.close();
                }

                try {
                    return mVisual.buildWaveAnimator();
                } catch (Exception ex) {
                    Logger.log("TimeGridFragment", "Couldn't build WaveAnimator", ex);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(WaveAnimator anim) {
                mVisual.reloadedTable(anim);
            }
        }.execute();
    }

    public void removeTimeUnitInDb(final TimeUnit t) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                DbAccess access = new DbAccess(getContext());
                TimetableDatabase db = access.get();

                for (int d = Day.OF_WEEK.MONDAY;d <= Day.OF_WEEK.SUNDAY;d++) {
                    Day day = db.getDayForDayOfWeek(d);
                    long[] tids = day.getTimeUnits();

                    int x = -1;
                    for (int i = 0;i < tids.length;i++) {
                        if(tids[i] == t.getId())
                            x = i;
                    }

                    if(x != -1) {
                        tids = ArrayUtils.removeIndexFromLongArray(x, tids);
                        long[]lids = ArrayUtils.removeIndexFromLongArray(x, day.getLessons());
                        long[]pids = ArrayUtils.removeIndexFromLongArray(x, day.getPlaces());

                        day.setTimeUnits(tids);
                        day.setLessons(lids);
                        day.setPlaces(pids);

                        db.updateDatabaseEntry(day);
                    }
                }

                int pos = -1;
                for (int i = 0;i < mList.size();i++) {
                    if (mList.get(i).time.getId() == t.getId())
                        pos = i;
                }

                updateAllNums(t.getId());

                db.removeDatabaseEntry(t);
                access.close();
                return pos;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);

                mVisual.finishedDeletingTimeUnit(integer);
            }
        }.execute();
    }

    private void updateAllNums(long skipId) {
        int num = 1;

        for(int i = 0;i < mList.size();i++) {
            if(skipId != mList.get(i).time.getId()) {
                if(mList.get(i).time.isBreak()) {
                    mList.get(i).num = -1;
                } else {
                    mList.get(i).num = num;
                    num++;
                }
            }
        }
    }

    public Data getDataAtPos(int pos) {
        return mList.get(pos);
    }

    public int getDataSize() {
        return mList.size();
    }

    public Data removeDataAtPos(int pos) {
        return mList.remove(pos);
    }

    public void saveDataToDb(Data d) {
        mTask.doExecute(d);
    }

    public int indexOf(Data d) {
        return mList.indexOf(d);
    }

    @SuppressWarnings("UnusedDeclaration")
    public class Data {
        public TimeUnit time;
        public int num;
        public View.OnClickListener lis;
        public View view;
        public View more;
        public TimelyView firstDigit;
        public TimelyView secondDigit;
        public Button startButton, endButton;
    }

    public class SavingAsyncTask extends AsyncTask<Data, Void, Void> {
        @Override
        protected Void doInBackground(Data... params) {
            DbAccess access = new DbAccess(getContext());
            TimetableDatabase db = access.get();

            Data d = params[0];

            if(d.time.getId() != -1) {
                db.updateDatabaseEntry(d.time);
            } else {
                d.time = (TimeUnit) db.insertDatabaseEntryForId(d.time);
            }

            getLessonNotifier().checkForNewNotifications();

            access.close();

            return null;
        }

        public void doExecute(Data d) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                executeOnExecutor(THREAD_POOL_EXECUTOR, d);
            } else {
                execute(d);
            }

            mTask = new SavingAsyncTask();
        }
    }
}
