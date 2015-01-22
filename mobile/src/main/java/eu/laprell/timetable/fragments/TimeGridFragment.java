package eu.laprell.timetable.fragments;

import android.app.Activity;
import android.graphics.Color;
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

import eu.laprell.timetable.R;
import eu.laprell.timetable.animation.WaveAnimator;
import eu.laprell.timetable.database.TimeUnit;
import eu.laprell.timetable.fragments.model.TimeGridModel;
import eu.laprell.timetable.fragments.model.TimeGridModel.Data;
import eu.laprell.timetable.utils.AnimUtils;
import eu.laprell.timetable.utils.MetricsUtils;
import eu.laprell.timetable.utils.misc.FlWrapper;
import eu.laprell.timetable.widgets.ShortLoadingDialog;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;

/**
 * Created by david on 07.11.14
 */
public class TimeGridFragment extends BaseFragment implements TimeGridModel.TimeGridVisualizer {

    private LinearLayout mTimeContainer;
    private CircularProgressBar mProgress;
    private TimeGridModel mModel;

    private ShortLoadingDialog mDialog;

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.add) {
                addNewTime();
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mModel = new TimeGridModel(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final ViewGroup v = (ViewGroup)inflater.inflate(
                R.layout.fragment_timegrid, container, false);

        mTimeContainer = (LinearLayout)v.findViewById(R.id.time_container);
        mProgress = (CircularProgressBar)v.findViewById(R.id.circular_loading);
        v.findViewById(R.id.add).setOnClickListener(mClickListener);

        final View tabletScroll = v.findViewById(R.id.tablet_scroll);
        if(tabletScroll != null) {
            AnimUtils.afterPreDraw(tabletScroll, new Runnable() {
                @Override
                public void run() {
                    int height = tabletScroll.getHeight();
                    mTimeContainer.setMinimumHeight(height);
                }
            });
        }

        mModel.reloadTableAsync();

        return v;
    }

    private void addNewTime() {
        mModel.addNewTime();
    }

    @Override
    public View prepareViewFromData(Data d) {
        d.view = LayoutInflater.from(mTimeContainer.getContext()).inflate(
                R.layout.list_item_time_grid, mTimeContainer, false);
        d.lis = new ButtonClickListener(d);

        return d.view;
    }

    @Override
    public void animateNewViewIn(Data d, int pos) {
        updateCompleteView(d);
        AnimUtils.animateViewAddingInLayout(d.view, mTimeContainer, pos);
    }

    public WaveAnimator buildWaveAnimator() {
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

        for (int i = 0;i < mModel.getDataSize();i++) {
            Data d = mModel.getDataAtPos(i);

            View v = prepareViewFromData(d);
            v.setAlpha(0f);

            updateTime(d);
            int num = getNumToShow(d);
            if(num >= 0) {
                int firstDigit = num / 10;
                int secondDigit = num % 10;

                if (firstDigit <= 0)
                    firstDigit = -1;

                if (firstDigit > 0) {
                    d.firstDigit.setNumber(firstDigit);
                    d.secondDigit.setNumber(secondDigit);
                } else {
                    d.firstDigit.setNumber(secondDigit);
                    d.secondDigit.setNumber(-1);
                }
            }

            anim.addTarget(v);
        }

        return anim;
    }

    @Override
    public void reloadedTable(@Nullable WaveAnimator anim) {
        AnimUtils.animateProgressExit(mProgress);

        if(!isAdded()) {
            return;
        }

        if(anim == null) // Fallback solution
            anim = buildWaveAnimator();

        for (int i = 0;i < mModel.getDataSize();i++) {
            mTimeContainer.addView(mModel.getDataAtPos(i).view, i);
        }

        anim.startOnPreDraw(mTimeContainer);
    }

    private void deleteTimeUnit(TimeUnit t) {
        mDialog = new ShortLoadingDialog(getActivity());
        mDialog.show();

        mModel.removeTimeUnitInDb(t);
    }

    @Override
    public void finishedDeletingTimeUnit(int pos) {
        final int[] loc = new int[2];

        TimeGridModel.Data d = mModel.removeDataAtPos(pos);
        d.view.getLocationOnScreen(loc);
        loc[0] += d.view.getWidth() / 2;
        loc[1] += d.view.getHeight() / 2;

        mDialog.finish(null);
        mDialog = null;
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

                for (int i = 0;i < mModel.getDataSize();i++) {
                    Data d = mModel.getDataAtPos(i);
                    animator.addTarget(d.view, d);
                }

                animator.start();
            }
        });
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

    private void updateCompleteView(Data d) {
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

            if(firstnum > 0) {
                a1 = d.firstDigit.animate(getIntTag(d.firstDigit), firstnum);
                d.firstDigit.setTag(firstnum);

                a2 = d.secondDigit.animate(getIntTag(d.secondDigit), lastnum);
                d.secondDigit.setTag(lastnum);
            } else {
                a1 = d.firstDigit.animate(getIntTag(d.firstDigit), lastnum);
                d.firstDigit.setTag(lastnum);

                a2 = d.secondDigit.animate(getIntTag(d.secondDigit), -1);
                d.secondDigit.setTag(-1);
            }
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

    /**
     * Gets the tag of a {@link android.view.View} through the getTag() method and
     * converts it to an int
     * @param v the view, who's tag should be converted
     * @return the int value of the tag
     */
    private int getIntTag(View v) {
        return v.getTag() == null ? -1 : ((Integer)v.getTag());
    }

    private class ButtonClickListener implements View.OnClickListener {

        private TimeGridModel.Data mData;

        public ButtonClickListener(TimeGridModel.Data d) {
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

                                mModel.saveDataToDb(mData);

                                updateCompleteView(mData);
                            }
                        }, mData.time.getStartTime() / 60 , mData.time.getStartTime() % 60, false, false);

                timePickerDialog.show(getSupportFragmentManager(), "timePicker");
            } else if(v.getId() == R.id.btn_end_time) {
                final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(RadialPickerLayout radialPickerLayout, int i, int i2) {
                                mData.time.setEndTime((i * 60) + i2);

                                mModel.saveDataToDb(mData);

                                updateCompleteView(mData);
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
                            updateNumForAllAfter(mModel.indexOf(mData), true);
                        }
                    } else if (position == 1) {
                        if (!mData.time.isBreak()) {
                            updateNumForAllAfter(mModel.indexOf(mData), false);
                        }
                    } else {
                        if (mModel.getDataSize() <= 1) {
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
                                            deleteTimeUnit(mData.time);
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
            popUp.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.adapter_list_popup_item,
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
                Data d = mModel.getDataAtPos(pos);
                if(!d.time.isBreak())
                    curNum = d.num;
                i--;
            } while (i >= 0 && curNum == 0);
        }

        Data d = mModel.getDataAtPos(pos);
        d.num = newLesson ? ++curNum : -1;
        d.time.setBreak(!newLesson);

        mModel.saveDataToDb(d);

        updateCompleteView(d);

        WaveAnimator waveAnimator = new WaveAnimator(new WaveAnimator.WaveAnimationApplier<Data>() {
            @Override
            public Animator makeAnimationForView(View v, Data d) {
                return animateNumView(d);
            }
        }).setSpeed(MetricsUtils.convertDpToPixel(300)).setStartAnchorView(d.firstDigit, true);

        for(i = pos + 1;i < mModel.getDataSize();i++) {
            d = mModel.getDataAtPos(i);

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
}
