package eu.laprell.timetable.fragments;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.ListPopupWindow;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.adnansm.timelytextview.TimelyView;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.TypeEvaluator;
import com.nineoldandroids.util.Property;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.util.ArrayList;

import eu.laprell.timetable.R;
import eu.laprell.timetable.animation.WaveAnimator;
import eu.laprell.timetable.background.Logger;
import eu.laprell.timetable.database.Day;
import eu.laprell.timetable.database.DbAccess;
import eu.laprell.timetable.database.TimeUnit;
import eu.laprell.timetable.database.TimetableDatabase;
import eu.laprell.timetable.utils.AnimUtils;
import eu.laprell.timetable.utils.ArrayUtils;
import eu.laprell.timetable.utils.MetricsUtils;
import eu.laprell.timetable.utils.misc.FlWrapper;
import eu.laprell.timetable.widgets.ShortLoadingDialog;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;

/**
 * Created by david on 07.11.14.
 */
public class TimeGridFragment extends BaseFragment {

    private LinearLayout mTimeContainer;

    private ArrayList<Data> mList;
    private CircularProgressBar mProgress;
    private SavingAsyncTask mTask;

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.add) {
                addNewTime();
            }
        }
    };

    public TimeGridFragment() {
        mList = new ArrayList<Data>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTask = new SavingAsyncTask();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final ViewGroup v = (ViewGroup)inflater.inflate(
                R.layout.fragment_timegrid, container, false);

        mTimeContainer = (LinearLayout)v.findViewById(R.id.time_container);
        mProgress = (CircularProgressBar)v.findViewById(R.id.circular_loading);
        v.findViewById(R.id.add).setOnClickListener(mClickListener);

        loadTable();

        return v;
    }

    private void addNewTime() {
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

        prepareView(pos);
        displayNewItem(d, pos, true);

        mTask.doExecute(d);
    }

    private void loadTable() {
        new AsyncTask<Void, Void, WaveAnimator>() {
            @Override
            protected WaveAnimator doInBackground(Void... params) {
                DbAccess access = new DbAccess(mProgress.getContext());
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
                    return buildWaveAnimator();
                } catch (Exception ex) {
                    Logger.log("TimeGridFragment", "Couldn't build WaveAnimator", ex);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(WaveAnimator anim) {
                AnimUtils.animateProgressExit(mProgress);

                if(!isAdded()) {
                    return;
                }

                if(anim == null) // Fallback solution
                    anim = buildWaveAnimator();

                for (int i = 0;i < mList.size();i++) {
                    displayNewItem(mList.get(i), i, false);
                }

                anim.startOnPreDraw(mTimeContainer);
            }
        }.execute();
    }

    private WaveAnimator buildWaveAnimator() {
        WaveAnimator anim = new WaveAnimator(new WaveAnimator.WaveAnimationApplier() {
            @Override
            public Animator makeAnimationForView(View v, Object data) {
                ObjectAnimator moveIn = ObjectAnimator.ofObject(v, MOVE_IN_PROPERTY,
                        new MoveUpEvaluator(), FlWrapper.of(0f), FlWrapper.of(1f));

                moveIn.setDuration(150);
                moveIn.setInterpolator(new DecelerateInterpolator());
                moveIn.addListener(new AnimUtils.LayerAdapter(v));

                return moveIn;
            }
        });
        anim.setSpeed(MetricsUtils.convertDpToPixel(72));
        int[] loc = AnimUtils.getViewLoc(mTimeContainer);
        loc[0] += mTimeContainer.getWidth() / 2;
        anim.setStartPoint(loc);
        anim.setComputeMoreExactly(true);

        for (int i = 0;i < mList.size();i++) {
            View v = prepareView(i);
            v.setAlpha(0f);

            Data d = mList.get(i);
            updateTime(d);
            int num = getNumToShow(d);
            if(num >= 0) {
                int firstDigit = num / 10;
                int secondDigit = num % 10;

                if (firstDigit <= 0)
                    firstDigit = -1;

                d.firstDigit.setNumber(firstDigit);
                d.secondDigit.setNumber(secondDigit);
            }

            anim.addTarget(v);
        }

        return anim;
    }

    private static final float DISTANCE = MetricsUtils.convertDpToPixel(32);
    private static final Property<View, FlWrapper> MOVE_IN_PROPERTY
            = new Property<View, FlWrapper>(FlWrapper.class, "moveIn") {

        final FlWrapper mFloat = new FlWrapper();

        @Override
        public FlWrapper get(View object) {
            mFloat.mValue = object.getAlpha();
            return mFloat;
        }

        @Override
        public void set(View object, FlWrapper value) {
            object.setAlpha(value.mValue);
            object.setTranslationY(DISTANCE * (1f - value.mValue));
        }
    };

    private class MoveUpEvaluator implements TypeEvaluator<FlWrapper> {
        private FlWrapper mFloat = new FlWrapper();

        @Override
        public FlWrapper evaluate(float fraction, FlWrapper startValue, FlWrapper endValue) {
            mFloat.mValue =  startValue.mValue + ((endValue.mValue - startValue.mValue) * fraction);
            return mFloat;
        }
    }

    private View prepareView(int pos) {
        Data d = mList.get(pos);

        d.view = LayoutInflater.from(mTimeContainer.getContext()).inflate(
                R.layout.list_item_time_grid, mTimeContainer, false);
        d.lis = new ButtonClickListener(d);

        return d.view;
    }

    private void displayNewItem(Data d, int pos, boolean withAnimation) {
        if(withAnimation) {
            updateView(d);
            AnimUtils.animateViewAddingInLayout(d.view, mTimeContainer, pos);
        } else {
            mTimeContainer.addView(d.view, pos);
        }
    }

    private void updateView(Data d) {
        Animator a = animateNumView(d);
        if(a != null)a.start();

        updateTime(d);
    }

    private void updateTime(Data d) {
        d.startButton.setText(d.time.makeTimeString("s"));
        d.endButton.setText(d.time.makeTimeString("e"));
    }

    private Animator animateNumView(Data d) {
        ObjectAnimator a1;
        ObjectAnimator a2;

        if(d.time.isBreak()) {
            a1 = d.firstDigit.animate(getIntTag(d.firstDigit), -1);
            d.firstDigit.setTag(-1);

            a2 = d.secondDigit.animate(getIntTag(d.firstDigit), -1);
            d.secondDigit.setTag(-1);
        } else {
            int firstnum = d.num / 10;
            int lastnum = d.num % 10;

            if(firstnum <= 0) firstnum = -1;

            a1 = d.firstDigit.animate(getIntTag(d.firstDigit), firstnum);
            d.firstDigit.setTag(firstnum);

            a2 = d.secondDigit.animate(getIntTag(d.secondDigit), lastnum);
            d.secondDigit.setTag(lastnum);
        }

        AnimatorSet set = new AnimatorSet();
        set.setDuration(200);
        set.playTogether(a1, a2);

        return set;
    }

    private int getNumToShow(Data d) {
        if(!d.time.isBreak()) {
            return d.num;
        } else {
            return -1;
        }
    }

    private void removeTimeUnit(final TimeUnit t) {
        final ShortLoadingDialog dialog = new ShortLoadingDialog(getActivity());
        dialog.show();
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                DbAccess access = new DbAccess(getActivity());
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

                final int[] loc = new int[2];

                Data d = mList.remove(integer.intValue());
                d.view.getLocationOnScreen(loc);
                loc[0] += d.view.getWidth() / 2;
                loc[1] += d.view.getHeight() / 2;

                dialog.finish(null);
                AnimUtils.animateViewDeletingInLayout(d.view, mTimeContainer, new Runnable() {
                    @Override
                    public void run() {
                        WaveAnimator animator = new WaveAnimator(new WaveAnimator.WaveAnimationApplier<Data>() {
                            @Override
                            public Animator makeAnimationForView(View v, Data data) {
                                return animateNumView(data);
                            }
                        })
                        .setSpeed(MetricsUtils.convertDpToPixel(300))
                        .setStartPoint(loc);

                        for (int i = 0;i < mList.size();i++) {
                            animator.addTarget(mList.get(i).view, mList.get(i));
                        }

                        animator.start();
                    }
                });


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

    /**
     * Gets the tag of a {@link android.view.View} through the getTag() method and
     * converts it to an int
     * @param v the view, who's tag should be converted
     * @return the int value of the tag
     */
    private int getIntTag(View v) {
        return v.getTag() == null ? -1 : ((Integer)v.getTag());
    }

    public class Data {
        TimeUnit time;
        int num;
        ButtonClickListener lis;
        View view;
        View more;
        TimelyView firstDigit;
        TimelyView secondDigit;
        Button startButton, endButton;
    }

    private class ButtonClickListener implements View.OnClickListener {

        private Data mData;

        public ButtonClickListener(Data d) {
            mData = d;

            d.startButton = (Button)d.view.findViewById(R.id.btn_start_time);
            d.endButton = (Button)d.view.findViewById(R.id.btn_end_time);
            d.firstDigit = (TimelyView)d.view.findViewById(R.id.btn_num);
            d.secondDigit = (TimelyView)d.view.findViewById(R.id.btn_num_2);
            d.more = d.view.findViewById(R.id.more);

            d.startButton.setOnClickListener(this);
            d.endButton.setOnClickListener(this);
            d.more.setOnClickListener(this);

            d.firstDigit.setTextColor(d.firstDigit.getResources().getColor(R.color.accent_));
            d.firstDigit.setTextStroke((int) MetricsUtils.convertDpToPixel(2));

            d.secondDigit.setTextColor(d.secondDigit.getResources().getColor(R.color.accent_));
            d.secondDigit.setTextStroke((int) MetricsUtils.convertDpToPixel(2));
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btn_start_time) {
                final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(RadialPickerLayout radialPickerLayout, int i, int i2) {
                                mData.time.setStartTime((i * 60) + i2);

                                mTask.doExecute(mData);

                                updateView(mData);
                            }
                        }, mData.time.getStartTime() / 60 , mData.time.getStartTime() % 60, false, false);

                timePickerDialog.show(getSupportFragmentManager(), "timePicker");
            } else if(v.getId() == R.id.btn_end_time) {
                final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(RadialPickerLayout radialPickerLayout, int i, int i2) {
                                mData.time.setEndTime((i * 60) + i2);

                                mTask.doExecute(mData);

                                updateView(mData);
                            }
                        }, mData.time.getEndTime() / 60 , mData.time.getEndTime() % 60, false, false);

                timePickerDialog.show(getSupportFragmentManager(), "timePicker");
            } else if(v.getId() == R.id.more) {
                showMoreMenu();
            }
        }

        private void showMoreMenu() {
            final ListPopupWindow popUp = new ListPopupWindow(getActivity());
            popUp.setAnimationStyle(R.style.NoEnterAnimationWindow);
            popUp.setAnchorView(mData.more);
            popUp.setWidth((int) MetricsUtils.convertDpToPixel(240));
            popUp.setVerticalOffset(-mData.more.getHeight());
            popUp.setModal(true);
            popUp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        if (mData.time.isBreak()) {
                            updateNumForAllAfter(mList.indexOf(mData), true);
                        }
                    } else if (position == 1) {
                        if (!mData.time.isBreak()) {
                            updateNumForAllAfter(mList.indexOf(mData), false);
                        }
                    } else {
                        if (mList.size() <= 1) {
                            Toast.makeText(getActivity(), R.string.cannot_remove_timeunit_toast,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            MaterialDialog d = new MaterialDialog.Builder(getActivity())
                                    .title(R.string.remove_timeunit)
                                    .content(R.string.remove_timeunit_dialog_text,
                                            mData.time.makeTimeString("s - e"))
                                    .positiveText(R.string.remove)
                                    .negativeText(R.string.cancel)
                                    .callback(new MaterialDialog.ButtonCallback() {
                                        @Override
                                        public void onPositive(MaterialDialog dialog) {
                                            removeTimeUnit(mData.time);
                                        }
                                    })
                                    .build();

                            d.show();

                            //AnimUtils.animateMaterialDialogIn(view, d);
                        }
                    }
                    popUp.dismiss();
                }
            });
            popUp.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    mData.view.setBackgroundColor(Color.TRANSPARENT);
                }
            });
            popUp.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.adapter_list_popup_item,
                    android.R.id.text1, getResources().getStringArray(R.array.array_list_popup_time_more)));
            popUp.show();

            AnimUtils.animateListPopupWindowIn(popUp);

            mData.view.setBackgroundColor(0x77AAAAAA);
        }
    }

    private void updateNumForAllAfter(int pos, boolean newLesson) {
        int curNum = 0;
        int i = pos - 1;

        if(pos > 0) {
            do {
                if(!mList.get(i).time.isBreak())
                    curNum = mList.get(i).num;
                i--;
            } while (i >= 0 && curNum == 0);
        }

        Data d = mList.get(pos);
        d.num = newLesson ? ++curNum : 0;
        d.time.setBreak(!newLesson);

        mTask.doExecute(d);

        updateView(d);

        WaveAnimator waveAnimator = new WaveAnimator(new WaveAnimator.WaveAnimationApplier<Data>() {
            @Override
            public Animator makeAnimationForView(View v, Data d) {
                return animateNumView(d);
            }
        }).setSpeed(MetricsUtils.convertDpToPixel(300)).setStartAnchorView(d.firstDigit, true);

        for(i = pos + 1;i < mList.size();i++) {
            d = mList.get(i);

            d.num = d.time.isBreak() ? 0 : ++curNum;

            waveAnimator.addTarget(d.firstDigit, d);
        }

        waveAnimator.start();
    }

    public FragmentManager getSupportFragmentManager() {
        return getActivity().getSupportFragmentManager();
    }

    @Override
    public int getBackgroundColor() {
        return Color.TRANSPARENT;
    }

    public class SavingAsyncTask extends AsyncTask<Data, Void, Void> {
        @Override
        protected Void doInBackground(Data... params) {
            DbAccess access = new DbAccess(getActivity());
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
