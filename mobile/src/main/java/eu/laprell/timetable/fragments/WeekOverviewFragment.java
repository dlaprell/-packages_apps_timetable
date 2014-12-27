package eu.laprell.timetable.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.ArrayList;

import eu.laprell.timetable.LessonViewActivity;
import eu.laprell.timetable.R;
import eu.laprell.timetable.database.Day;
import eu.laprell.timetable.database.DbAccess;
import eu.laprell.timetable.database.Lesson;
import eu.laprell.timetable.database.Place;
import eu.laprell.timetable.database.TimeUnit;
import eu.laprell.timetable.database.TimetableDatabase;
import eu.laprell.timetable.widgets.TableView;

/**
 * Created by david on 06.11.14.
 */
public class WeekOverviewFragment extends Fragment {

    private TableView.DataProvider mFakeProvider = new TableView.DataProvider() {
        @Override
        public String getHeaderTitle(int pos) {
            return "";
        }

        @Override
        public String getRowTitle(int pos) {
            return "";
        }

        @Override
        public String getContentAt(int x, int y) {
            return "";
        }

        @Override
        public int getColorAt(int x, int y) {
            return Color.TRANSPARENT;
        }

        @Override
        public int getTextColorAt(int x, int y) {
            return Color.TRANSPARENT;
        }
    };

    private TableView mTable;
    private TimeUnit[] mAbsTimes;
    private int[] mNums;
    private String mBreakFirstLetter;
    private String[] mDays;
    private ArrayList<DayData> mList;

    private class DayData {
        private TimeUnit[] mTimes;
        private Day mDay;
        private Lesson[] mLessons;
        private Place[] mPlaces;
    }

    public WeekOverviewFragment() {
        mList = new ArrayList<DayData>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //icToast.makeText(getActivity(), "Coming soon ... ", Toast.LENGTH_SHORT).show();

        mBreakFirstLetter = getString(R.string.break_first_letter);
        mDays = getResources().getStringArray(R.array.array_days);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_week_overview, container, false);

        mTable = ((TableView) v.findViewById(R.id.table));

        mTable.setLegendColor(getResources().getColor(R.color.accent));

        mTable.setDataProvider(mFakeProvider);
        mTable.setAlpha(0f);

        mTable.setInputListener(new TableView.InputListener() {
            @Override
            public boolean onClickCell(int x, int y) {
                //Toast.makeText(mTable.getContext(), "clicked cell with x=" + x + " y=" + y, Toast.LENGTH_SHORT).show();

                if(x >= 0 && x < mList.size()) {
                    DayData data = mList.get(x);

                    if(data != null && y < data.mLessons.length
                            && data.mLessons[y] != null) {
                        Place p = data.mPlaces[y];
                        Lesson l = data.mLessons[y];
                        TimeUnit t = data.mTimes[y];

                        Intent i = new Intent(getActivity(), LessonViewActivity.class);

                        i.putExtra("lesson", l);
                        i.putExtra("timeunit", t);
                        i.putExtra("place", p);
                        i.putExtra("day_of_week", data.mDay.getDayOfWeek());

                        startActivityForResult(i, 1);
                    }
                }

                return true;
            }

            @Override
            public void onLongClickCell(int x, int y) {

            }
        });

        loadTable();

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1) {
            if(resultCode == Activity.RESULT_OK) {
                loadTable();

                mList.clear();
                mTable.setAlpha(0f);
            }
        } else super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadTable() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                DbAccess access = new DbAccess(getActivity());
                TimetableDatabase db = access.get();

                mAbsTimes = db.getTimeUnitsByIds(db.getDatabaseEntries(TimetableDatabase.TYPE_TIMEUNIT));
                mNums = new int[mAbsTimes.length];

                int num = 1;
                for (int i = 0;i < mNums.length;i++)
                    mNums[i] = mAbsTimes[i].isBreak() ? -1 : num++;

                for (int i = Day.OF_WEEK.MONDAY;i <= Day.OF_WEEK.FRIDAY;i++) {
                    DayData d = new DayData();
                    d.mDay = db.getDayForDayOfWeek(i);
                    d.mTimes = new TimeUnit[mAbsTimes.length];
                    d.mLessons = new Lesson[mAbsTimes.length];
                    d.mPlaces = new Place[mAbsTimes.length];

                    long[]lids = d.mDay.getLessons();
                    long[]tids = d.mDay.getTimeUnits();
                    long[]pids = d.mDay.getPlaces();

                    for(int z = 0;z < d.mTimes.length;z++) {
                        TimeUnit ref = mAbsTimes[z];

                        for (int j = 0;j < tids.length;j++) {
                            if(tids[j] == ref.getId()) {
                                d.mTimes[z] = ref;
                                d.mLessons[z] = (Lesson) db.getDatabaseEntryById(TimetableDatabase.TYPE_LESSON,
                                        lids[j]);
                                d.mPlaces[z] = (Place) db.getDatabaseEntryById(TimetableDatabase.TYPE_PLACE,
                                        pids[j]);
                                tids[j] = -1;

                                break;
                            }
                        }
                    }

                    mList.add(d);
                }

                access.close();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                mTable.setNumOfColumns(mList.size());
                mTable.setNumOfRows(mAbsTimes.length);

                mTable.setDataProvider(mProvider);
                ViewPropertyAnimator.animate(mTable).setDuration(300)
                        .alpha(1f);
                mTable.invalidate();
            }
        }.execute();
    }

    private TableView.DataProvider mProvider = new TableView.DataProvider() {
        @Override
        public String getHeaderTitle(int pos) {
            if(pos < mDays.length) {
                return mDays[pos].substring(0, 2);
            }
            return null;
        }

        @Override
        public String getRowTitle(int pos) {
            if(pos < mNums.length) {
                return mNums[pos] == -1 ? mBreakFirstLetter : String.valueOf(mNums[pos]);
            }
            return null;
        }

        @Override
        public String getContentAt(int x, int y) {
            if(x < mList.size()) {
                DayData d = mList.get(x);

                if(y < d.mLessons.length) {
                    if(d.mLessons[y] != null)
                        return d.mLessons[y].getTitle();
                }
            }

            return null;
        }

        @Override
        public int getColorAt(int x, int y) {
            if(x < mList.size()) {
                DayData d = mList.get(x);

                if(y < d.mLessons.length) {
                    if(d.mLessons[y] != null)
                        return d.mLessons[y].getColor() & 0xDDFFFFFF;
                }
            }

            return Color.TRANSPARENT;
        }

        @Override
        public int getTextColorAt(int x, int y) {
            if(x < mList.size()) {
                DayData d = mList.get(x);

                if(y < d.mLessons.length) {
                    return Color.WHITE;
                }
            }
            return Color.GRAY & 0xAAFFFFFF;
        }
    };
}
