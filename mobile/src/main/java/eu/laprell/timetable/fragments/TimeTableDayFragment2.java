package eu.laprell.timetable.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.ArrayList;

import eu.laprell.timetable.BackgroundService;
import eu.laprell.timetable.LessonViewActivity;
import eu.laprell.timetable.R;
import eu.laprell.timetable.animation.ActivityTransitions;
import eu.laprell.timetable.background.Logger;
import eu.laprell.timetable.background.SpecialBitmapCache;
import eu.laprell.timetable.database.Creator;
import eu.laprell.timetable.database.Day;
import eu.laprell.timetable.database.DbAccess;
import eu.laprell.timetable.database.DbUtils;
import eu.laprell.timetable.database.Lesson;
import eu.laprell.timetable.database.Place;
import eu.laprell.timetable.database.TimeUnit;
import eu.laprell.timetable.database.TimetableDatabase;
import eu.laprell.timetable.utils.AnimUtils;
import eu.laprell.timetable.utils.ArrayUtils;
import eu.laprell.timetable.utils.BitmapUtils;
import eu.laprell.timetable.utils.Const;
import eu.laprell.timetable.utils.Dialogs;
import eu.laprell.timetable.utils.MetricsUtils;
import eu.laprell.timetable.widgets.LessonCardView;
import eu.laprell.timetable.widgets.NavigationFrameLayout;
import eu.laprell.timetable.widgets.VerticalDropShadowCard;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;

/**
 * Created by david on 07.11.14.
 */
public class TimeTableDayFragment2 extends Fragment {

    private static final String TAG = "TimeTableDayFragment2";

    private static final int REQUEST_CODE_MAYBE_REFRESH = 2;

    private static volatile int CRASHED = 0;

    private NavigationFrameLayout mContainer;
    private RecyclerView mRecyclerView;
    private TimeTableDayDatabaseAdapter mAdapter;

    private Day mDay;

    private ArrayList<Data> mList;
    private CircularProgressBar mProgress;

    private TimeUnit[] mTimes;

    private ListAdapter mEditAdapter;
    private int mCurrentEdit;
    private ListPopupWindow mEditList;

    private Button mDayText;

    public TimeTableDayFragment2() {
        mList = new ArrayList<Data>();
    }

    private int mDayToDisplay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            if(getArguments().getInt("day", -1) != -1)
                mDayToDisplay = getArguments().getInt("day", -1);
        }

        String[] options = getResources().getStringArray(R.array.array_list_popup_edit_entry);
        mEditAdapter = new ArrayAdapter<>(getActivity(), R.layout.popup_window_text,
                android.R.id.text1, options);
        mEditList = new ListPopupWindow(getActivity());
        mEditList.setAdapter(mEditAdapter);

        mEditList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    makeFreeTime();
                } else {
                    makeNewLesson(null, true);
                }

                mEditList.dismiss();
            }
        });
        mEditList.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mContainer.setEnableTouchEventsForChildren(true);
            }
        });

        mTimes = new TimeUnit[0];
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mList.clear();
        mAdapter = null;
    }

    private void makeFreeTime() {
        AnimUtils.animateProgressEnter(mProgress);

        long[] lids = mDay.getLessons();
        lids[mCurrentEdit] = -1;
        mDay.setLessons(lids);

        DbUtils.saveDayAsync(mDay, getActivity(), new DbUtils.TaskFinishedCallback() {
            @Override
            public void onFinished() {
                loadTable();
            }
        });
    }

    private void convertInFreeTime(TimeUnit t) {
        long[] lids = mDay.getLessons();
        lids[mCurrentEdit] = -1;
        mDay.setLessons(lids);

        long[] tids = mDay.getTimeUnits();
        tids[mCurrentEdit] = t.getId();
        mDay.setTimeUnits(tids);

        long[] pids = mDay.getPlaces();
        pids[mCurrentEdit] = -1;
        mDay.setPlaces(pids);

        DbUtils.saveDayAsync(mDay, getActivity(), new DbUtils.TaskFinishedCallback() {
            @Override
            public void onFinished() {
                loadTable();
            }
        });
    }

    private void addFreeTime(TimeUnit t) {
        AnimUtils.animateProgressEnter(mProgress);

        mDay.expandIdArrays();

        convertInFreeTime(t);
    }

    private void askforBreak(final TimeUnit t) {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.insert_break)
                .content(R.string.insert_break_dialog_text)
                .positiveText(R.string.break_)
                .negativeText(R.string.lesson)
                .callback(new MaterialDialog.Callback() {
                    @Override
                    public void onNegative(MaterialDialog materialDialog) {
                        makeNewLesson(null, true);
                    }

                    @Override
                    public void onPositive(MaterialDialog materialDialog) {
                        addFreeTime(t);

                        // TODO export into function
                        mContainer.setEnableTouchEventsForChildren(true);
                    }
                }).build();

        dialog.show();
    }

    private void makeNewLesson() {
        makeNewLesson(null, false);
    }

    private void makeNewLesson(TimeUnit time, boolean force) {
        TimeUnit tmp;

        if(time == null) {
            if (mList.size() == 0) {
                tmp = mTimes[0];
            } else if (mCurrentEdit == mList.size()) {
                TimeUnit tBefore = mList.get(mList.size() - 1).time;
                tmp = mTimes[ArrayUtils.getTimeUnitAfter(tBefore, mTimes)];
            } else
                tmp = mList.get(mCurrentEdit).time;
        } else {
            tmp = time;
        }

        if(tmp.isBreak() && !force) {
            askforBreak(tmp);
            return;
        }

        final TimeUnit t = tmp;

        mContainer.setEnableTouchEventsForChildren(true);

        Dialogs.makeSimpleLessonDialog(getActivity(), new Dialogs.LessonCreatedCallback() {
            @Override
            public void createdLesson(Lesson l) {
                if(l == null || l.getTitle() == null) {
                    Toast.makeText(getActivity(), "You must specify a subject", Toast.LENGTH_SHORT).show();
                } else {
                    final Creator.Data data = new Creator.Data();

                    data.time_id = t.getId();
                    if(l.getId() > 0)
                        data.lesson_id = l.getId();
                    else
                        data.lesson = l.getTitle();
                    data.color = l.getColor() & 0x00FFFFFF;
                    data.day_of_week = mDay.getDayOfWeek();

                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            Creator.createLessonFromData(BackgroundService.get().getTimetableDatabase(), data);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);

                            loadTable();
                        }
                    }.execute();
                }
            }

            @Override
            public void createFreetime() {
                final Creator.Data data = new Creator.Data();

                data.time_id = t.getId();
                data.day_of_week = mDay.getDayOfWeek();

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        Creator.createFreeTimeFromData(BackgroundService.get().getTimetableDatabase(), data);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);

                        loadTable();
                    }
                }.execute();
            }
        }, Day.getString(mDay.getDayOfWeek(), getActivity()), t);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mContainer = (NavigationFrameLayout)inflater.inflate(
                R.layout.fragment_timetable_dayview, container, false);

        mRecyclerView = (RecyclerView)mContainer.findViewById(
                R.id.timetable_dayview_recycler_view);

        mDayText = (Button)mContainer.findViewById(R.id.day_text);
        if(mDayText != null) {
            mDayText.setText(mDayText.getResources().getStringArray(R.array.array_days)[mDayToDisplay - 1]);
        }

        // use the true setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(false);

        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // specify an adapter (see also next example)
        mAdapter = new TimeTableDayDatabaseAdapter(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int itemPosition = mRecyclerView.getChildPosition(v);

                        if(mAdapter.getItemViewType(itemPosition) == 1) {
                            LessonCardView card = (LessonCardView)v.findViewById(R.id.lesson_card_view);

                            card.setAlpha(0.5f);
                            ViewPropertyAnimator.animate(card).alpha(1f).setDuration(300)
                                    .setInterpolator(new DecelerateInterpolator()).start();

                            Intent i = new Intent(getActivity(), LessonViewActivity.class);

                            Data d = mList.get(itemPosition);
                            int color = d.lesson.getColor();

                            i.putExtra("lesson", d.lesson);
                            i.putExtra("timeunit", d.time);
                            i.putExtra("place", d.place);
                            i.putExtra("day_of_week", mDayToDisplay);

                            ActivityTransitions.makeHeroTransitionFromLessonView(card, color, d.scale_config, i);
                            startActivityForResult(i, REQUEST_CODE_MAYBE_REFRESH);

                            if(!Const.FW_SUPPORTS_HERO_TRANSITION)
                                getActivity().overridePendingTransition(0, 0);
                        } else if (itemPosition == mList.size()) {
                            // Special case: Some time is missing!
                            if(mList.size() != 0
                                    && mList.get(mList.size() - 1).time.isMatching(mTimes[mTimes.length - 1])) {
                                searchForMissing();
                                return;
                            }

                            long[] tids = ArrayUtils.expandByOne(mDay.getTimeUnits());
                            if(tids.length == mList.size()) {
                                tids[itemPosition] = mTimes[itemPosition].getId();
                                mDay.setTimeUnits(tids);

                                long[] lids = ArrayUtils.expandByOne(mDay.getLessons());
                                lids[itemPosition] = -1;
                                mDay.setLessons(lids);

                                long[] pids = ArrayUtils.expandByOne(mDay.getPlaces());
                                pids[itemPosition] = -1;
                                mDay.setPlaces(pids);
                            }

                            mContainer.setEnableTouchEventsForChildren(false);

                            mCurrentEdit = itemPosition;

                            makeNewLesson();
                        }
                    }
                }, mMoreViewClick
        );
        mRecyclerView.setAdapter(mAdapter);

        mProgress = (CircularProgressBar)mContainer.findViewById(R.id.circular_loading);

        AnimUtils.afterPreDraw(mRecyclerView, new Runnable() {
            @Override
            public void run() {
                loadTable();
            }
        });

        return mContainer;
    }

    private View.OnClickListener mMoreViewClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View original = null;
            if(v.getId() == R.id.more) {
                if(v.getParent() instanceof View) {
                    original = (View)v.getParent();
                }
            }

            if(original == null) return;

            int itemPosition = mRecyclerView.getChildPosition(original);

            /*if(original instanceof VerticalDropShadowCard) {
                VerticalDropShadowCard card = (VerticalDropShadowCard)original;
                card.setBackgroundColor(0xFFDDDDDD);
            }*/

            mContainer.setEnableTouchEventsForChildren(false);

            mEditList.setAnchorView(v);
            mEditList.setVerticalOffset(-v.getHeight());
            mEditList.setContentWidth((int) MetricsUtils.convertDpToPixel(196f));
            mEditList.show();

            AnimUtils.animateListPopupWindowIn(mEditList);

            mCurrentEdit = itemPosition;
        }
    };

    private void searchForMissing() {
        long[] times = mDay.getTimeUnits();

        for (int i = 0;i < times.length;i++) {
            if(times[i] != mTimes[i].getId()) {
                makeNewLesson(mTimes[i], false);
                return;
            }
        }
    }

    private Data getDataFrom(TimetableDatabase db, Day d, int index, int lessonNum) {
        Data data = new Data();

        TimeUnit t = (TimeUnit) db.getDatabaseEntryById(TimetableDatabase.TYPE_TIMEUNIT,
                d.getTimeUnits()[index]);
        long lid = d.getLessons()[index];

        if(t == null) // Here is something really going wrong
            return null;

        if(!t.isBreak() || lid != -1) {
            data.lesson = (Lesson) db.getDatabaseEntryById(TimetableDatabase.TYPE_LESSON, lid);

            data.num = t.isBreak() ? -1 : lessonNum;
        }

        if(lid != -1) {
            data.place = (Place) db.getDatabaseEntryById(TimetableDatabase.TYPE_PLACE,
                    d.getPlaces()[index]);
        }

        data.time = t;

        return data;
    }

    private void fetchData() {
        DbAccess access = new DbAccess(getActivity());

        mList.clear();

        TimetableDatabase db = access.get();
        int dayOfWeek = mDayToDisplay;

        mTimes = db.getTimeUnitsByIds(db.getDatabaseEntries(TimetableDatabase.TYPE_TIMEUNIT));
        mDay = db.getDayForDayOfWeek(dayOfWeek);

        if(CRASHED > 0)
            return;

        View v;

        try {
            v = LayoutInflater.from(mRecyclerView.getContext())
                    .inflate(R.layout.times_list_item_cards, mRecyclerView, false);
        } catch (Throwable t) {
            CRASHED++;
            Log.d("Timetable", "FATAL! ", t);
            throw new RuntimeException("Damn - Alina was right ^^");
        }

        final LessonCardView card = (LessonCardView) v.findViewById(R.id.lesson_card_view);
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        int w = mRecyclerView.getWidth() - (v.getPaddingLeft() + v.getPaddingRight());
        int h = card.getLayoutParams().height;

        card.calculateSizes(w, h);
        Rect imageRect = card.getImageRect();

        int lesson = 1;
        for (int i = 0;i < mDay.getTimeUnits().length;i++) {
            Data data = getDataFrom(db, mDay, i, lesson);

            if(data == null) // Here is something really going wrong
                continue;

            if(data.lesson != null) {
                int resId = db.getImageIdForLesson(data.lesson);

                if(resId != -1) {
                    BitmapDrawable bitmapDrawable = scaleForInserting(resId, imageRect, data);
                    if (bitmapDrawable != null) {

                        if(data.lesson.hasColor()) {
                            PorterDuff.Mode mMode = PorterDuff.Mode.MULTIPLY;
                            bitmapDrawable.setColorFilter(data.lesson.getColor(), mMode);
                        }

                        data.image = bitmapDrawable;
                    }
                }
            }

            if(!data.time.isBreak())
                lesson++;

            mList.add(data);
        }

        access.close();
    }

    private void loadTable() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                fetchData();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                AnimUtils.animateProgressExit(mProgress);

                if(mAdapter != null)
                    mAdapter.notifyDataSetChanged();

                mRecyclerView.scrollBy(0, 1);
            }
        }.execute();
    }

    private void refreshTable() {
        AnimUtils.animateProgressEnter(mProgress);

        mAdapter.setCountToNull(true);

        new AsyncTask<Void, Void, int[]>() {
            @Override
            protected int[] doInBackground(Void... params) {
                fetchData();

                for(Data d : mList) {
                    d.first = true;
                }

                return null;
            }

            @Override
            protected void onPostExecute(int[] res) {
                AnimUtils.animateProgressExit(mProgress);

                mRecyclerView.scrollBy(0, 50);
                mRecyclerView.scrollBy(0, -50);

                mAdapter.setCountToNull(false);
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    private class Data {
        private Lesson lesson;
        private TimeUnit time;
        private Place place;
        private int num;
        private boolean first = true;
        private BitmapDrawable image;
        private BitmapUtils.ScalingConfig scale_config;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View mView;
        public VerticalDropShadowCard mShadow;
        public LessonCardView mCard;
        public TextView mCaption, mSubCaption, mNr, mTime;
        public ImageView mImage;

        public ViewHolder(View v) {
            super(v);
            mView = v;

            if(v instanceof VerticalDropShadowCard)
                mShadow = (VerticalDropShadowCard)v;

            mCaption = (TextView)v.findViewById(R.id.text);
            mSubCaption = (TextView)v.findViewById(R.id.room);
            mImage = (ImageView)v.findViewById(R.id.image);
            mNr = (TextView)v.findViewById(R.id.nr);
            mTime = (TextView)v.findViewById(R.id.time);

            mCard = (LessonCardView)v.findViewById(R.id.lesson_card_view);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_MAYBE_REFRESH) {
            if(resultCode == Activity.RESULT_OK) {
                refreshTable();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private BitmapDrawable scaleForInserting(int resid, Rect r, Data data) {
        try {
            Bitmap original = SpecialBitmapCache.getInstance().loadBitmap(resid, r.width(), r.height());

            BitmapUtils.ScalingConfig c = BitmapUtils.makeScaleConfigCenterCrop(
                    original, r.width(), r.height());
            c.res_id = resid;

            data.scale_config = c;

            Bitmap newB = BitmapUtils.scaleCenterCrop(original, c);
            //original.recycle();

            return new BitmapDrawable(getResources(), newB);
        } catch (Exception ex) {
            Logger.log(TAG, "Failed to scale Drawable for Inserting", ex);
        }

        return null;
    }

    public class TimeTableDayDatabaseAdapter extends RecyclerView.Adapter<ViewHolder> {

        private View.OnClickListener mListener, mMoreListener;
        private boolean mCountNull = false;

        public TimeTableDayDatabaseAdapter(View.OnClickListener lis, View.OnClickListener lis2) {
            mListener = lis;
            mMoreListener = lis2;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == mList.size())
                return 4;

            if(mList.get(position).time.isBreak() && mList.get(position).lesson == null) {
                return 2; //Break without lesson
            } else if(mList.get(position).lesson == null){
                return 3; //Just without lesson
            } else {
                return 1; //Normal lesson
            }
        }

        @Override
        public long getItemId(int position) {
            if(position < mList.size())
                return mList.get(position).time.getId();
            else
                return -1;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int layout = 0;
            switch (viewType) {
                case 4: layout = R.layout.activity_add_item_add; break;
                case 3: layout = R.layout.times_list_item_no_cards_break; break;
                case 2: layout = R.layout.times_list_item_no_cards_break; break;
                case 1: layout = R.layout.times_list_item_cards; break;
            }

            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(layout, parent, false);

            // set the view's size, margins, paddings and layout parameters
            ViewHolder vh = new ViewHolder(v);
            v.setOnClickListener(mListener);
            return vh;
        }


        private void onBindViewHolderDebug(ViewHolder holder, int position) {
            int type = getItemViewType(position);

            // - get element from your dataset at this position
            // - replace the contents of the view with that element

            if(position == mList.size()) {
                holder.mView.setAlpha(0f);
                ViewPropertyAnimator.animate(holder.mView)
                        .alpha(1f).setDuration(300).start();
                return;
            }

            Data d = mList.get(position);

            if(type == 1) {

                if(holder.mShadow != null) {
                    holder.mShadow.setDrawingTopShadow(position > 0
                            && getItemViewType(position - 1) != 1);

                    holder.mShadow.setDrawingBottomShadow(position < getItemCount() - 1
                            && getItemViewType(position + 1) != 1);
                }

                LessonCardView c = holder.mCard;
                c.setTitle(d.lesson.getTitle());
                /*if(d.place != null && d.place.getTitle() != null)
                    c.setAdditionalInfo(d.place.getTitle());
                else if(d.lesson.getTeacher() != null)
                    c.setAdditionalInfo(d.lesson.getTeacher());*/
                c.setLessonNr(d.num);
                c.setStartTime(d.time.makeTimeString("s"));
                c.setEndTime(d.time.makeTimeString("e"));

                if(d.image != null)
                    c.setImage(d.image);
                else
                    c.setImage(new ColorDrawable(d.lesson.getColor()));
            } else if(type == 3) {
                //holder.mNr.setText(String.valueOf(d.num));
                holder.mTime.setText(d.time.makeTimeString("s\ne"));
                holder.mCaption.setText(R.string.free);
            } else if (type == 2) {
                holder.mTime.setText(d.time.makeTimeString("s\ne"));
            }

            // Start animation if necessary
            if (d.first) {
                holder.mView.setAlpha(0f);
                holder.mView.setTranslationY(MetricsUtils.convertDpToPixel(64f));
                ViewPropertyAnimator.animate(holder.mView)
                        .translationY(0f)
                        .alpha(1f)
                        .setDuration(300)
                        .setInterpolator(new DecelerateInterpolator())
                        .setListener(new AnimUtils.LayerAdapter(holder.mView))
                        .start();

                d.first = false;
            }

            if(holder.mView.findViewById(R.id.more) != null)
                holder.mView.findViewById(R.id.more).setOnClickListener(mMoreListener);
        }
        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            try {
                onBindViewHolderDebug(holder, position);
            } catch (Exception ex) {
                String txt = "position=" + position;

                if(position < mList.size()) {
                    Data d = mList.get(position);

                    if(d == null) {
                        txt += " Data in mList == null";
                    } else {
                        txt += " ";
                        txt += d.lesson == null ? " d.lesson == null" : d.lesson.getTitle();
                    }
                }

                if(holder == null) txt += " holder == null";
                else {
                    txt += " holder != null";

                    txt += " holder.mView " + (holder.mView == null ? "==null" : holder.mView.getId());
                }

                Logger.log(TAG, txt, ex);

                //throw new RuntimeException(txt, ex);
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            if(mCountNull)
                return 0;

            if(mList.size() == mTimes.length)
                return mList.size();
            else
                return mList.size() + 1;
        }

        public void setCountToNull(boolean b) {
            mCountNull = b;
        }
    }
}
